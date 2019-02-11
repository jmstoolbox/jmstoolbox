
package org.titou10.jtb.script.gen;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.titou10.jtb.util.Constants;

/**
 * <p>
 * Java class for step complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="step"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="kind" type="{}stepKind"/&gt;
 *         &lt;element name="templateName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="templateDirectory" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="sessionName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="destinationName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="variablePrefix" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="payloadDirectory" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="pauseSecsAfter" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="iterations" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "step",
         propOrder = { "kind", "templateName", "templateDirectory", "sessionName", "destinationName", "variablePrefix",
                       "payloadDirectory", "pauseSecsAfter", "iterations" })
public class Step {

   @XmlElement(required = true)
   @XmlSchemaType(name = "string")
   protected StepKind kind;
   @XmlElement(required = true)
   protected String   templateName;
   @XmlElement(required = true)
   protected String   templateDirectory;
   @XmlElement(required = true)
   protected String   sessionName;
   @XmlElement(required = true)
   protected String   destinationName;
   protected String   variablePrefix;
   protected String   payloadDirectory;
   protected Integer  pauseSecsAfter;
   protected int      iterations;

   // Set templateDirectory for script < v4.1.0
   public String getTemplateDirectory() {
      if ((templateName != null) && (templateDirectory == null)) {
         templateDirectory = Constants.JTB_TEMPLATE_CONFIG_FOLDER_NAME;
      }
      return templateDirectory;
   }

   /**
    * Gets the value of the kind property.
    * 
    * @return possible object is {@link StepKind }
    * 
    */
   public StepKind getKind() {
      return kind;
   }

   /**
    * Sets the value of the kind property.
    * 
    * @param value
    *           allowed object is {@link StepKind }
    * 
    */
   public void setKind(StepKind value) {
      this.kind = value;
   }

   /**
    * Gets the value of the templateName property.
    * 
    * @return possible object is {@link String }
    * 
    */
   public String getTemplateName() {
      return templateName;
   }

   /**
    * Sets the value of the templateName property.
    * 
    * @param value
    *           allowed object is {@link String }
    * 
    */
   public void setTemplateName(String value) {
      this.templateName = value;
   }

   // /**
   // * Gets the value of the templateDirectory property.
   // *
   // * @return possible object is {@link String }
   // *
   // */
   // public String getTemplateDirectory() {
   // return templateDirectory;
   // }

   /**
    * Sets the value of the templateDirectory property.
    * 
    * @param value
    *           allowed object is {@link String }
    * 
    */
   public void setTemplateDirectory(String value) {
      this.templateDirectory = value;
   }

   /**
    * Gets the value of the sessionName property.
    * 
    * @return possible object is {@link String }
    * 
    */
   public String getSessionName() {
      return sessionName;
   }

   /**
    * Sets the value of the sessionName property.
    * 
    * @param value
    *           allowed object is {@link String }
    * 
    */
   public void setSessionName(String value) {
      this.sessionName = value;
   }

   /**
    * Gets the value of the destinationName property.
    * 
    * @return possible object is {@link String }
    * 
    */
   public String getDestinationName() {
      return destinationName;
   }

   /**
    * Sets the value of the destinationName property.
    * 
    * @param value
    *           allowed object is {@link String }
    * 
    */
   public void setDestinationName(String value) {
      this.destinationName = value;
   }

   /**
    * Gets the value of the variablePrefix property.
    * 
    * @return possible object is {@link String }
    * 
    */
   public String getVariablePrefix() {
      return variablePrefix;
   }

   /**
    * Sets the value of the variablePrefix property.
    * 
    * @param value
    *           allowed object is {@link String }
    * 
    */
   public void setVariablePrefix(String value) {
      this.variablePrefix = value;
   }

   /**
    * Gets the value of the payloadDirectory property.
    * 
    * @return possible object is {@link String }
    * 
    */
   public String getPayloadDirectory() {
      return payloadDirectory;
   }

   /**
    * Sets the value of the payloadDirectory property.
    * 
    * @param value
    *           allowed object is {@link String }
    * 
    */
   public void setPayloadDirectory(String value) {
      this.payloadDirectory = value;
   }

   /**
    * Gets the value of the pauseSecsAfter property.
    * 
    * @return possible object is {@link Integer }
    * 
    */
   public Integer getPauseSecsAfter() {
      return pauseSecsAfter;
   }

   /**
    * Sets the value of the pauseSecsAfter property.
    * 
    * @param value
    *           allowed object is {@link Integer }
    * 
    */
   public void setPauseSecsAfter(Integer value) {
      this.pauseSecsAfter = value;
   }

   /**
    * Gets the value of the iterations property.
    * 
    */
   public int getIterations() {
      return iterations;
   }

   /**
    * Sets the value of the iterations property.
    * 
    */
   public void setIterations(int value) {
      this.iterations = value;
   }

}
