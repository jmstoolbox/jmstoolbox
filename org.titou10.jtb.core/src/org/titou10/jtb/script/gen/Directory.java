
package org.titou10.jtb.script.gen;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;

/**
 * <p>
 * Classe Java pour directory complex type.
 * </p>
 * 
 * <p>
 * Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * </p>
 * 
 * <pre>{@code
 * <complexType name="directory">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="script" type="{}script" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="directory" type="{}directory" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "directory", propOrder = { "script", "directory" })
public class Directory {

   protected List<Script>    script;
   protected List<Directory> directory;
   @XmlAttribute(name = "name")
   protected String          name;

   @XmlTransient
   protected Directory       parent;

   public Directory getParent() {
      return parent;
   }

   public void setParent(Directory parent) {
      this.parent = parent;
   }

   /**
    * Gets the value of the script property.
    * 
    * <p>
    * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned
    * list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the script property.
    * </p>
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * </p>
    * 
    * <pre>
    * getScript().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list {@link Script }
    * </p>
    * 
    * 
    * @return The value of the script property.
    */
   public List<Script> getScript() {
      if (script == null) {
         script = new ArrayList<>();
      }
      return this.script;
   }

   /**
    * Gets the value of the directory property.
    * 
    * <p>
    * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned
    * list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the directory property.
    * </p>
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * </p>
    * 
    * <pre>
    * getDirectory().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list {@link Directory }
    * </p>
    * 
    * 
    * @return The value of the directory property.
    */
   public List<Directory> getDirectory() {
      if (directory == null) {
         directory = new ArrayList<>();
      }
      return this.directory;
   }

   /**
    * Obtient la valeur de la propriété name.
    * 
    * @return possible object is {@link String }
    * 
    */
   public String getName() {
      return name;
   }

   /**
    * Définit la valeur de la propriété name.
    * 
    * @param value
    *           allowed object is {@link String }
    * 
    */
   public void setName(String value) {
      this.name = value;
   }

}
