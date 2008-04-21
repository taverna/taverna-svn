package net.sf.taverna.raven.repository;

import java.net.URL;
import java.util.List;

/**
 * A local repository containing Artifact instances in various states.
 * The repository implementation may or may not cache artifacts from
 * the remote sites but is not required to. If it does then it should
 * use a local directory structure compatible with that used by Maven2 
 * @author Tom
 *
 */
public interface Repository {

	/**
	 * Add a new artifact to this repository. If the artifact already exists
	 * no action will be taken, otherwise it will be created and given the status
	 * ArtifactStatus.Queued
	 * @param a Artifact to query status
	 */
	public abstract void addArtifact(Artifact a);

	/**
	 * Add a reference to a remote repository to be used to fetch pom and jar
	 * files. Repositories are scanned in turn until a match is found.
	 * @param repositoryURL
	 */
	public abstract void addRemoteRepository(URL repositoryURL);

	/**
	 * Get a ClassLoader for the specified artifact
	 * @param a The artifact to set up the classloader for
	 * @param parent Optional parent ClassLoader, may be null for no parent
	 * @return ClassLoader for the specified artifact
	 * @throws ArtifactNotFoundException 
	 * @throws ArtifactStateException 
	 */
	public abstract ClassLoader getLoader(Artifact a, ClassLoader parent) throws ArtifactNotFoundException, ArtifactStateException;

	/**
	 * Fetch the ArtifactStatus represengint the state of the given Artifact
	 * in this Repository, if the artifact is not found then this method will
	 * return ArtifactStatus.Unknown
	 * @param a Artifact to query
	 * @return ArtifactStatus corresponding to the state of the specified artifact
	 * in relation to this Repository
	 */
	public abstract ArtifactStatus getStatus(Artifact a);
	
	/**
	 * Fetch the DownloadStatus for the given Artifact, only valid for 
	 * PomFetching and JarFetching Artifact states.
	 * @param a Artifact to query
	 * @return DownloadStatus corresponding to the state of the download in progress
	 * for this artifact, if any. The artifact must be in the state ArtifactStatus.PomFetching
	 * or ArtifactStatus.JarFetching
	 * @throws ArtifactStateException if the artifact is not in an appropriate state
	 * @throws ArtifactNotFoundException 
	 */
	public abstract DownloadStatus getDownloadStatus(Artifact a) 
	throws ArtifactStateException, ArtifactNotFoundException;
	
	/**
	 * Get the list of known Artifacts for this Repository
	 * @return a copy of the current list of all known Artifacts within the
	 * Repository
	 */
	public abstract List<Artifact> getArtifacts();
	
	/**
	 * Get the list of known Artifacts for this Repository with the specified
	 * ArtifactStatus
	 * @return a list of all known Artifacts with the given ArtifactStatus
	 * within this Repository
	 */
	public abstract List<Artifact> getArtifacts(ArtifactStatus s);
	
	/**
	 * Scan the status table, perform actions on each item based
	 * on the status.
	 * If an item is Queued then start downloading the POM
	 * If the item has a downloaded POM then parse it, add any unknown
	 * dependencies to the status with Queued state and start downloading
	 * the jar file
	 * If the item has a jar file then traverse dependencies and determine
	 * whether all transitive dependencies are satisfied in which case mark
	 * as Ready state
	 * Repeat this until all dependencies are resolved or failed
	 */
	public abstract void update();
	
	/**
	 * Add a listener to be notified on changes to the repository status
	 */
	public abstract void addRepositoryListener(RepositoryListener l);
	
	/**
	 * Remove a listener from the list of interested observers
	 */
	public abstract void removeRepositoryListener(RepositoryListener l);

	/**
	 * Given a Class object return the Artifact whose LocalArtifactClassLoader created it. 
	 * If the classloader was not an instance of LocalArtifactClassLoader an 
	 * ArtifactNotFoundException is thrown
	 */
	public abstract Artifact artifactForClass(Class c) throws ArtifactNotFoundException;
	
}
