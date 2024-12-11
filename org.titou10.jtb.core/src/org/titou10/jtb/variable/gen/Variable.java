
package org.titou10.jtb.variable.gen;

import java.util.ArrayList;
import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for variable complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="variable">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="min" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         <element name="max" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         <element name="dateTimeMin" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         <element name="dateTimeMax" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         <element name="dateTimePattern" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="dateTimeKind" type="{}variableDateTimeKind" minOccurs="0"/>
 *         <element name="dateTimeOffset" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         <element name="dateTimeOffsetTU" type="{}variableDateTimeOffsetTU" minOccurs="0"/>
 *         <element name="stringLength" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         <element name="stringKind" type="{}variableStringKind" minOccurs="0"/>
 *         <element name="stringChars" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         <element name="listValue" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="kind" type="{}variableKind" />
 *       <attribute name="system" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
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
    "dateTimeOffset",
    "dateTimeOffsetTU",
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
    protected Integer dateTimeOffset;
    @XmlSchemaType(name = "string")
    protected VariableDateTimeOffsetTU dateTimeOffsetTU;
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
     * Gets the value of the min property.
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
     * Sets the value of the min property.
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
     * Gets the value of the max property.
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
     * Sets the value of the max property.
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
     * Gets the value of the dateTimeMin property.
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
     * Sets the value of the dateTimeMin property.
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
     * Gets the value of the dateTimeMax property.
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
     * Sets the value of the dateTimeMax property.
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
     * Gets the value of the dateTimePattern property.
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
     * Sets the value of the dateTimePattern property.
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
     * Gets the value of the dateTimeKind property.
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
     * Sets the value of the dateTimeKind property.
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
     * Gets the value of the dateTimeOffset property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getDateTimeOffset() {
        return dateTimeOffset;
    }

    /**
     * Sets the value of the dateTimeOffset property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setDateTimeOffset(Integer value) {
        this.dateTimeOffset = value;
    }

    /**
     * Gets the value of the dateTimeOffsetTU property.
     * 
     * @return
     *     possible object is
     *     {@link VariableDateTimeOffsetTU }
     *     
     */
    public VariableDateTimeOffsetTU getDateTimeOffsetTU() {
        return dateTimeOffsetTU;
    }

    /**
     * Sets the value of the dateTimeOffsetTU property.
     * 
     * @param value
     *     allowed object is
     *     {@link VariableDateTimeOffsetTU }
     *     
     */
    public void setDateTimeOffsetTU(VariableDateTimeOffsetTU value) {
        this.dateTimeOffsetTU = value;
    }

    /**
     * Gets the value of the stringLength property.
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
     * Sets the value of the stringLength property.
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
     * Gets the value of the stringKind property.
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
     * Sets the value of the stringKind property.
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
     * Gets the value of the stringChars property.
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
     * Sets the value of the stringChars property.
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
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the listValue property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getListValue().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * </p>
     * 
     * 
     * @return
     *     The value of the listValue property.
     */
    public List<String> getListValue() {
        if (listValue == null) {
            listValue = new ArrayList<>();
        }
        return this.listValue;
    }

    /**
     * Gets the value of the name property.
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
     * Sets the value of the name property.
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
     * Gets the value of the kind property.
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
     * Sets the value of the kind property.
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
     * Gets the value of the system property.
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
     * Sets the value of the system property.
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
