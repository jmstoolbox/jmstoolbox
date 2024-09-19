
package org.titou10.jtb.script.gen;

import jakarta.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.titou10.jtb.script.gen package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.titou10.jtb.script.gen
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Scripts }
     * 
     * @return
     *     the new instance of {@link Scripts }
     */
    public Scripts createScripts() {
        return new Scripts();
    }

    /**
     * Create an instance of {@link Directory }
     * 
     * @return
     *     the new instance of {@link Directory }
     */
    public Directory createDirectory() {
        return new Directory();
    }

    /**
     * Create an instance of {@link Script }
     * 
     * @return
     *     the new instance of {@link Script }
     */
    public Script createScript() {
        return new Script();
    }

    /**
     * Create an instance of {@link Step }
     * 
     * @return
     *     the new instance of {@link Step }
     */
    public Step createStep() {
        return new Step();
    }

    /**
     * Create an instance of {@link GlobalVariable }
     * 
     * @return
     *     the new instance of {@link GlobalVariable }
     */
    public GlobalVariable createGlobalVariable() {
        return new GlobalVariable();
    }

    /**
     * Create an instance of {@link DataFile }
     * 
     * @return
     *     the new instance of {@link DataFile }
     */
    public DataFile createDataFile() {
        return new DataFile();
    }

}
