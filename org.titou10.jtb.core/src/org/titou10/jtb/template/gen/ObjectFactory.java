
package org.titou10.jtb.template.gen;

import jakarta.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.titou10.jtb.template.gen package. 
 * <p>An ObjectFactory allows you to programmatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.titou10.jtb.template.gen
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Templates }
     * 
     * @return
     *     the new instance of {@link Templates }
     */
    public Templates createTemplates() {
        return new Templates();
    }

    /**
     * Create an instance of {@link TemplateDirectory }
     * 
     * @return
     *     the new instance of {@link TemplateDirectory }
     */
    public TemplateDirectory createTemplateDirectory() {
        return new TemplateDirectory();
    }

}
