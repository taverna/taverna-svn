package net.sf.taverna.service.datastore.bean;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 * A workflow representation.
 * 
 * @author Stian Soiland
 *
 */
@Entity
@NamedQueries(value = { @NamedQuery(name = Workflow.NAMED_QUERY_ALL, query = "SELECT w FROM Workflow w ORDER BY w.created DESC") })
public class Workflow extends OwnedResource {

	public static final String NAMED_QUERY_ALL = "allWorkflows";

	/*
	 * Maximum size of Scufl
	 * 
	 */
	public static final int SCUFL_MAX = 65535;

	@Lob
	@Column(length = Workflow.SCUFL_MAX)
	private String scufl;

	public String getScufl() {
		return scufl;
	}

	public void setScufl(String scufl) {
		this.scufl = scufl;
		setLastModified();
	}

}
