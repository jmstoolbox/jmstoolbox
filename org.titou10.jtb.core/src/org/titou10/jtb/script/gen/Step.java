
package org.titou10.jtb.script.gen;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour step complex type.
 * 
 * <p>Le fragment de sch�ma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="step">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="templateName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="sessionName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="destinationName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="pauseSecsAfter" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="iterations" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "step", propOrder = {
    "templateName",
    "sessionName",
    "destinationName",
    "pauseSecsAfter",
    "iterations"
})
public class Step {

    @XmlElement(required = true)
    protected String templateName;
    @XmlElement(required = true)
    protected String sessionName;
    @XmlElement(required = true)
    protected String destinationName;
    protected Integer pauseSecsAfter;
    protected int iterations;

    /**
     * Obtient la valeur de la propri�t� templateName.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTemplateName() {
        return templateName;
    }

    /**
     * D�finit la valeur de la propri�t� templateName.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTemplateName(String value) {
        this.templateName = value;
    }

    /**
     * Obtient la valeur de la propri�t� sessionName.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSessionName() {
        return sessionName;
    }

    /**
     * D�finit la valeur de la propri�t� sessionName.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSessionName(String value) {
        this.sessionName = value;
    }

    /**
     * Obtient la valeur de la propri�t� destinationName.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDestinationName() {
        return destinationName;
    }

    /**
     * D�finit la valeur de la propri�t� destinationName.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDestinationName(String value) {
        this.destinationName = value;
    }

    /**
     * Obtient la valeur de la propri�t� pauseSecsAfter.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPauseSecsAfter() {
        return pauseSecsAfter;
    }

    /**
     * D�finit la valeur de la propri�t� pauseSecsAfter.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPauseSecsAfter(Integer value) {
        this.pauseSecsAfter = value;
    }

    /**
     * Obtient la valeur de la propri�t� iterations.
     * 
     */
    public int getIterations() {
        return iterations;
    }

    /**
     * D�finit la valeur de la propri�t� iterations.
     * 
     */
    public void setIterations(int value) {
        this.iterations = value;
    }

}
