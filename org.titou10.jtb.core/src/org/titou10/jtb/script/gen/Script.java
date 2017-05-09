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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Classe Java pour script complex type.
 * 
 * <p>
 * Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="script">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="step" type="{}step" maxOccurs="unbounded"/>
 *         &lt;element name="globalVariable" type="{}globalVariable" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="dataFile" type="{}dataFile" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "script", propOrder = { "step", "globalVariable", "dataFile" })
public class Script {

   @XmlElement(required = true)
   protected List<Step>           step;
   protected List<GlobalVariable> globalVariable;
   protected List<DataFile>       dataFile;
   @XmlAttribute(name = "name")
   protected String               name;

   @XmlTransient
   protected Directory            parent;

   public Directory getParent() {
      return parent;
   }

   public void setParent(Directory parent) {
      this.parent = parent;
   }

   /**
    * Gets the value of the step property.
    * 
    * <p>
    * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned
    * list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the step property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * 
    * <pre>
    * getStep().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list {@link Step }
    * 
    * 
    */
   public List<Step> getStep() {
      if (step == null) {
         step = new ArrayList<Step>();
      }
      return this.step;
   }

   /**
    * Gets the value of the globalVariable property.
    * 
    * <p>
    * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned
    * list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the globalVariable
    * property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * 
    * <pre>
    * getGlobalVariable().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list {@link GlobalVariable }
    * 
    * 
    */
   public List<GlobalVariable> getGlobalVariable() {
      if (globalVariable == null) {
         globalVariable = new ArrayList<GlobalVariable>();
      }
      return this.globalVariable;
   }

   /**
    * Gets the value of the dataFile property.
    * 
    * <p>
    * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned
    * list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the dataFile property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * 
    * <pre>
    * getDataFile().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list {@link DataFile }
    * 
    * 
    */
   public List<DataFile> getDataFile() {
      if (dataFile == null) {
         dataFile = new ArrayList<DataFile>();
      }
      return this.dataFile;
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
