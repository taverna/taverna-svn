package net.sf.taverna.raven;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.net.URL;

import net.sf.taverna.raven.log.Log;
import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.RepositoryListener;
import net.sf.taverna.raven.repository.impl.LocalRepository;
import net.sf.taverna.raven.spi.Profile;
import net.sf.taverna.raven.spi.ProfileFactory;

/**
 * Bootstrap a raven installation, allows a non raven aware
 * loader to create a repository structure etc and boot into
 * an artifact class loader driven world view without having
 * to actually have raven installed to begin with. Also handles
 * splash screen management and a few other conveniences.
 * @author Tom Oinn
 * @author Matthew Pocock
 */
public class Loader {
	
	public static Log logger = Log.getLogger(Loader.class);
	
	/**
	 * Initialize raven's repository and load the specified artifact into it.
	 * Returns the named class from the classloader containing the specified
	 * artifact and all of its dependencies.
	 *
	 * @param localRepositoryLocation Directory on disk to use as a local cache for m2 artifacts
	 * @param remoteRepositories array of URLs to remote Maven2 repositories
	 * @param targetGroup Group ID of the Maven2 artifact to bootstrap from
	 * @param targetArtifact Artifact ID of the Maven2 artifact to bootstrap from
	 * @param targetVersion Version of the Maven2 artifact to bootstrap from
	 * @param className Class name to return from the target artifact
	 * @param listener  a RepositoryListener that will be informed of repository
	 * events
	 * @return Class object loaded by raven as specified by the arguments to this method call
	 * @throws ArtifactNotFoundException
	 * @throws ArtifactStateException
	 * @throws ClassNotFoundException
	 */
	public static Class doRavenMagic(
			File localRepositoryLocation,
			URL[] remoteRepositories,
			String targetGroup,
			String targetArtifact,
			String targetVersion,
			String className,
			RepositoryListener listener)
	throws ArtifactNotFoundException,
	ArtifactStateException,
	ClassNotFoundException
	
	{
		Repository repository =
			LocalRepository.getRepository(localRepositoryLocation);
		
		for(URL remoteLocation : remoteRepositories)
		{
			repository.addRemoteRepository(remoteLocation);
		}
		
		Artifact artifact =
			new BasicArtifact(targetGroup, targetArtifact, targetVersion);
		repository.addArtifact(artifact);
		repository.addRepositoryListener(listener);
		repository.update();
		repository.removeRepositoryListener(listener);
		ClassLoader loader = repository.getLoader(artifact, null);
		return loader.loadClass(className);
	}
	
	/**
	 * Initialize raven's repository, show a splash screen and load the specified
	 * artifact into it. Returns the named class from the classloader containing
	 * the specified artifact and all of its dependencies. This will
	 * additionally download the specified version of raven.
	 * .
	 * @param ravenVersion The version of Raven to install or check in the local repository
	 * @param localRepositoryLocation Directory on disk to use as a local cache for m2 artifacts
	 * @param remoteRepositories array of URLs to remote Maven2 repositories
	 * @param targetGroup Group ID of the Maven2 artifact to bootstrap from
	 * @param targetArtifact Artifact ID of the Maven2 artifact to bootstrap from
	 * @param targetVersion Version of the Maven2 artifact to bootstrap from
	 * @param className Class name to return from the target artifact
	 * @param splashScreenURL URL to a splash screen image of a form a JLabel can use
	 * @param splashTime Time in milliseconds to display the splashscreen, splashscreen is
	 * displayed while either this timeout is not exceeded or there is any raven activity.
	 * @return Class object loaded by raven as specified by the arguments to this method call
	 * @throws ArtifactNotFoundException
	 * @throws ArtifactStateException
	 * @throws ClassNotFoundException
	 */
	public static Class doRavenMagic(String ravenVersion,
			File localRepositoryLocation,
			URL[] remoteRepositories,
			String targetGroup,
			String targetArtifact,
			String targetVersion,
			String className,
			URL splashScreenURL,
			int splashTime)
	throws ArtifactNotFoundException,
	ArtifactStateException,
	ClassNotFoundException {			
		SplashScreen splash = null;
		if (!GraphicsEnvironment.isHeadless() && splashScreenURL != null) {			
			splash = SplashScreen.getSplashScreen(splashScreenURL);
		}
			
		final Repository repository =
			LocalRepository.getRepository(localRepositoryLocation);
		
		for (URL remoteLocation : remoteRepositories) {
			repository.addRemoteRepository(remoteLocation);
		}
		Artifact artifact = new BasicArtifact(targetGroup, targetArtifact, targetVersion);
		repository.addArtifact(artifact);
		repository.addArtifact(new BasicArtifact("uk.org.mygrid.taverna.raven","raven",ravenVersion));
		
		//add any profile artifacts, if defined, so that all required artifacts get loaded at startup
		Profile profile = ProfileFactory.getInstance().getProfile();
		if (profile != null) {
			for (Artifact a : profile.getArtifacts()) {
				repository.addArtifact(a);
			}
		}
		if (splash != null) {
			splash.listenToRepository(repository);
		}
		repository.update();
		if (splash != null) {
			splash.removeListener();
			splash.setText("Done...");			
		} 
		//logger.debug("Done close request");
		ClassLoader loader = repository.getLoader(artifact,null);
		//logger.debug("Got classloader");
		return loader.loadClass(className);
	}	
}



