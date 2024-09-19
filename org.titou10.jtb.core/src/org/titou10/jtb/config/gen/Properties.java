
package org.titou10.jtb.config.gen;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import org.titou10.jtb.util.EncryptUtils;

/**
 * <p>
 * Java class for anonymous complex type
 * </p>
 * .
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * </p>
 * 
 * <pre>{@code
 * <complexType>
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="property" maxOccurs="unbounded">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 <attribute name="value" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 <attribute name="kind" type="{}propertyKind" />
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "property" })
@XmlRootElement(name = "properties")
public class Properties {

   @XmlElement(required = true)
   protected List<Properties.Property> property;

   /**
    * Gets the value of the property property.
    * 
    * <p>
    * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned
    * list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the property property.
    * </p>
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * </p>
    * 
    * <pre>
    * getProperty().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list {@link Properties.Property }
    * </p>
    * 
    * 
    * @return The value of the property property.
    */
   public List<Properties.Property> getProperty() {
      if (property == null) {
         property = new ArrayList<>();
      }
      return this.property;
   }

   /**
    * <p>
    * Java class for anonymous complex type
    * </p>
    * .
    * 
    * <p>
    * The following schema fragment specifies the expected content contained within this class.
    * </p>
    * 
    * <pre>{@code
    * <complexType>
    *   <complexContent>
    *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
    *       <attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
    *       <attribute name="value" type="{http://www.w3.org/2001/XMLSchema}string" />
    *       <attribute name="kind" type="{}propertyKind" />
    *     </restriction>
    *   </complexContent>
    * </complexType>
    * }</pre>
    * 
    * 
    */
   @XmlAccessorType(XmlAccessType.FIELD)
   @XmlType(name = "")
   public static class Property {

      @XmlAttribute(name = "name")
      protected String       name;
      @XmlAttribute(name = "value")
      protected String       value;
      @XmlAttribute(name = "kind")
      protected PropertyKind kind;

      public String getValue() {
         return EncryptUtils.decrypt(value);
      }

      public void setValue(String value, boolean encrypt) {
         if (encrypt) {
            this.value = EncryptUtils.encrypt(value);
         } else {
            this.value = value;
         }
      }

      /**
       * Gets the value of the name property.
       * 
       * @return possible object is {@link String }
       * 
       */
      public String getName() {
         return name;
      }

      /**
       * Sets the value of the name property.
       * 
       * @param value
       *           allowed object is {@link String }
       * 
       */
      public void setName(String value) {
         this.name = value;
      }

      // /**
      // * Gets the value of the value property.
      // *
      // * @return possible object is {@link String }
      // *
      // */
      // public String getValue() {
      // return value;
      // }

      /**
       * Sets the value of the value property.
       * 
       * @param value
       *           allowed object is {@link String }
       * 
       */
      public void setValue(String value) {
         this.value = value;
      }

      /**
       * Gets the value of the kind property.
       * 
       * @return possible object is {@link PropertyKind }
       * 
       */
      public PropertyKind getKind() {
         return kind;
      }

      /**
       * Sets the value of the kind property.
       * 
       * @param value
       *           allowed object is {@link PropertyKind }
       * 
       */
      public void setKind(PropertyKind value) {
         this.kind = value;
      }

   }

}
