
package org.titou10.jtb.script.gen;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;

/**
 * <p>
 * Classe Java pour script complex type.
 * </p>
 * 
 * <p>
 * Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * </p>
 * 
 * <pre>{@code
 * <complexType name="script">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="step" type="{}step" maxOccurs="unbounded"/>
 *         <element name="globalVariable" type="{}globalVariable" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="dataFile" type="{}dataFile" maxOccurs="unbounded" minOccurs="0"/>
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
    * </p>
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * </p>
    * 
    * <pre>
    * getStep().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list {@link Step }
    * </p>
    * 
    * 
    * @return The value of the step property.
    */
   public List<Step> getStep() {
      if (step == null) {
         step = new ArrayList<>();
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
    * </p>
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * </p>
    * 
    * <pre>
    * getGlobalVariable().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list {@link GlobalVariable }
    * </p>
    * 
    * 
    * @return The value of the globalVariable property.
    */
   public List<GlobalVariable> getGlobalVariable() {
      if (globalVariable == null) {
         globalVariable = new ArrayList<>();
      }
      return this.globalVariable;
   }

   /**
    * Gets the value of the dataFile property.
    * 
    * <p>
    * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned
    * list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the dataFile property.
    * </p>
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * </p>
    * 
    * <pre>
    * getDataFile().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list {@link DataFile }
    * </p>
    * 
    * 
    * @return The value of the dataFile property.
    */
   public List<DataFile> getDataFile() {
      if (dataFile == null) {
         dataFile = new ArrayList<>();
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
