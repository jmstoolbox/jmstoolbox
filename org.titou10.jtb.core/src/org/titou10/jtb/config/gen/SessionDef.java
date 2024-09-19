
package org.titou10.jtb.config.gen;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.titou10.jtb.util.jaxb.EncryptedStringXmlAdapter;

/**
 * <p>
 * Java class for anonymous complex type
 * </p>
 * .
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * </p>
 * 
 * <pre>{@code
 * <complexType>
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="host" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         <element name="port" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         <element name="host2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="port2" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         <element name="host3" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="port3" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         <element name="userid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="password" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="promptForCredentials" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         <element name="destinationFilter" type="{}destinationFilter" minOccurs="0"/>
 *         <element name="columnsSetName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element ref="{}properties"/>
 *       </sequence>
 *       <attribute name="qManagerDef" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="folder" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="sessionType" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "",
         propOrder = { "host", "port", "host2", "port2", "host3", "port3", "userid", "password", "promptForCredentials",
                       "destinationFilter", "columnsSetName", "properties" })
@XmlRootElement(name = "sessionDef")
public class SessionDef {

   @XmlElement(required = true)
   protected String            host;
   protected int               port;
   protected String            host2;
   protected Integer           port2;
   protected String            host3;
   protected Integer           port3;
   protected String            userid;
   protected Boolean           promptForCredentials;
   protected DestinationFilter destinationFilter;
   protected String            columnsSetName;
   @XmlElement(required = true)
   protected Properties        properties;
   @XmlAttribute(name = "qManagerDef")
   protected String            qManagerDef;
   @XmlAttribute(name = "name")
   protected String            name;
   @XmlAttribute(name = "folder")
   protected String            folder;
   @XmlAttribute(name = "sessionType")
   protected String            sessionType;

   @XmlJavaTypeAdapter(EncryptedStringXmlAdapter.class)
   protected String            password;
   @XmlTransient
   private String              activeUserid;
   @XmlTransient
   private String              activePassword;

   public String getActiveUserid() {
      return activeUserid;
   }

   public void setActiveUserid(String activeUserid) {
      this.activeUserid = activeUserid;
   }

   public String getActivePassword() {
      return activePassword;
   }

   public void setActivePassword(String activePassword) {
      this.activePassword = activePassword;
   }

   void afterUnmarshal(Unmarshaller u, Object parent) {
      this.activeUserid = this.userid;
      this.activePassword = this.password;
   }

   /**
    * Gets the value of the host property.
    * 
    * @return possible object is {@link String }
    * 
    */
   public String getHost() {
      return host;
   }

   /**
    * Sets the value of the host property.
    * 
    * @param value
    *           allowed object is {@link String }
    * 
    */
   public void setHost(String value) {
      this.host = value;
   }

   /**
    * Gets the value of the port property.
    * 
    */
   public int getPort() {
      return port;
   }

   /**
    * Sets the value of the port property.
    * 
    */
   public void setPort(int value) {
      this.port = value;
   }

   /**
    * Gets the value of the host2 property.
    * 
    * @return possible object is {@link String }
    * 
    */
   public String getHost2() {
      return host2;
   }

   /**
    * Sets the value of the host2 property.
    * 
    * @param value
    *           allowed object is {@link String }
    * 
    */
   public void setHost2(String value) {
      this.host2 = value;
   }

   /**
    * Gets the value of the port2 property.
    * 
    * @return possible object is {@link Integer }
    * 
    */
   public Integer getPort2() {
      return port2;
   }

   /**
    * Sets the value of the port2 property.
    * 
    * @param value
    *           allowed object is {@link Integer }
    * 
    */
   public void setPort2(Integer value) {
      this.port2 = value;
   }

   /**
    * Gets the value of the host3 property.
    * 
    * @return possible object is {@link String }
    * 
    */
   public String getHost3() {
      return host3;
   }

   /**
    * Sets the value of the host3 property.
    * 
    * @param value
    *           allowed object is {@link String }
    * 
    */
   public void setHost3(String value) {
      this.host3 = value;
   }

   /**
    * Gets the value of the port3 property.
    * 
    * @return possible object is {@link Integer }
    * 
    */
   public Integer getPort3() {
      return port3;
   }

   /**
    * Sets the value of the port3 property.
    * 
    * @param value
    *           allowed object is {@link Integer }
    * 
    */
   public void setPort3(Integer value) {
      this.port3 = value;
   }

   /**
    * Gets the value of the userid property.
    * 
    * @return possible object is {@link String }
    * 
    */
   public String getUserid() {
      return userid;
   }

   /**
    * Sets the value of the userid property.
    * 
    * @param value
    *           allowed object is {@link String }
    * 
    */
   public void setUserid(String value) {
      this.userid = value;
   }

   /**
    * Gets the value of the password property.
    * 
    * @return possible object is {@link String }
    * 
    */
   public String getPassword() {
      return password;
   }

   /**
    * Sets the value of the password property.
    * 
    * @param value
    *           allowed object is {@link String }
    * 
    */
   public void setPassword(String value) {
      this.password = value;
   }

   /**
    * Gets the value of the promptForCredentials property.
    * 
    * @return possible object is {@link Boolean }
    * 
    */
   public Boolean isPromptForCredentials() {
      return promptForCredentials;
   }

   /**
    * Sets the value of the promptForCredentials property.
    * 
    * @param value
    *           allowed object is {@link Boolean }
    * 
    */
   public void setPromptForCredentials(Boolean value) {
      this.promptForCredentials = value;
   }

   /**
    * Gets the value of the destinationFilter property.
    * 
    * @return possible object is {@link DestinationFilter }
    * 
    */
   public DestinationFilter getDestinationFilter() {
      return destinationFilter;
   }

   /**
    * Sets the value of the destinationFilter property.
    * 
    * @param value
    *           allowed object is {@link DestinationFilter }
    * 
    */
   public void setDestinationFilter(DestinationFilter value) {
      this.destinationFilter = value;
   }

   /**
    * Gets the value of the columnsSetName property.
    * 
    * @return possible object is {@link String }
    * 
    */
   public String getColumnsSetName() {
      return columnsSetName;
   }

   /**
    * Sets the value of the columnsSetName property.
    * 
    * @param value
    *           allowed object is {@link String }
    * 
    */
   public void setColumnsSetName(String value) {
      this.columnsSetName = value;
   }

   /**
    * Gets the value of the properties property.
    * 
    * @return possible object is {@link Properties }
    * 
    */
   public Properties getProperties() {
      return properties;
   }

   /**
    * Sets the value of the properties property.
    * 
    * @param value
    *           allowed object is {@link Properties }
    * 
    */
   public void setProperties(Properties value) {
      this.properties = value;
   }

   /**
    * Gets the value of the qManagerDef property.
    * 
    * @return possible object is {@link String }
    * 
    */
   public String getQManagerDef() {
      return qManagerDef;
   }

   /**
    * Sets the value of the qManagerDef property.
    * 
    * @param value
    *           allowed object is {@link String }
    * 
    */
   public void setQManagerDef(String value) {
      this.qManagerDef = value;
   }

   /**
    * Gets the value of the name property.
    * 
    * @return possible object is {@link String }
    * 
    */
   public String getName() {
      return name;
   }

   /**
    * Sets the value of the name property.
    * 
    * @param value
    *           allowed object is {@link String }
    * 
    */
   public void setName(String value) {
      this.name = value;
   }

   /**
    * Gets the value of the folder property.
    * 
    * @return possible object is {@link String }
    * 
    */
   public String getFolder() {
      return folder;
   }

   /**
    * Sets the value of the folder property.
    * 
    * @param value
    *           allowed object is {@link String }
    * 
    */
   public void setFolder(String value) {
      this.folder = value;
   }

   /**
    * Gets the value of the sessionType property.
    * 
    * @return possible object is {@link String }
    * 
    */
   public String getSessionType() {
      return sessionType;
   }

   /**
    * Sets the value of the sessionType property.
    * 
    * @param value
    *           allowed object is {@link String }
    * 
    */
   public void setSessionType(String value) {
      this.sessionType = value;
   }

}
