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
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour dataFile complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="dataFile">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="variablePrefix" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="delimiter" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="variableNames" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="fileName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="scriptLevel" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="charset" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dataFile", propOrder = {
    "variablePrefix",
    "delimiter",
    "variableNames",
    "fileName",
    "scriptLevel",
    "charset"
})
public class DataFile {

    @XmlElement(required = true)
    protected String variablePrefix;
    @XmlElement(required = true)
    protected String delimiter;
    @XmlElement(required = true)
    protected String variableNames;
    @XmlElement(required = true)
    protected String fileName;
    protected boolean scriptLevel;
    protected String charset;

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
     * Obtient la valeur de la propriété delimiter.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDelimiter() {
        return delimiter;
    }

    /**
     * Définit la valeur de la propriété delimiter.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDelimiter(String value) {
        this.delimiter = value;
    }

    /**
     * Obtient la valeur de la propriété variableNames.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVariableNames() {
        return variableNames;
    }

    /**
     * Définit la valeur de la propriété variableNames.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVariableNames(String value) {
        this.variableNames = value;
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
     * Obtient la valeur de la propriété scriptLevel.
     * 
     */
    public boolean isScriptLevel() {
        return scriptLevel;
    }

    /**
     * Définit la valeur de la propriété scriptLevel.
     * 
     */
    public void setScriptLevel(boolean value) {
        this.scriptLevel = value;
    }

    /**
     * Obtient la valeur de la propriété charset.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCharset() {
        return charset;
    }

    /**
     * Définit la valeur de la propriété charset.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCharset(String value) {
        this.charset = value;
    }

}
