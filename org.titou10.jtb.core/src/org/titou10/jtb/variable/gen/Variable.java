
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
 * <p>Classe Java pour variable complex type.</p>
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.</p>
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
     * Obtient la valeur de la propriété min.
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
     * Définit la valeur de la propriété min.
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
     * Obtient la valeur de la propriété max.
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
     * Définit la valeur de la propriété max.
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
     * Obtient la valeur de la propriété dateTimeMin.
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
     * Définit la valeur de la propriété dateTimeMin.
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
     * Obtient la valeur de la propriété dateTimeMax.
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
     * Définit la valeur de la propriété dateTimeMax.
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
     * Obtient la valeur de la propriété dateTimePattern.
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
     * Définit la valeur de la propriété dateTimePattern.
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
     * Obtient la valeur de la propriété dateTimeKind.
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
     * Définit la valeur de la propriété dateTimeKind.
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
     * Obtient la valeur de la propriété dateTimeOffset.
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
     * Définit la valeur de la propriété dateTimeOffset.
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
     * Obtient la valeur de la propriété dateTimeOffsetTU.
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
     * Définit la valeur de la propriété dateTimeOffsetTU.
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
     * Obtient la valeur de la propriété stringLength.
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
     * Définit la valeur de la propriété stringLength.
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
     * Obtient la valeur de la propriété stringKind.
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
     * Définit la valeur de la propriété stringKind.
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
     * Obtient la valeur de la propriété stringChars.
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
     * Définit la valeur de la propriété stringChars.
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
     * Obtient la valeur de la propriété kind.
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
     * Définit la valeur de la propriété kind.
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
