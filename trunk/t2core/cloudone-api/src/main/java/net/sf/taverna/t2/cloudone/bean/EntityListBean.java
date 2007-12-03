package net.sf.taverna.t2.cloudone.bean;

import java.util.ArrayList;
import java.util.List;

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
public class EntityListBean {
	private String identifier;

	private List<String> content = new ArrayList<String>();

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
