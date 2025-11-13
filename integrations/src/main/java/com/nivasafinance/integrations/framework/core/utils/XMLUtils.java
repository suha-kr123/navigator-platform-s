package com.nivasafinance.integrations.framework.core.utils;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.springframework.lang.Nullable;

import java.io.StringReader;
import java.io.StringWriter;

public class XMLUtils {

    @Nullable
    public static String serializeToXML(final Object object) {
        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
            final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            final StringWriter sw = new StringWriter();
            jaxbMarshaller.marshal(object, sw);
            return sw.toString();
        } catch (final JAXBException e) {
            return null;
        }
    }

    public static <T> T xmlToClassObject(String xml, Class<T> requiredType) {
        try {
            JAXBContext context = JAXBContext.newInstance(requiredType);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            StringReader reader = new StringReader(xml);
            return requiredType.cast(unmarshaller.unmarshal(reader));
        } catch (JAXBException e) {
            throw new RuntimeException("Error converting XML to object", e);
        }
    }
}
