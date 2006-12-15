package net.sf.taverna.raven;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

import net.sf.taverna.raven.log.Log;
import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.raven.repository.ArtifactStatus;
import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.raven.repository.DownloadStatus;
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
			splash = new SplashScreen(splashScreenURL, splashTime);
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
		Profile profile=ProfileFactory.getInstance().getProfile();
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
			splash.requestClose();
		} 
		//logger.debug("Done close request");
		ClassLoader loader = repository.getLoader(artifact,null);
		//logger.debug("Got classloader");
		return loader.loadClass(className);
	}	
}

class SplashScreen extends JWindow {

	private static Log logger = Log.getLogger(SplashScreen.class);
	
	private static class SplashListener implements RepositoryListener {
		
		private final Repository repository;
		private final SplashScreen splash;

		private SplashListener(Repository repository, SplashScreen splash) {
			this.repository = repository;
			this.splash = splash;
		}

		public void statusChanged(Artifact a, ArtifactStatus oldStatus, ArtifactStatus newStatus) {
			splash.setText(a.getArtifactId()+"-"+a.getVersion()+" : "+newStatus.toString());				
			if (newStatus.isError()) {
				//logger.debug(a.toString()+" :: "+newStatus.toString());
			}
			if (! newStatus.equals(ArtifactStatus.JarFetching)) {
				return;
			}
			final DownloadStatus dls;
			try {
				dls = repository.getDownloadStatus(a);
			} catch (RavenException ex) {
				logger.warn("Could not get download status for: " + a, ex);
				return;
			}
			// FIXME: What if there are several artifacts JarFetching at the 
			// same time? Would we get many progressThreads?
			Thread progressThread = new Thread(new Runnable() {
				public void run() {
					while (true) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							logger.warn("Progress thread interrupted", e);
							return;
						}
						int progress = Math.min(100, 
								(dls.getReadBytes() * 100) / dls.getTotalBytes());
						splash.setProgress(progress);
						if (dls.isFinished()) {
							return;
						}
					}
				}
			});
			progressThread.start();			
		}
	}

	private static final long serialVersionUID = -3444472402764959095L;
	private boolean canClose = false;
	private boolean timedOut = false;
	private JProgressBar progress = new JProgressBar();
	private SplashListener listener;

	public SplashScreen(URL imageURL, final int timeout) {
		super();
		ImageIcon image = new ImageIcon(imageURL);
		JLabel label = new JLabel(image);
		getContentPane().add(label, BorderLayout.CENTER);
		progress.setStringPainted(true);
		progress.setString("Initializing");
		//progress.setIndeterminate(true);
		getContentPane().add(progress, BorderLayout.SOUTH);
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension labelSize = label.getPreferredSize();
		setLocation(screenSize.width / 2 - (labelSize.width / 2),
				screenSize.height / 2 - (labelSize.height / 2));

		// If the mouse is clicked then we can close the splash
		// screen once all raven activity has ceased
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent me) {
				requestCloseFromMouse();					
			}
		});

		Runnable waitRunner = new Runnable() {
			public void run() {
				try {
					Thread.sleep(timeout);
				} catch (InterruptedException e) {
					logger.warn("timeout thread interrupted", e);
				}
				requestCloseFromTimeout();
			}
		};

		setVisible(true);
		Thread timerThread = new Thread(waitRunner, "SplashThread");
		timerThread.setDaemon(true);
		timerThread.start();
	}

	/**
	 * Remove listener as set by listenToRepository(Repository repository).
	 *
	 * @see listenToRepository(Repository repository)
	 */
	public void removeListener() {
		if (listener != null) {
			listener.repository.removeRepositoryListener(listener);
			listener = null;
		}
	}

	/**
	 * Add a listener to the repository that will update this
	 * splashscreen. Any old listeners connected to this
	 * splashscreen will be removed. 
	 * Remove the listener with removeListener()
	 * when finished. 
	 * 
	 * @see removeListener()
	 * @param repository
	 */
	public void listenToRepository(Repository repository) {
		removeListener();
		listener = new SplashListener(repository, this);
		repository.addRepositoryListener(listener);
	}

	public void setText(String text) {
		progress.setString(text);
	}

	public void setProgress(int percentage) {
		if (percentage > 0) {
			progress.setValue(percentage);
		} else {
			progress.setValue(0);
		}
	}

	public synchronized void setClosable() {
		canClose = true;
	}

	public synchronized void requestClose() {
		canClose = true;
		if (timedOut) {
			closeMe();
		}
	}

	private synchronized void requestCloseFromTimeout() {
		timedOut = true;		
		if (canClose) {
			closeMe();
		}
	}

	private synchronized void requestCloseFromMouse() {		
		setVisible(false);
		dispose();		
	}

	private synchronized void closeMe() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setVisible(false);
				dispose();
			}
		});
	}
}

