//
// Ce fichier a été généré par Java Architecture for XML Binding (JAXB) Reference Implementation, v2.2.8-b130911.1802 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Les modifications apportées à ce fichier seront perdues lors de la recompilation du schéma source. 
// Généré sur : 2017.04.12 le 09:31:50 AM EDT 
//


package org.titou10.jtb.visualizer.gen;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour visualizer complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="visualizer">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="sourceKind" type="{}visualizerSourceKind"/>
 *         &lt;element name="messageType" type="{}visualizerMessageType"/>
 *         &lt;choice>
 *           &lt;element name="extension" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *           &lt;element name="source" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *           &lt;element name="fileName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="language" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="system" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "visualizer", propOrder = {
    "sourceKind",
    "messageType",
    "extension",
    "source",
    "fileName"
})
public class Visualizer {

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected VisualizerSourceKind sourceKind;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected VisualizerMessageType messageType;
    protected String extension;
    protected String source;
    protected String fileName;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "language")
    protected String language;
    @XmlAttribute(name = "system")
    protected Boolean system;

    /**
     * Obtient la valeur de la propriété sourceKind.
     * 
     * @return
     *     possible object is
     *     {@link VisualizerSourceKind }
     *     
     */
    public VisualizerSourceKind getSourceKind() {
        return sourceKind;
    }

    /**
     * Définit la valeur de la propriété sourceKind.
     * 
     * @param value
     *     allowed object is
     *     {@link VisualizerSourceKind }
     *     
     */
    public void setSourceKind(VisualizerSourceKind value) {
        this.sourceKind = value;
    }

    /**
     * Obtient la valeur de la propriété messageType.
     * 
     * @return
     *     possible object is
     *     {@link VisualizerMessageType }
     *     
     */
    public VisualizerMessageType getMessageType() {
        return messageType;
    }

    /**
     * Définit la valeur de la propriété messageType.
     * 
     * @param value
     *     allowed object is
     *     {@link VisualizerMessageType }
     *     
     */
    public void setMessageType(VisualizerMessageType value) {
        this.messageType = value;
    }

    /**
     * Obtient la valeur de la propriété extension.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Définit la valeur de la propriété extension.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExtension(String value) {
        this.extension = value;
    }

    /**
     * Obtient la valeur de la propriété source.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSource() {
        return source;
    }

    /**
     * Définit la valeur de la propriété source.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSource(String value) {
        this.source = value;
    }

    /**
     * Obtient la valeur de la propriété fileName.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Définit la valeur de la propriété fileName.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFileName(String value) {
        this.fileName = value;
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
     * Obtient la valeur de la propriété language.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Définit la valeur de la propriété language.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLanguage(String value) {
        this.language = value;
    }

    /**
     * Obtient la valeur de la propriété system.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSystem() {
        return system;
    }

    /**
     * Définit la valeur de la propriété system.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSystem(Boolean value) {
        this.system = value;
    }

}
