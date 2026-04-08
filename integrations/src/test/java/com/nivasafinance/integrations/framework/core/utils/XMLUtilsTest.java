package com.nivasafinance.integrations.framework.core.utils;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class XMLUtilsTest {

    @XmlRootElement(name = "item")
    static class TestXmlObject {
        private String name;

        public TestXmlObject() {}

        public TestXmlObject(String name) {
            this.name = name;
        }

        @XmlElement
        public String getName() { return name; }

        public void setName(String name) { this.name = name; }
    }

    // ── serializeToXML ──

    @Test
    void serializeToXML_withValidObject_returnsXmlString() {
        TestXmlObject object = new TestXmlObject("test-value");

        String xml = XMLUtils.serializeToXML(object);

        assertNotNull(xml, "Serialized XML should not be null for a valid JAXB object");
        assertTrue(xml.contains("<name>test-value</name>"),
                "XML should contain the serialized name element");
        assertTrue(xml.contains("<item>"),
                "XML should contain the root element tag");
    }

    @Test
    void serializeToXML_withNonJaxbObject_returnsNull() {
        Object plainObject = new Object();

        String xml = XMLUtils.serializeToXML(plainObject);

        assertNull(xml, "Should return null when JAXB cannot serialize the object");
    }

    // ── xmlToClassObject ──

    @Test
    void xmlToClassObject_withValidXml_returnsDeserializedObject() {
        String xml = "<item><name>deserialized-value</name></item>";

        TestXmlObject result = XMLUtils.xmlToClassObject(xml, TestXmlObject.class);

        assertNotNull(result, "Deserialized object should not be null");
        assertEquals("deserialized-value", result.getName(),
                "Deserialized name should match the XML content");
    }

    @Test
    void xmlToClassObject_withInvalidXml_throwsRuntimeException() {
        String invalidXml = "not-valid-xml<<<";

        assertThrows(RuntimeException.class,
                () -> XMLUtils.xmlToClassObject(invalidXml, TestXmlObject.class),
                "Should throw RuntimeException when XML is malformed");
    }
}
