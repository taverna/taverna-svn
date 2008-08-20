package net.sf.taverna.t2.cloudone.bean;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.sf.taverna.t2.cloudone.entity.EntityList;
import net.sf.taverna.t2.util.beanable.Beanable;


/**
 * Bean for serialising an {@link EntityList}. An EntityList is serialised as a
 * String identifier from {@link #getIdentifier()} and the content of the list
 * as a {@link List} of String identifiers.
 * 
 * @see Beanable
 * @see EntityList
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
@Entity
@XmlRootElement(namespace = "http://taverna.sf.net/t2/cloudone/bean/", name = "entityList")
@XmlType(namespace = "http://taverna.sf.net/t2/cloudone/bean/", name = "entityList")
public class EntityListBean {
	@Id
	private String identifier;
	//@CollectionOfElements //needed since JPA can't cope with lists of strings!
	private List<String> content = new ArrayList<String>();

	@XmlElement(name = "entity")
	//@CollectionOfElements
	public List<String> getContent() {
		return content;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setContent(List<String> content) {
		this.content = content;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

}
