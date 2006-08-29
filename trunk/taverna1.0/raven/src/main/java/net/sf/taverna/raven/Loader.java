package net.sf.taverna.raven;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;


import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.raven.repository.ArtifactStatus;
import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.raven.repository.DownloadStatus;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.RepositoryListener;
import net.sf.taverna.raven.repository.impl.LocalRepository;

/**
 * Bootstrap a raven installation, allows a non raven aware
 * loader to create a repository structure etc and boot into
 * an artifact class loader driven world view without having
 * to actually have raven installed to begin with. Also handles
 * splash screen management and a few other conveniences.
 * @author Tom Oinn
 */
public class Loader {
	
	/**
	 * Initialize raven's repository, show a splash screen and load the specified
	 * artifact into it. Returns the named class from that specified artifact.
	 * @param ravenVersion The version of Raven to install or check in the local repository
	 * @param localRepositoryLocation Directory on disk to use as a local cache for m2 artifacts
	 * @param remoteRepositories array of URLs to remote Maven2 repositories
	 * @param splashScreenURL URL to a splash screen image of a form a JLabel can use
	 * @param targetGroup Group ID of the Maven2 artifact to bootstrap from
	 * @param targetArtifact Artifact ID of the Maven2 artifact to bootstrap from
	 * @param targetVersion Version of the Maven2 artifact to bootstrap from
	 * @param splashTime Time in milliseconds to display the splashscreen, splashscreen is
	 * displayed while either this timeout is not exceeded or there is any raven activity.
	 * @param className Class name to return from the target artifact
	 * @return Class object loaded by raven as specified by the arguments to this method call
	 * @throws ArtifactNotFoundException
	 * @throws ArtifactStateException
	 * @throws ClassNotFoundException
	 */
	public static Class doRavenMagic(String ravenVersion, 
			File localRepositoryLocation, 
			URL[] remoteRepositories,
			URL splashScreenURL,
			String targetGroup,
			String targetArtifact,
			String targetVersion,
			int splashTime,
			String className) throws ArtifactNotFoundException, ArtifactStateException, ClassNotFoundException {
		
		final SplashScreen splash = 
			new SplashScreen(splashScreenURL,splashTime);
		
		final Repository repository = 
			LocalRepository.getRepository(localRepositoryLocation);
		
		for (URL remoteLocation : remoteRepositories) {
			repository.addRemoteRepository(remoteLocation);
		}
		Artifact artifact = new BasicArtifact(targetGroup, targetArtifact, targetVersion);
		repository.addArtifact(artifact);
		repository.addArtifact(new BasicArtifact("uk.org.mygrid.taverna.raven","raven",ravenVersion));
		RepositoryListener listener = new RepositoryListener() {
			public void statusChanged(Artifact a, ArtifactStatus oldStatus, ArtifactStatus newStatus) {
				splash.setText(a.getArtifactId()+"-"+a.getVersion()+" : "+newStatus.toString());				
				if (newStatus.isError()) {
					//System.out.println(a.toString()+" :: "+newStatus.toString());
				}
				if (newStatus.equals(ArtifactStatus.JarFetching)) {
					try {
						final DownloadStatus dls = repository.getDownloadStatus(a);
						new Thread(new Runnable() {
							public void run() {
								//System.out.println("Progress thread starting");
								boolean running = true;
								try {
									while (running) {
										Thread.sleep(100);
										splash.setProgress((dls.getReadBytes() * 100) / dls.getTotalBytes());
										if (dls.getReadBytes() == dls.getTotalBytes()) {
											running = false;
										}
									}
									//System.out.println("Progress bar thread finished");
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									//e.printStackTrace();
									//System.out.println("Progress bar thread interrupted");
								}
							}
						}).start();
					} catch (ArtifactStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ArtifactNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}			
		};
		repository.addRepositoryListener(listener);
		repository.update();
		repository.removeRepositoryListener(listener);
		splash.setText("Done...");
		splash.requestClose();
		//System.out.println("Done close request");
		ClassLoader loader = repository.getLoader(artifact,null);
		//System.out.println("Got classloader");
		return Class.forName(className,true,loader);
	}
	
	private static class SplashScreen extends JWindow {
		
		private static final long serialVersionUID = -3444472402764959095L;
		private boolean canClose = false;
		private boolean timedOut = false;
		private JProgressBar progress = new JProgressBar();
		
		public void setText(String text) {
			progress.setString(text);
		}
		
		public void setProgress(int percentage) {
			if (percentage > 0) {
				//progress.setIndeterminate(false);
				progress.setValue(percentage);
			}
			else {
				progress.setValue(0);
			}
		}
		
		public synchronized void setClosable() {
			this.canClose = true;
		}
		
		public synchronized void requestClose() {
			canClose = true;
			//progress.setIndeterminate(false);
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
			timedOut = true;
			if (canClose) {
				setVisible(false);
				dispose();
			}
		}
		
		private synchronized void closeMe() {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					setVisible(false);
					dispose();
				}
			});
		}
		
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
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					requestCloseFromTimeout();
				}
			};

			setVisible(true);
			Thread timerThread = new Thread(waitRunner, "SplashThread");
			timerThread.setDaemon(true);
			timerThread.start();
		}
		
	}
	
}
