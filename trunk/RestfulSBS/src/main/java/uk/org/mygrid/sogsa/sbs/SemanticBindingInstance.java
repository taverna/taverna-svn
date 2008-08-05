package uk.org.mygrid.sogsa.sbs;
/**
 * An instance of a Semantic Binding
 * @author Ian Dunlop
 *
 */
public class SemanticBindingInstance {
	
	private String key;
	private String rdf;


	public SemanticBindingInstance(String key, String rdf) {
		this.key = key;
		this.rdf = rdf;
	}

	public String getKey() {
		return key;
	}

	public String getRdf() {
		return rdf;
	}

}
