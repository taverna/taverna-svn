//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.10.31 at 02:43:23 PM GMT 
//


package net.sf.taverna.t2.platform.plugin.generated.impl;

import java.net.URI;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import net.sf.taverna.t2.platform.plugin.adapters.URIAdapter;
import net.sf.taverna.t2.platform.plugin.generated.ExternalLink;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "linkType", propOrder = {
    "linkDescription",
    "link"
})
public class ExternalLinkImpl
    implements ExternalLink
{

    @XmlElement(name = "linkdescription", required = true)
    protected String linkDescription;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(URIAdapter.class)
    protected URI link;

    public String getLinkDescription() {
        return linkDescription;
    }

    public void setLinkDescription(String value) {
        this.linkDescription = value;
    }

    public URI getLink() {
        return link;
    }

    public void setLink(URI value) {
        this.link = value;
    }

}
