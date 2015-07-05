
package org.titou10.jtb.variable.gen;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Classe Java pour variable complex type.
 * 
 * <p>Le fragment de sch�ma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="variable">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="min" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="max" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="dateTimeMin" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="dateTimeMax" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="dateTimePattern" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dateTimeKind" type="{}variableDateTimeKind" minOccurs="0"/>
 *         &lt;element name="stringLength" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="stringKind" type="{}variableStringKind" minOccurs="0"/>
 *         &lt;element name="stringChars" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="listValue" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="kind" type="{}variableKind" />
 *       &lt;attribute name="system" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "variable", propOrder = {
    "min",
    "max",
    "dateTimeMin",
    "dateTimeMax",
    "dateTimePattern",
    "dateTimeKind",
    "stringLength",
    "stringKind",
    "stringChars",
    "listValue"
})
public class Variable {

    protected Integer min;
    protected Integer max;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dateTimeMin;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar dateTimeMax;
    protected String dateTimePattern;
    @XmlSchemaType(name = "string")
    protected VariableDateTimeKind dateTimeKind;
    protected Integer stringLength;
    @XmlSchemaType(name = "string")
    protected VariableStringKind stringKind;
    protected String stringChars;
    protected List<String> listValue;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "kind")
    protected VariableKind kind;
    @XmlAttribute(name = "system")
    protected Boolean system;

    /**
     * Obtient la valeur de la propri�t� min.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMin() {
        return min;
    }

    /**
     * D�finit la valeur de la propri�t� min.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMin(Integer value) {
        this.min = value;
    }

    /**
     * Obtient la valeur de la propri�t� max.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMax() {
        return max;
    }

    /**
     * D�finit la valeur de la propri�t� max.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMax(Integer value) {
        this.max = value;
    }

    /**
     * Obtient la valeur de la propri�t� dateTimeMin.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateTimeMin() {
        return dateTimeMin;
    }

    /**
     * D�finit la valeur de la propri�t� dateTimeMin.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateTimeMin(XMLGregorianCalendar value) {
        this.dateTimeMin = value;
    }

    /**
     * Obtient la valeur de la propri�t� dateTimeMax.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDateTimeMax() {
        return dateTimeMax;
    }

    /**
     * D�finit la valeur de la propri�t� dateTimeMax.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDateTimeMax(XMLGregorianCalendar value) {
        this.dateTimeMax = value;
    }

    /**
     * Obtient la valeur de la propri�t� dateTimePattern.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDateTimePattern() {
        return dateTimePattern;
    }

    /**
     * D�finit la valeur de la propri�t� dateTimePattern.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDateTimePattern(String value) {
        this.dateTimePattern = value;
    }

    /**
     * Obtient la valeur de la propri�t� dateTimeKind.
     * 
     * @return
     *     possible object is
     *     {@link VariableDateTimeKind }
     *     
     */
    public VariableDateTimeKind getDateTimeKind() {
        return dateTimeKind;
    }

    /**
     * D�finit la valeur de la propri�t� dateTimeKind.
     * 
     * @param value
     *     allowed object is
     *     {@link VariableDateTimeKind }
     *     
     */
    public void setDateTimeKind(VariableDateTimeKind value) {
        this.dateTimeKind = value;
    }

    /**
     * Obtient la valeur de la propri�t� stringLength.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getStringLength() {
        return stringLength;
    }

    /**
     * D�finit la valeur de la propri�t� stringLength.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setStringLength(Integer value) {
        this.stringLength = value;
    }

    /**
     * Obtient la valeur de la propri�t� stringKind.
     * 
     * @return
     *     possible object is
     *     {@link VariableStringKind }
     *     
     */
    public VariableStringKind getStringKind() {
        return stringKind;
    }

    /**
     * D�finit la valeur de la propri�t� stringKind.
     * 
     * @param value
     *     allowed object is
     *     {@link VariableStringKind }
     *     
     */
    public void setStringKind(VariableStringKind value) {
        this.stringKind = value;
    }

    /**
     * Obtient la valeur de la propri�t� stringChars.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStringChars() {
        return stringChars;
    }

    /**
     * D�finit la valeur de la propri�t� stringChars.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStringChars(String value) {
        this.stringChars = value;
    }

    /**
     * Gets the value of the listValue property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the listValue property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getListValue().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getListValue() {
        if (listValue == null) {
            listValue = new ArrayList<String>();
        }
        return this.listValue;
    }

    /**
     * Obtient la valeur de la propri�t� name.
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
     * D�finit la valeur de la propri�t� name.
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
     * Obtient la valeur de la propri�t� kind.
     * 
     * @return
     *     possible object is
     *     {@link VariableKind }
     *     
     */
    public VariableKind getKind() {
        return kind;
    }

    /**
     * D�finit la valeur de la propri�t� kind.
     * 
     * @param value
     *     allowed object is
     *     {@link VariableKind }
     *     
     */
    public void setKind(VariableKind value) {
        this.kind = value;
    }

    /**
     * Obtient la valeur de la propri�t� system.
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
     * D�finit la valeur de la propri�t� system.
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
