//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.8-b130911.1802 
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2017.08.09 à 07:47:37 AM EDT 
//


package org.titou10.jtb.cs.gen;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.titou10.jtb.cs.gen package. 
 * <p>An ObjectFactory allows you to programatically 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.titou10.jtb.cs.gen
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ColumnsSets }
     * 
     */
    public ColumnsSets createColumnsSets() {
        return new ColumnsSets();
    }

    /**
     * Create an instance of {@link ColumnsSet }
     * 
     */
    public ColumnsSet createColumnsSet() {
        return new ColumnsSet();
    }

    /**
     * Create an instance of {@link Column }
     * 
     */
    public Column createColumn() {
        return new Column();
    }

    /**
     * Create an instance of {@link UserProperty }
     * 
     */
    public UserProperty createUserProperty() {
        return new UserProperty();
    }

}
