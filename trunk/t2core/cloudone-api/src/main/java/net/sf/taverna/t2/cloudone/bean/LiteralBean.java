package net.sf.taverna.t2.cloudone.bean;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Bean for serialising a Literal
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
@Entity
@XmlRootElement(namespace = "http://taverna.sf.net/t2/cloudone/bean/", name = "literal")
@XmlType(namespace = "http://taverna.sf.net/t2/cloudone/bean/", name = "literal")
public class LiteralBean {
	@Id
	String literal;

	@XmlAttribute
	public String getLiteral() {
		return literal;
	}

	public void setLiteral(String literal) {
		this.literal = literal;
	}

}
