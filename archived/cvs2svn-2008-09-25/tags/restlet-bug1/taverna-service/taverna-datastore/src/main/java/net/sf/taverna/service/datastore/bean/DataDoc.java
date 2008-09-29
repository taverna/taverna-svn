package net.sf.taverna.service.datastore.bean;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Transient;

import net.sf.taverna.service.interfaces.ParseException;
import net.sf.taverna.service.util.XMLUtils;

import org.embl.ebi.escience.baclava.DataThing;
import org.hibernate.validator.NotNull;

/**
 * A Baclava data document. Usable both as a input document for running a
 * Taverna workflow and containing the output document.
 * 
 * @author Stian Soiland
 */
@Entity
@NamedQueries(value = { @NamedQuery(name = DataDoc.NAMED_QUERY_ALL, query = "SELECT d FROM DataDoc d ORDER BY d.created DESC") })
public class DataDoc extends OwnedResource {
	public static final String NAMED_QUERY_ALL = "allDatadocs";

	/**
	 * Maximum size of Baclava data document, 100 MB
	 */
	public static final int BACLAVA_MAX = 100 * 1024 * 1024;

	@NotNull
	@Lob
	@Column(length = DataDoc.BACLAVA_MAX)
	private String baclava = "";

	public String getBaclava() {
		return baclava;
	}

	public void setBaclava(String baclava) {
		this.baclava = baclava;
		setLastModified();
	}

	/**
	 * Return a {@link Map} representation of the Baclava data document. Note
	 * that modifications to the map are not preserved unless submitted back
	 * with setDataMap().
	 */
	@Transient
	public Map<String, DataThing> getDataMap() throws ParseException {
		if (getBaclava().equals("")) {
			return new HashMap<String, DataThing>();
		}
		return XMLUtils.parseDataDoc(baclava);
	}

	@Transient
	public void setDataMap(Map<String, DataThing> values) {
		setBaclava(XMLUtils.makeDataDocument(values));
	}

}
