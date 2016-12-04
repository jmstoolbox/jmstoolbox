
package org.titou10.jtb.config.gen;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour anonymous complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="host" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="port" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="userid" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="password" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element ref="{}properties"/>
 *       &lt;/sequence>
 *       &lt;attribute name="qManagerDef" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="transacted" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "host",
    "port",
    "userid",
    "password",
    "properties"
})
@XmlRootElement(name = "sessionDef")
public class SessionDef {

    @XmlElement(required = true)
    protected String host;
    protected int port;
    @XmlElement(required = true)
    protected String userid;
    @XmlElement(required = true)
    protected String password;
    @XmlElement(required = true)
    protected Properties properties;
    @XmlAttribute(name = "qManagerDef")
    protected String qManagerDef;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "transacted")
    protected Boolean transacted;

    /**
     * Obtient la valeur de la propriété host.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHost() {
        return host;
    }

    /**
     * Définit la valeur de la propriété host.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHost(String value) {
        this.host = value;
    }

    /**
     * Obtient la valeur de la propriété port.
     * 
     */
    public int getPort() {
        return port;
    }

    /**
     * Définit la valeur de la propriété port.
     * 
     */
    public void setPort(int value) {
        this.port = value;
    }

    /**
     * Obtient la valeur de la propriété userid.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserid() {
        return userid;
    }

    /**
     * Définit la valeur de la propriété userid.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserid(String value) {
        this.userid = value;
    }

    /**
     * Obtient la valeur de la propriété password.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPassword() {
        return password;
    }

    /**
     * Définit la valeur de la propriété password.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPassword(String value) {
        this.password = value;
    }

    /**
     * Obtient la valeur de la propriété properties.
     * 
     * @return
     *     possible object is
     *     {@link Properties }
     *     
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Définit la valeur de la propriété properties.
     * 
     * @param value
     *     allowed object is
     *     {@link Properties }
     *     
     */
    public void setProperties(Properties value) {
        this.properties = value;
    }

    /**
     * Obtient la valeur de la propriété qManagerDef.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQManagerDef() {
        return qManagerDef;
    }

    /**
     * Définit la valeur de la propriété qManagerDef.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQManagerDef(String value) {
        this.qManagerDef = value;
    }

    /**
     * Obtient la valeur de la propriété name.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Définit la valeur de la propriété name.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Obtient la valeur de la propriété transacted.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isTransacted() {
        return transacted;
    }

    /**
     * Définit la valeur de la propriété transacted.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setTransacted(Boolean value) {
        this.transacted = value;
    }

}
