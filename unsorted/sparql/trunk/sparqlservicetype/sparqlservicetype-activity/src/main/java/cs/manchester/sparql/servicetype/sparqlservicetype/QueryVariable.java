package cs.manchester.sparql.servicetype.sparqlservicetype;

public class QueryVariable {
	
	private String variableName;
	
	private boolean exposeAsPort;

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	public boolean isExposeAsPort() {
		return exposeAsPort;
	}

	public void setExposeAsPort(boolean exposeAsPort) {
		this.exposeAsPort = exposeAsPort;
	}	
	
}
