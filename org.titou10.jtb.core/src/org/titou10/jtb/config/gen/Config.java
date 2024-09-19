
package org.titou10.jtb.config.gen;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType>
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element ref="{}qManagerDef" maxOccurs="unbounded"/>
 *         <element ref="{}sessionDef" maxOccurs="unbounded"/>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "qManagerDef",
    "sessionDef"
})
@XmlRootElement(name = "config")
public class Config {

    @XmlElement(required = true)
    protected List<QManagerDef> qManagerDef;
    @XmlElement(required = true)
    protected List<SessionDef> sessionDef;

    /**
     * Gets the value of the qManagerDef property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the qManagerDef property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getQManagerDef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link QManagerDef }
     * </p>
     * 
     * 
     * @return
     *     The value of the qManagerDef property.
     */
    public List<QManagerDef> getQManagerDef() {
        if (qManagerDef == null) {
            qManagerDef = new ArrayList<>();
        }
        return this.qManagerDef;
    }

    /**
     * Gets the value of the sessionDef property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sessionDef property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getSessionDef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SessionDef }
     * </p>
     * 
     * 
     * @return
     *     The value of the sessionDef property.
     */
    public List<SessionDef> getSessionDef() {
        if (sessionDef == null) {
            sessionDef = new ArrayList<>();
        }
        return this.sessionDef;
    }

}
