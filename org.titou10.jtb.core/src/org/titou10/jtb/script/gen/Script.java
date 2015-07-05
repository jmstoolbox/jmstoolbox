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
 *       &lt;/sequence>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="promptVariables" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "script", propOrder = { "step", "globalVariable" })
public class Script {

   @XmlTransient
   protected Directory parent;

   public Directory getParent() {
      return parent;
   }

   public void setParent(Directory parent) {
      this.parent = parent;
   }

   @XmlElement(required = true)
   protected List<Step>           step;
   protected List<GlobalVariable> globalVariable;
   @XmlAttribute(name = "name")
   protected String               name;
   @XmlAttribute(name = "promptVariables")
   protected Boolean              promptVariables;

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

   /**
    * Obtient la valeur de la propriété promptVariables.
    * 
    * @return possible object is {@link Boolean }
    * 
    */
   public Boolean isPromptVariables() {
      return promptVariables;
   }

   /**
    * Définit la valeur de la propriété promptVariables.
    * 
    * @param value
    *           allowed object is {@link Boolean }
    * 
    */
   public void setPromptVariables(Boolean value) {
      this.promptVariables = value;
   }

}
