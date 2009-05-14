package net.sf.taverna.t2.platform.pom;

import java.util.List;

/**
 * Populated description for a Maven 2 artifact. This interface does not reflect
 * all possible properties defined in a Maven 2 POM file, just those used by the
 * T2 platform. To obtain an instance of ArtifactDescription you should use a
 * PomParser.
 * <p>
 * This interface does not represent a Maven 2 POM file, it represents the
 * artifact the POM file represents. This means there is no 'get parent' method
 * here, although the parent POM would be used to populate this description by
 * the parser if needed.
 * 
 * @author Tom Oinn
 * 
 */
public interface ArtifactDescription {

	/**
	 * Each artifact description has a single artifact identifier in the form of
	 * an ArtifactIdentifier. Use this to get the artifactId, groupId and
	 * version properties.
	 */
	public ArtifactIdentifier getId();

	/**
	 * Artifacts have a list of associated mandatory runtime dependencies -
	 * these are required for the classes in the artifact to link or otherwise
	 * function at runtime. We're not interested here in dependencies only
	 * needed in compilation or test phases. These are only dependencies
	 * declared in the pom for this artifact, not those inherited transitively.
	 */
	public List<ArtifactIdentifier> getMandatoryDependencies();

	/**
	 * Artifacts may also have optional runtime dependencies, in general
	 * libraries which enhance the functionality of the artifact if present but
	 * are not required for all scenarios. These are only dependencies declared
	 * in the pom for this artifact, not those inherited transitively.
	 */
	public List<ArtifactIdentifier> getOptionalDependencies();

	/**
	 * POM files can define exclusions, dependencies which would be implied by
	 * transitivity but which are explicitly excluded. These are specified
	 * simply as a groupId and artifactId, the exclusion applies irrespective of
	 * version.
	 * 
	 * @return a list of strings of the form <code>artifactId:groupId</code>
	 *         defining exclusions which should not be linked to any class
	 *         loaders created from this artifact description
	 */
	public List<String> getExclusions();

}
