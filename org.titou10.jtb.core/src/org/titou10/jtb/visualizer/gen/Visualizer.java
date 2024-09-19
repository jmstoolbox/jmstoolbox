
package org.titou10.jtb.visualizer.gen;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour visualizer complex type.</p>
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.</p>
 * 
 * <pre>{@code
 * <complexType name="visualizer">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="kind" type="{}visualizerKind"/>
 *         <element name="targetMsgType" type="{}visualizerMessageType" maxOccurs="unbounded"/>
 *         <element name="language" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="showScriptLogs" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         <choice>
 *           <element name="extension" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *           <element name="source" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *           <element name="fileName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         </choice>
 *       </sequence>
 *       <attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="system" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "visualizer", propOrder = {
    "kind",
    "targetMsgType",
    "language",
    "showScriptLogs",
    "extension",
    "source",
    "fileName"
})
public class Visualizer {

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected VisualizerKind kind;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected List<VisualizerMessageType> targetMsgType;
    protected String language;
    protected boolean showScriptLogs;
    protected String extension;
    protected String source;
    protected String fileName;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "system")
    protected Boolean system;

    /**
     * Obtient la valeur de la propriété kind.
     * 
     * @return
     *     possible object is
     *     {@link VisualizerKind }
     *     
     */
    public VisualizerKind getKind() {
        return kind;
    }

    /**
     * Définit la valeur de la propriété kind.
     * 
     * @param value
     *     allowed object is
     *     {@link VisualizerKind }
     *     
     */
    public void setKind(VisualizerKind value) {
        this.kind = value;
    }

    /**
     * Gets the value of the targetMsgType property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the targetMsgType property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getTargetMsgType().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link VisualizerMessageType }
     * </p>
     * 
     * 
     * @return
     *     The value of the targetMsgType property.
     */
    public List<VisualizerMessageType> getTargetMsgType() {
        if (targetMsgType == null) {
            targetMsgType = new ArrayList<>();
        }
        return this.targetMsgType;
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
     * Obtient la valeur de la propriété showScriptLogs.
     * 
     */
    public boolean isShowScriptLogs() {
        return showScriptLogs;
    }

    /**
     * Définit la valeur de la propriété showScriptLogs.
     * 
     */
    public void setShowScriptLogs(boolean value) {
        this.showScriptLogs = value;
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
