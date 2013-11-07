package net.sf.taverna.t2.component.registry.myexperiment;

class MyExperimentGroupPolicy extends MyExperimentSharingPolicy {

	private final String name;
	private final String id;

	public MyExperimentGroupPolicy(String name, String id) {
		this.name = name;
		this.id = id;

	}

	@Override
	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	@Override
	public String getPolicyString() {
		StringBuilder contentXml = new StringBuilder();
		contentXml.append("<permissions>");
		contentXml.append("<group-policy-id>");
		contentXml.append(getId());
		contentXml.append("</group-policy-id>");
		contentXml.append("</permissions>");
		return contentXml.toString();

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MyExperimentGroupPolicy other = (MyExperimentGroupPolicy) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
