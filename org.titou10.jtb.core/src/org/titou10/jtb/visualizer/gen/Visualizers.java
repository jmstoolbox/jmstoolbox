//
// Ce fichier a été généré par Java Architecture for XML Binding (JAXB) Reference Implementation, v2.2.8-b130911.1802 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Les modifications apportées à ce fichier seront perdues lors de la recompilation du schéma source. 
// Généré sur : 2017.04.12 le 09:31:50 AM EDT 
//


package org.titou10.jtb.visualizer.gen;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour anonymous complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu dans cette classe.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="visualizer" type="{}visualizer" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "visualizer"
})
@XmlRootElement(name = "visualizers")
public class Visualizers {

    @XmlElement(required = true)
    protected List<Visualizer> visualizer;

    /**
     * Gets the value of the visualizer property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the visualizer property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVisualizer().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Visualizer }
     * 
     * 
     */
    public List<Visualizer> getVisualizer() {
        if (visualizer == null) {
            visualizer = new ArrayList<Visualizer>();
        }
        return this.visualizer;
    }

}
