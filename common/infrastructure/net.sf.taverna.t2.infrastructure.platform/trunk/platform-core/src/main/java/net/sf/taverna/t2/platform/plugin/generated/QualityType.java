//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.10.31 at 02:43:23 PM GMT 
//


package net.sf.taverna.t2.platform.plugin.generated;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * <p>Java class for qualityType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="qualityType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="alpha"/>
 *     &lt;enumeration value="beta"/>
 *     &lt;enumeration value="stable"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum QualityType {

    @XmlEnumValue("alpha")
    ALPHA("alpha"),
    @XmlEnumValue("beta")
    BETA("beta"),
    @XmlEnumValue("stable")
    STABLE("stable");
    private final String value;

    QualityType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static QualityType fromValue(String v) {
        for (QualityType c: QualityType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
