package net.sf.taverna.t2.component.registry.myexperiment;

class MyExperimentPrivatePolicy extends MyExperimentSharingPolicy {
	@Override
	public String getName() {
		return "Private";
	}

	@Override
	public String getPolicyString() {
		StringBuilder contentXml = new StringBuilder();
//		contentXml.append("<permissions>");
//		contentXml.append("</permissions>");
		return contentXml.toString();
	}

}
