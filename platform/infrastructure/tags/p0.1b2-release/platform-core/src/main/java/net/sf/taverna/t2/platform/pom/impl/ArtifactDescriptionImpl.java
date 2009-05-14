package net.sf.taverna.t2.platform.pom.impl;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.platform.pom.ArtifactDescription;
import net.sf.taverna.t2.platform.pom.ArtifactIdentifier;

/**
 * Simple implementation of ArtifactDescription, use the PomParserImpl to create
 * and populate correctly
 * 
 * @author Tom Oinn
 */
public class ArtifactDescriptionImpl implements ArtifactDescription {

	ArtifactIdentifier id;
	List<ArtifactIdentifier> deps = new ArrayList<ArtifactIdentifier>();
	List<ArtifactIdentifier> optionalDeps = new ArrayList<ArtifactIdentifier>();
	List<String> exclusions = new ArrayList<String>();

	ArtifactDescriptionImpl(ArtifactIdentifier id,
			List<ArtifactIdentifier> deps, List<ArtifactIdentifier> optionalDeps, List<String> exclusions) {
		this.id = id;
		this.deps = deps;
		this.optionalDeps = optionalDeps;
		this.exclusions = exclusions;
	}

	public List<String> getExclusions() {
		return this.exclusions;
	}
	
	public ArtifactIdentifier getId() {
		return this.id;
	}

	public List<ArtifactIdentifier> getMandatoryDependencies() {
		return this.deps;
	}

	public List<ArtifactIdentifier> getOptionalDependencies() {
		return this.optionalDeps;
	}

	/**
	 * Print out a summary of the artifact description including the optional
	 * and mandatory dependencies
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Description for " + id.toString() + "\n");
		sb.append("Dependencies (mandatory) : \n");
		for (ArtifactIdentifier dep : deps) {
			sb.append("  " + dep.toString() + "\n");
		}
		sb.append("Dependencies (optional) : \n");
		for (ArtifactIdentifier dep : optionalDeps) {
			sb.append("  " + dep.toString() + "\n");
		}
		sb.append("End description\n");
		return sb.toString();
	}

}
