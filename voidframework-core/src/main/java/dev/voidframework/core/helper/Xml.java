package dev.voidframework.core.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Helper to handle XML document.
 */
public final class Xml {

    private static final XmlMapper OBJECT_MAPPER = XmlMapper.builder()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
        .addModule(new JavaTimeModule())
        .addModule(new Jdk8Module())
        .addModule(new JodaModule())
        .build();

    /**
     * Converts an XML to string.
     *
     * @param xml The XML to convert.
     * @return The string representation.
     */
    public static String toString(final Document xml) {

        final Writer writer = new StringWriter();
        try {
            TransformerFactory.newInstance()
                .newTransformer()
                .transform(new DOMSource(xml), new StreamResult(writer));
            writer.flush();
        } catch (final TransformerException | IOException e) {
            throw new RuntimeException(e);
        }

        return writer.toString();
    }

    /**
     * Converts a XML document into to a Java object.
     *
     * @param <OUTPUT_TYPE> The type of the Java object
     * @param xml           XML document to convert
     * @param clazz         Expected Java object type
     * @return The Java object
     */
    public static <OUTPUT_TYPE> OUTPUT_TYPE fromXml(final Document xml, final Class<OUTPUT_TYPE> clazz) {

        try {
            return OBJECT_MAPPER.readValue(toString(xml), clazz);
        } catch (final NullPointerException | IllegalArgumentException | JsonProcessingException ignore) {
            return null;
        }
    }

    /**
     * Converts a XML document into to a Java object.
     *
     * @param <OUTPUT_TYPE> The type of the Java object
     * @param xmlByteArray  XML document as bytes array to convert
     * @param clazz         Expected Java object type
     * @return The Java object
     */
    public static <OUTPUT_TYPE> OUTPUT_TYPE fromXml(final byte[] xmlByteArray, final Class<OUTPUT_TYPE> clazz) {

        try {
            return OBJECT_MAPPER.readValue(xmlByteArray, clazz);
        } catch (final NullPointerException | IllegalArgumentException | IOException ignore) {
            return null;
        }
    }

    /**
     * Converts a byte array to an XML document.
     *
     * @param data data to convert in XML
     * @return The XML document
     */
    public static Document toXml(final byte[] data) {

        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();

            final InputStream inputStream = new ByteArrayInputStream(data);
            return builder.parse(inputStream);
        } catch (final ParserConfigurationException | SAXException | IOException ignore) {
            return null;
        }
    }
}
