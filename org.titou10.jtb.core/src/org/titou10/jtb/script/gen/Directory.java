//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.8-b130911.1802 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2017.05.09 à 03:37:23 PM EDT 
//

package org.titou10.jtb.script.gen;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Classe Java pour directory complex type.
 * 
 * <p>
 * Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="directory">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="script" type="{}script" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="directory" type="{}directory" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
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
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * 
    * <pre>
    * getScript().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list {@link Script }
    * 
    * 
    */
   public List<Script> getScript() {
      if (script == null) {
         script = new ArrayList<Script>();
      }
      return this.script;
   }

   /**
    * Gets the value of the directory property.
    * 
    * <p>
    * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned
    * list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the directory property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * 
    * <pre>
    * getDirectory().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list {@link Directory }
    * 
    * 
    */
   public List<Directory> getDirectory() {
      if (directory == null) {
         directory = new ArrayList<Directory>();
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
