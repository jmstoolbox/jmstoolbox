/*
 * Copyright (C) 2015 Denis Forveille titou10.titou10@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.titou10.jtb.qm.solace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.gen.SessionDef;
import org.titou10.jtb.jms.qm.DestinationData;
import org.titou10.jtb.jms.qm.JMSPropertyKind;
import org.titou10.jtb.jms.qm.QManager;
import org.titou10.jtb.jms.qm.QManagerProperty;
import org.titou10.jtb.jms.qm.QueueData;
import org.titou10.jtb.jms.qm.TopicData;

import com.google.gson.Gson;
import com.solace.labs.sempclient.samplelib.ApiClient;
import com.solace.labs.sempclient.samplelib.ApiException;
import com.solace.labs.sempclient.samplelib.api.QueueApi;
import com.solace.labs.sempclient.samplelib.model.MsgVpnQueue;
import com.solace.labs.sempclient.samplelib.model.MsgVpnQueueResponse;
import com.solace.labs.sempclient.samplelib.model.MsgVpnQueuesResponse;
import com.solace.labs.sempclient.samplelib.model.SempError;
import com.solace.labs.sempclient.samplelib.model.SempMetaOnlyResponse;
import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolJmsUtility;
import com.solacesystems.jms.SupportedProperty;

/**
 * 
 * Implements Solace
 * 
 * @author Denis Forveille
 *
 */
public class SolaceQManager extends QManager {

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(SolaceQManager.class);

	private static final String SOLJMS_INITIAL_CONTEXT_FACTORY = "com.solacesystems.jndi.SolJNDIInitialContextFactory";
	private static final String MESSAGE_VPN = "msgVpn";
	private static final String CF_JNDI_NAME = "cfJndiName";
	private static final String QUEUE_JNDI_NAMES = "queueJndiNames";
	private static final String TOPIC_JNDI_NAMES = "topicJndiNames";
	private static final String PHYSICAL_QUEUE_NAME = "physicalQueueName";
	
	private static final String MGMT_URL = "mgmtUrl";
	private static final String MGMT_USERNAME = "mgmtUsername";
	private static final String MGMT_PASSWORD = "mgmtPassword";
	
	private static final String WAIT_FOR_FIRST_MSG = "waitForFirstMsg";
	
	private static final List<String> QUEUE_ATTRIBUTES = Arrays.asList(new String[] {"queueName", "ingressEnabled", "egressEnabled", 
			"accessType", "maxBindCount", "maxMsgSpoolUsage", "owner", "permission"});

	private final Map<Integer, SessionInfo> sessionsInfo = new HashMap<>();
	private final Map<Integer, InitialContext> jndiContexts = new HashMap<>();

	private List<QManagerProperty> parameters = new ArrayList<QManagerProperty>();
	
	private QueueApi sempQueueApiInstance = null;
	

	public SolaceQManager() {
		log.debug("Instantiate Solace");
		parameters.add(
				new QManagerProperty(MESSAGE_VPN, true, JMSPropertyKind.STRING, false, "message vpn name", "default"));
		parameters.add(new QManagerProperty(CF_JNDI_NAME, false, JMSPropertyKind.STRING, false,
				"connection factory JNDI name (e.g /jms/cf/default)", ""));
		parameters.add(new QManagerProperty(QUEUE_JNDI_NAMES, false, JMSPropertyKind.STRING, false,
				"comma separated queue JNDI names (e.g /jms/queue/q1,/jms/queue/q2)", ""));
		parameters.add(new QManagerProperty(TOPIC_JNDI_NAMES, false, JMSPropertyKind.STRING, false,
				"comma separated topic JNDI names (e.g /jms/queue/t1,/jms/queue/t2)", ""));		
		
		parameters.add(new QManagerProperty(MGMT_URL, false, JMSPropertyKind.STRING, false,
		"MGMT url (eg 'http://localhost:8080','https://localhost:8943)", ""));
		parameters.add(new QManagerProperty(MGMT_USERNAME, false, JMSPropertyKind.STRING, false,
		"MGMT username", ""));
		parameters.add(new QManagerProperty(MGMT_PASSWORD, false, JMSPropertyKind.STRING, false,
		"MGMT password", ""));
		parameters.add(new QManagerProperty(PHYSICAL_QUEUE_NAME, false, JMSPropertyKind.STRING, false,
		"physical queue name", ""));		
		
		parameters.add(new QManagerProperty(WAIT_FOR_FIRST_MSG, true, JMSPropertyKind.INT, false,
				"The number of milliseconds to wait for the first message", "250"));
	}

	@Override
	public Connection connect(SessionDef sessionDef, boolean showSystemObjects, String clientID) throws Exception {

		log.info("connecting to {} - {}", sessionDef.getName(), clientID);

		// Save System properties
		saveSystemProperties();

		try {
			// Extract properties
			Map<String, String> mapProperties = extractProperties(sessionDef);
			String brokerUrl = sessionDef.getHost() + ":" + sessionDef.getPort();
			String username = sessionDef.getActiveUserid();
			if (username == null) {
				throw new Exception("Username cannot be empty");
			}
			String password = sessionDef.getActivePassword();
			if (password == null) {
				password = "";
			}
			String msgVpn = mapProperties.get(MESSAGE_VPN);
			
			// Lookup connection factory via JNDI
			String cfJndiName = mapProperties.get(CF_JNDI_NAME);
			// Lookup queues via JNDI
			String queueJndiNames = mapProperties.get(QUEUE_JNDI_NAMES);
			// Lookup topics via JNDI 
			String topicJndiNames = mapProperties.get(TOPIC_JNDI_NAMES);						
			
			
			// Using MGMT Management interface to look up queues under a message VPN
			String mgmtUrl = mapProperties.get(MGMT_URL);
			String mgmtUsername = mapProperties.get(MGMT_USERNAME);
			String mgmtPassword = mapProperties.get(MGMT_PASSWORD);
			if (checkAvailable(mgmtUrl)) {
				if (!checkAvailable(mgmtUsername)) {
					throw new Exception("Management username cannot be empty");
				}
				if (!checkAvailable(mgmtPassword)) {
					throw new Exception("Management password cannot be empty");
				}				
			}
			
			// physical queue  name
			String physicalQueueName = mapProperties.get(PHYSICAL_QUEUE_NAME);
			if (checkAvailable(physicalQueueName) && !checkAvailable(mgmtUrl)) {
				throw new Exception("Management URL and credentials are required to look up a physical queue");
			}
			
			SessionInfo sessionInfo = new SessionInfo();
			sessionInfo.setMsgVpn(msgVpn);
			sessionInfo.setCfJndiName(cfJndiName);
			sessionInfo.setQueueJndiNames(queueJndiNames);
			sessionInfo.setTopicJndiNames(topicJndiNames);
			sessionInfo.setMgmtUrl(mgmtUrl);
			sessionInfo.setMgmtPassword(mgmtPassword);
			sessionInfo.setMgmtUsername(mgmtUsername);
			sessionInfo.setPhysicalQueueName(physicalQueueName);
			
			Hashtable<String, Object> env = new Hashtable<>();
			env.put(InitialContext.INITIAL_CONTEXT_FACTORY, SOLJMS_INITIAL_CONTEXT_FACTORY);
			env.put(InitialContext.PROVIDER_URL, brokerUrl);
			env.put(Context.SECURITY_PRINCIPAL, username);
			env.put(Context.SECURITY_CREDENTIALS, password);
			env.put(SupportedProperty.SOLACE_JMS_VPN, msgVpn);
			env.put(SupportedProperty.SOLACE_JMS_SSL_VALIDATE_CERTIFICATE, false);
			InitialContext ctx = new InitialContext(env);

			ConnectionFactory cf = null;
			// JMS Connections
			if (checkAvailable(cfJndiName)) {
				// lookup connection factory via JDNI
				cf = (ConnectionFactory) ctx.lookup(cfJndiName);
			} else {
				// programmatically creating connection factory
		       SolConnectionFactory scf = SolJmsUtility.createConnectionFactory();
		       scf.setHost(brokerUrl);
		       scf.setUsername(username);
		       scf.setPassword(password);
		       scf.setDirectTransport(false);
		       scf.setVPN(msgVpn);
		       cf = scf;
			}

			Connection jmsConnection = cf.createConnection();
			jmsConnection.setClientID(clientID);

			// Store per connection related data
			Integer hash = jmsConnection.hashCode();
			if (ctx != null) {
				jndiContexts.put(hash, ctx);
			}
			sessionsInfo.put(hash, sessionInfo);

			jmsConnection.start();

			return jmsConnection;
		} finally {
			restoreSystemProperties();
		}
	}

	@Override
	public void close(Connection jmsConnection) throws JMSException {
		log.debug("close connection {}", jmsConnection);

		Integer hash = jmsConnection.hashCode();
		Context ctx = jndiContexts.get(hash);

		if (ctx != null) {
			try {
				ctx.close();
			} catch (Exception e) {
				log.warn("Exception occurred while closing initial context. Ignore it. Msg={}", e.getMessage());
			}
			jndiContexts.remove(hash);
		}

		try {
			jmsConnection.close();
		} catch (Exception e) {
			log.warn("Exception occurred while closing jmsConnection. Ignore it. Msg={}", e.getMessage());
		}
		
		if (sessionsInfo.containsKey(hash)) {
			sessionsInfo.remove(hash);
		}
	}

	@Override
	public DestinationData discoverDestinations(Connection jmsConnection, boolean showSystemObjects) throws Exception {
		log.debug("discoverDestinations : {} - {}", jmsConnection, showSystemObjects);

		Integer hash = jmsConnection.hashCode();
		Context ctx = jndiContexts.get(hash);
		SessionInfo sessionInfo = sessionsInfo.get(hash);

		// Build Queues/Topics lists
		SortedSet<QueueData> listQueueData = new TreeSet<>();
		SortedSet<TopicData> listTopicData = new TreeSet<>();
		
		String queueJndiNames = sessionInfo.getQueueJndiNames();
		String topicJndiNames = sessionInfo.getTopicJndiNames();

		if (checkAvailable(queueJndiNames)) {
			lookupDestinations(queueJndiNames, ctx, listQueueData, listTopicData);
		} else {
			lookupQueues(sessionInfo, listQueueData);
		}
		lookupDestinations(topicJndiNames, ctx, listQueueData, listTopicData);

		return new DestinationData(listQueueData, listTopicData);
	}
	
	@Override
	public boolean manulAcknoledge() {
		return false;
	}
	
	@Override
	public int getWaitForFirstMsgInMills(SessionDef sessionDef) {
		try {
			// Extract properties
			Map<String, String> mapProperties = extractProperties(sessionDef);
			// The number of milliseconds to wait for first message before doing hasMoreElements check
			int waitForFirstMsg = Integer.parseInt(mapProperties.get(WAIT_FOR_FIRST_MSG));
			return waitForFirstMsg;
		} catch (Exception e) {
			return 0;
		}
	}
	

	// -------
	// Helpers
	// -------
	
	private boolean checkAvailable(String value) {
		return (value != null && value.length() > 0);
	}

	private void lookupDestinations(String jndiNames, Context ctx, SortedSet<QueueData> q, SortedSet<TopicData> t)
			throws NamingException {
		if (checkAvailable(jndiNames)) {
			for (String jndiName : jndiNames.split(",")) {
				Object o;
				String name = null;
				try {	
					name = jndiName.trim();
					o = ctx.lookup(name);
					if (o instanceof Queue) {
						log.debug("   It's a Queue");
						Queue oq = (Queue) o;
						q.add(new QueueData(oq.getQueueName()));
					}
					if (o instanceof Topic) {
						log.debug("   It's a Topic");
						Topic ot = (Topic) o;
						t.add(new TopicData(ot.getTopicName()));
					}
				} catch (Throwable e) {
					log.debug("   !!! Exception when processing jndiName '{}' : {}", name, e.getMessage());
					continue;
				}				
			}
		}
	}
	
	private void lookupQueues(SessionInfo sessionInfo, SortedSet<QueueData> q) throws Exception {
		// lookup first 100 queues under the message VPN using management interface
		try {
			initialize(sessionInfo.getMgmtUrl(), sessionInfo.getMgmtUsername(), sessionInfo.getMgmtPassword());
			if (checkAvailable(sessionInfo.getPhysicalQueueName())) {
				MsgVpnQueue queue = getQueue(sessionInfo.getMsgVpn(), sessionInfo.getPhysicalQueueName(), QUEUE_ATTRIBUTES);
				if (queue != null) {
					q.add(new QueueData(queue.getQueueName()));
				}
			} else {
				List<MsgVpnQueue> queues = getQueues(sessionInfo.getMsgVpn(), QUEUE_ATTRIBUTES);
				if (queues != null) {
					for (MsgVpnQueue queue : queues) {
						q.add(new QueueData(queue.getQueueName()));
					}
				}	
			}
		} catch (Exception e) {
			throw e;
		} catch (Throwable e) {
			log.debug("   !!! Exception when looking up queues using managment interface: " + e.getMessage());
		}		
	}
	
    private void handleError(ApiException ae) throws Exception {
        Gson gson = new Gson();
        String responseString = ae.getResponseBody();
        SempMetaOnlyResponse respObj = gson.fromJson(responseString, SempMetaOnlyResponse.class);
        SempError errorInfo = respObj.getMeta().getError();
        String errorMsg = "Error during operation. Details:" + 
                "\nHTTP Status Code: " + ae.getCode() + 
                "\nSEMP Error Code: " + errorInfo.getCode() + 
                "\nSEMP Error Status: " + errorInfo.getStatus() + 
                "\nSEMP Error Descriptions: " + errorInfo.getDescription();
        throw new Exception(errorMsg);
    }	

    private void initialize(String mgmtUrl, String mgmtUsername, String mgmtPassword) throws Exception {
        log.debug("SEMP initializing: %s, %s \n", mgmtUrl, mgmtUsername);
       
        ApiClient thisClient = new ApiClient();
        thisClient.setBasePath(thisClient.getBasePath().replace("http://www.solace.com", mgmtUrl));
        thisClient.setUsername(mgmtUsername);
        thisClient.setPassword(mgmtPassword);
        sempQueueApiInstance = new QueueApi(thisClient);
    }    
    
    private List<MsgVpnQueue> getQueues(String msgVpn, List<String> attributes) throws Exception {
    	try {
    		MsgVpnQueuesResponse queryResp = sempQueueApiInstance.getMsgVpnQueues(msgVpn, 100, null, null, null);
    		List<MsgVpnQueue> queuesList = queryResp.getData();
    		return queuesList;
    	} catch (ApiException ae) {
    		handleError(ae);
    	}
		return null;
    }
    
    private MsgVpnQueue getQueue(String msgVpn, String queueName, List<String> attributes) throws Exception {
    	try {
    		MsgVpnQueueResponse queryResp = sempQueueApiInstance.getMsgVpnQueue(msgVpn, queueName, null);
    		MsgVpnQueue queue = queryResp.getData();
    		return queue;
    	} catch (ApiException ae) {
    		handleError(ae);
    	}
		return null;  	
    }
    

	// ------------------------
	// Standard Getters/Setters
	// ------------------------

	@Override
	public List<QManagerProperty> getQManagerProperties() {
		return parameters;
	}
	

	@Override
	public Map<String, Object> getQueueInformation(Connection jmsConnection, String queueName) {

		LinkedHashMap<String, Object> properties = new LinkedHashMap<>();

		try {
		      SessionInfo sessionInfo = sessionsInfo.get(jmsConnection.hashCode());

		      if (sempQueueApiInstance == null) {
		         // No Management URL. No destination Info...
		         properties.put("Other", "n/a due to no Managment URL");
		      } else {
		    	  MsgVpnQueue queueInfo = getQueue(sessionInfo.getMsgVpn(), queueName, QUEUE_ATTRIBUTES);
		    	  if (queueInfo != null) {
		    		  properties.put("Incoming Enabled", queueInfo.getIngressEnabled());
		    		  properties.put("Outgoing Enabled", queueInfo.getEgressEnabled());
		    		  properties.put("Queue Quota", queueInfo.getMaxMsgSpoolUsage());
		    		  properties.put("Access Type", queueInfo.getAccessType());
		    		  properties.put("Consumer Limit", queueInfo.getMaxBindCount());
		    		  properties.put("Owner", queueInfo.getOwner());
		    		  properties.put("Other User Permission", queueInfo.getPermission());
		    	  }
		      }
		      
		} catch (Exception e) {
			log.error("Exception when reading Queue Information. Ignoring", e);
		}
		
		return properties;
	}

	private class SessionInfo {

		private String msgVpn;
		private String cfJndiName;
		private String queueJndiNames;
		private String topicJndiNames;
		private String mgmtUrl;
		private String mgmtUsername;
		private String mgmtPassword;
		private String physicalQueueName;
		
		public SessionInfo() {
			
		}

		public String getCfJndiName() {
			return cfJndiName;
		}

		public void setCfJndiName(String cfJndiName) {
			this.cfJndiName = cfJndiName;
		}

		public String getQueueJndiNames() {
			return queueJndiNames;
		}

		public void setQueueJndiNames(String queueJndiNames) {
			this.queueJndiNames = queueJndiNames;
		}

		public String getTopicJndiNames() {
			return topicJndiNames;
		}

		public void setTopicJndiNames(String topicJndiNames) {
			this.topicJndiNames = topicJndiNames;
		}

		public String getMgmtUrl() {
			return mgmtUrl;
		}

		public void setMgmtUrl(String mgmtUrl) {
			this.mgmtUrl = mgmtUrl;
		}

		public String getMgmtUsername() {
			return mgmtUsername;
		}

		public void setMgmtUsername(String mgmtUsername) {
			this.mgmtUsername = mgmtUsername;
		}

		public String getMgmtPassword() {
			return mgmtPassword;
		}

		public void setMgmtPassword(String mgmtPassword) {
			this.mgmtPassword = mgmtPassword;
		}

		public String getMsgVpn() {
			return msgVpn;
		}

		public void setMsgVpn(String msgVpn) {
			this.msgVpn = msgVpn;
		}

		public String getPhysicalQueueName() {
			return physicalQueueName;
		}

		public void setPhysicalQueueName(String physicalQueueName) {
			this.physicalQueueName = physicalQueueName;
		}
		
	}

}
