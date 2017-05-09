//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.8-b130911.1802 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2017.05.09 à 03:37:23 PM EDT 
//


package org.titou10.jtb.script.gen;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour step complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="step">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="kind" type="{}stepKind"/>
 *         &lt;element name="templateName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="templateDirectory" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="sessionName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="destinationName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="variablePrefix" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="payloadDirectory" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "kind",
    "templateName",
    "templateDirectory",
    "sessionName",
    "destinationName",
    "variablePrefix",
    "payloadDirectory",
    "pauseSecsAfter",
    "iterations"
})
public class Step {

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected StepKind kind;
    @XmlElement(required = true)
    protected String templateName;
    @XmlElement(required = true)
    protected String templateDirectory;
    @XmlElement(required = true)
    protected String sessionName;
    @XmlElement(required = true)
    protected String destinationName;
    protected String variablePrefix;
    protected String payloadDirectory;
    protected Integer pauseSecsAfter;
    protected int iterations;

    /**
     * Obtient la valeur de la propriété kind.
     * 
     * @return
     *     possible object is
     *     {@link StepKind }
     *     
     */
    public StepKind getKind() {
        return kind;
    }

    /**
     * Définit la valeur de la propriété kind.
     * 
     * @param value
     *     allowed object is
     *     {@link StepKind }
     *     
     */
    public void setKind(StepKind value) {
        this.kind = value;
    }

    /**
     * Obtient la valeur de la propriété templateName.
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
     * Définit la valeur de la propriété templateName.
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
     * Obtient la valeur de la propriété templateDirectory.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTemplateDirectory() {
        return templateDirectory;
    }

    /**
     * Définit la valeur de la propriété templateDirectory.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTemplateDirectory(String value) {
        this.templateDirectory = value;
    }

    /**
     * Obtient la valeur de la propriété sessionName.
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
     * Définit la valeur de la propriété sessionName.
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
     * Obtient la valeur de la propriété destinationName.
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
     * Définit la valeur de la propriété destinationName.
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
     * Obtient la valeur de la propriété variablePrefix.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVariablePrefix() {
        return variablePrefix;
    }

    /**
     * Définit la valeur de la propriété variablePrefix.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVariablePrefix(String value) {
        this.variablePrefix = value;
    }

    /**
     * Obtient la valeur de la propriété payloadDirectory.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPayloadDirectory() {
        return payloadDirectory;
    }

    /**
     * Définit la valeur de la propriété payloadDirectory.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPayloadDirectory(String value) {
        this.payloadDirectory = value;
    }

    /**
     * Obtient la valeur de la propriété pauseSecsAfter.
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
     * Définit la valeur de la propriété pauseSecsAfter.
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
     * Obtient la valeur de la propriété iterations.
     * 
     */
    public int getIterations() {
        return iterations;
    }

    /**
     * Définit la valeur de la propriété iterations.
     * 
     */
    public void setIterations(int value) {
        this.iterations = value;
    }

}
