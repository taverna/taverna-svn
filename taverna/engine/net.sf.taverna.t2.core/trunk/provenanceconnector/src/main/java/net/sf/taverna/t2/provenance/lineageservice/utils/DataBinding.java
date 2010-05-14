package net.sf.taverna.t2.provenance.lineageservice.utils;

public class DataBinding {
	private String dataBindingId;
	private Var port;
	private String t2Reference;
	private String workflowRunId;
	public String getDataBindingId() {
		return dataBindingId;
	}
	public void setDataBindingId(String dataBindingId) {
		this.dataBindingId = dataBindingId;
	}
	public Var getPort() {
		return port;
	}
	public void setPort(Var port) {
		this.port = port;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((dataBindingId == null) ? 0 : dataBindingId.hashCode());
		result = prime * result + ((port == null) ? 0 : port.hashCode());
		return result;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DataBinding [dataBindingId=");
		builder.append(dataBindingId);
		builder.append(", port=");
		builder.append(port);
		builder.append(", t2Reference=");
		builder.append(t2Reference);
		builder.append("]");
		return builder.toString();
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataBinding other = (DataBinding) obj;
		if (dataBindingId == null) {
			if (other.dataBindingId != null)
				return false;
		} else if (!dataBindingId.equals(other.dataBindingId))
			return false;
		if (port == null) {
			if (other.port != null)
				return false;
		} else if (!port.equals(other.port))
			return false;
		return true;
	}
	public String getT2Reference() {
		return t2Reference;
	}
	public void setT2Reference(String t2Reference) {
		this.t2Reference = t2Reference;
	}
	public String getWorkflowRunId() {
		return workflowRunId;
	}
	public void setWorkflowRunId(String workflowRunId) {
		this.workflowRunId = workflowRunId;
	}
}
