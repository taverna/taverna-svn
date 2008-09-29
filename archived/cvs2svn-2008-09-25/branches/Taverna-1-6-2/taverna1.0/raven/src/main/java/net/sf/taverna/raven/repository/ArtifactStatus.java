package net.sf.taverna.raven.repository;

import java.util.Date;

/**
 * Enumeration of the possible states a dependency
 * may occupy during the resolution, fetch and verify
 * process. The valid transitions between these states
 * are shown below:<p/>
 * <img src="doc-files/states.png"/><p/>
 * When an artifact is added to the Repository it is initially
 * in the Queued state. The first action is to attempt to download
 * the POM file from each of the remote URLs, if this fails the state
 * shifts to PomFailed, otherwise to Pom. The Pom is then analyzed and
 * the &lt;packaging&gt; element inspected, if present and its contents
 * are not 'jar' then the Pom is assumed to refer to something else
 * and the artifact moves to the PomNonJar state. If the artifact is
 * a jar type then the dependency section within the Pom is analyzed - 
 * this causes any new dependencies not already present in the repository
 * to be added in the Queued state and this artifact moves to JarFetching
 * status. If download of the Jar fails the state is marked as JarFailed otherwise
 * it transitions to Jar. Any artifacts with Jar state are checked to see
 * whether they have no dependencies - if so they move to Ready, they also
 * move to this state if all their child dependencies have Ready status. If
 * any child dependency has an error state as denoted by the red boxes then
 * the artifact moves to the DependencyFailure error state.<p/>
 * The PomFetching and JarFetching states denote download activities - while
 * an artifact is in these states the detailed progress of the download can be
 * monitored through the getDownloadStatus(Artifact a) method in the Repository
 * interface.
 * @author Tom Oinn
 */
public enum ArtifactStatus implements Comparable<ArtifactStatus> {

	/**
	 * No information is known about this Artifact, it is not present
	 * in the Repository
	 */
	Unknown ("No known artifact",0),
	
	/**
	 * Artifact has been added to the download queue but no fetch
	 * has occured
	 */
	Queued ("Queued",1), 

	/**
	 * Downloading Pom file
	 */
	PomFetching ("Downloading description",2),
	/**
	 * Pom has been fetched, no jar file as yet
	 */
	Pom ("Fetched description",3),	
	
	/**
	 * An error occured during download of the Pom
	 */
	PomFailed ("Failed to fetch description",3,true),
	
	/**
	 * Non jar package method for the POM, means we
	 * can't do anything further with it
	 */
	PomNonJar ("Pom specifies a non jar package, ignoring",4,true),
	
	/**
	 * Pom has been parsed, immediate dependencies added
	 */
	Analyzed ("Queued dependencies",4),
	
	/**
	 * Downloading the jar file
	 */
	JarFetching ("Downloading jar archive",5),
	/**
	 * Both pom and jar have been downloaded but dependencies may exist
	 * which are not resolved fully
	 */
	Jar ("Fetched jar archive",6), 
	
	/**
	 * An error occured during download of the Jar
	 */
	JarFailed ("Failed to fetch jar archive",6,true),

	/**
	 * Fully resolved, a ClassLoader can be created at this point
	 */
	Ready ("All dependencies resolved, ready for use",7),
	
	/**
	 * One or more dependencies of this artifact failed to resolve,
	 * this may include transitive dependency failure.
	 */
	DependencyFailure ("One or more dependencies could not be fetched",7,true);
	
	private Date created;
	private String description;
	private boolean error = false;
	private Exception exception;
	private int order;
	
	private ArtifactStatus(String description, int order) {
		this.description = description;
		this.created = new Date();
		this.order = order;
	}
	private ArtifactStatus(String description, int order, boolean error) {
		this.description = description;
		this.created = new Date();
		this.error = error;
		this.order = order;
	}

	/**
	 * @return the index in the partial order of ArtifactStatus enumeration
	 */
	public int getOrder() {
		return this.order;
	}
	
	/**
	 * @return Returns the Date this status object was created.
	 */
	public Date getCreated() {
		return this.created;
	}

	/**
	 * @return Returns a free text description of the status.
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * @return Returns the exception.
	 */
	public Exception getException() {
		return this.exception;
	}

	/**
	 * @param exception The exception to set.
	 */
	public void setException(Exception exception) {
		this.exception = exception;
	}

	/**
	 * @return Returns true if this status indicates a failure.
	 */
	public boolean isError() {
		return this.error;
	}
		
}
