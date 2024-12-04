package org.titou10.jtb.soapui;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import com.eviware.soapui.impl.wsdl.submit.RequestFilter;
import com.eviware.soapui.impl.wsdl.submit.RequestTransport;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.JMSResponse;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.plugins.auto.PluginRequestTransport;

@PluginRequestTransport(protocol = "jtb")
public class PluginTransport implements RequestTransport {

   public PluginTransport() {
      System.out.println(">>>>>>>> a");
   }

   @Override
   public void abortRequest(SubmitContext arg0) {
      System.out.println(">>>>>>>>>> b");
   }

   @Override
   public void addRequestFilter(RequestFilter arg0) {
      System.out.println(">>>>>>>>> c");
   }

   @Override
   public void insertRequestFilter(RequestFilter arg0, RequestFilter arg1) {
      System.out.println(">>>>>>>>>> d");
   }

   @Override
   public void removeRequestFilter(RequestFilter arg0) {
      System.out.println(">>>>>>>>> e");
   }

   @Override
   public Response sendRequest(SubmitContext arg0, Request request) throws Exception {

      System.out.println("f arg0=" + arg0 + " request=" + request);
      System.out.println("property n=" + arg0.getPropertyNames());
      System.out.println("mopdel ite=" + arg0.getModelItem());
      System.out.println("endpoint  =" + request.getEndpoint());
      System.out.println("operation =" + request.getOperation());
      System.out.println("rq context=" + request.getRequestContent());
      System.out.println("rr1=" + request.getRequestParts());
      System.out.println("rr2=" + request.getResponseParts());

      // UISupport.showInfoMessage("coucou");

      String url = request.getEndpoint().replaceFirst("jtb:", "http:");
      String content = buildContent(request.getRequestContent());

      System.out.println("url=" + url);
      System.out.println("content=" + content);

      String response = doPost(url, content);

      System.out.println("response=" + response);

      Response r = new JMSResponse(response, null, null, request, 10);

      return r;
   }

   // -------
   // Helpers
   // -------
   private String doPost(String targetURL, String content) throws Exception {

      HttpURLConnection connection = null;
      try {
         // URL url = new URL(targetURL);
         URL url = URI.create(targetURL).toURL();
         // Create connection
         connection = (HttpURLConnection) url.openConnection();
         connection.setRequestMethod("POST");
         connection.setRequestProperty("Content-Type", "application/json");
         connection.setRequestProperty("Accept", "application/json");
         connection.setRequestProperty("Content-Length", String.valueOf(content.length()));

         connection.setUseCaches(false);
         connection.setDoOutput(true);

         // Send request
         try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
            wr.writeBytes(content);
         }

         // Get Response
         BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
         StringBuffer response = new StringBuffer(1024);
         String line;
         while ((line = rd.readLine()) != null) {
            response.append(line);
         }
         rd.close();
         return response.toString();
      } finally {
         if (connection != null) {
            connection.disconnect();
         }
      }
   }

   private String buildContent(String content) {
      StringBuilder sb = new StringBuilder(256 + content.length());
      sb.append("{");
      // sb.append("\"deliveryMode\":\"PERSISTENT\",");
      // "priority":<[0-9]>,
      // "deliveryDelay":<n>,
      // "timeToLive":<n>,
      // "jmsCorrelationID":"",
      // "jmsType":"",
      sb.append("\"type\":\"TEXT\",");
      sb.append("\"payloadText\":").append(quote(content));
      // "payloadMap": {"p1":"","p2":""},
      // "properties":{"p1":""}
      sb.append("}");

      return sb.toString();
   }

   // from jettyson
   // http://grepcode.com/file/repo1.maven.org/maven2/org.codehaus.jettison/jettison/1.3.3/org/codehaus/jettison/json/JSONObject.java#JSONObject.quote%28java.lang.String%29
   private String quote(String string) {
      if (string == null || string.length() == 0) {
         return "\"\"";
      }

      char c = 0;
      int i;
      int len = string.length();
      // StringBuilder sb = new StringBuilder(len + 4);
      StringBuilder sb = new StringBuilder(len + 32);
      String t;

      sb.append('"');
      for (i = 0; i < len; i += 1) {
         c = string.charAt(i);
         switch (c) {
            case '\\':
            case '"':
               sb.append('\\');
               sb.append(c);
               break;
            case '/':
               // if (b == '<') {
               sb.append('\\');
               // }
               sb.append(c);
               break;
            case '\b':
               sb.append("\\b");
               break;
            case '\t':
               sb.append("\\t");
               break;
            case '\n':
               sb.append("\\n");
               break;
            case '\f':
               sb.append("\\f");
               break;
            case '\r':
               sb.append("\\r");
               break;
            default:
               if (c < ' ') {
                  t = "000" + Integer.toHexString(c);
                  sb.append("\\u" + t.substring(t.length() - 4));
               } else {
                  sb.append(c);
               }
         }
      }
      sb.append('"');
      return sb.toString();
   }

}
