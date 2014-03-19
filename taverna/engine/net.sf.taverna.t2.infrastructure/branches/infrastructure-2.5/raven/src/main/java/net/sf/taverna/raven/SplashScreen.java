/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: SplashScreen.java,v $
 * Revision           $Revision: 1.8 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2008/09/18 10:37:45 $
 *               by   $Author: stain $
 * Created on 18 Jan 2007
 *****************************************************************/
package net.sf.taverna.raven;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import net.sf.taverna.raven.log.Log;
import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactStatus;
import net.sf.taverna.raven.repository.DownloadStatus;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.RepositoryListener;

public class SplashScreen extends JFrame {

	private static Log logger = Log.getLogger(SplashScreen.class);

	private static SplashScreen splashscreen;

	private static final long serialVersionUID = -3444472402764959095L;

	public static SplashScreen getSplashScreen() {
		return splashscreen;
	}

	public static SplashScreen getSplashScreen(URL imageUrl) {
		if (splashscreen == null) {
			splashscreen = new SplashScreen(imageUrl);
		}
		return splashscreen;
	}

	public static SplashScreen getSplashScreen(URL imageUrl, int timeout) {
		if (splashscreen == null) {
			splashscreen = new SplashScreen(imageUrl, timeout);
		}
		return splashscreen;
	}

	private boolean canClose = false;
	private boolean timedOut = false;
	private JProgressBar progress = new JProgressBar();
	private SplashListener listener;
	private SplashScreen(URL imageURL) {
		this(imageURL, 0);
	}

	private SplashScreen(URL imageURL, final int timeout) {
		super();
		setUndecorated(true);
		ImageIcon image = new ImageIcon(imageURL);
		JLabel label = new JLabel(image);
		getContentPane().add(label, BorderLayout.CENTER);
		progress.setStringPainted(true);
		progress.setString("Initializing...");
		// progress.setIndeterminate(true);
		getContentPane().add(progress, BorderLayout.SOUTH);
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension labelSize = label.getPreferredSize();
		setLocation(screenSize.width / 2 - (labelSize.width / 2),
				screenSize.height / 2 - (labelSize.height / 2));

		// If the mouse is clicked then we can close the splash
		// screen once all raven activity has ceased
	/*	addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent me) {
				requestCloseFromMouse();
			}
		});*/

		if (timeout > 0) {

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

			Thread timerThread = new Thread(waitRunner, "SplashThread");
			timerThread.setDaemon(true);
			timerThread.start();
		} else {
			timedOut = true;
		}
		setAlwaysOnTop(false);
		setVisible(true);

	}

	/**
	 * Add a listener to the repository that will update this splashscreen. Any
	 * old listeners connected to this splashscreen will be removed. Remove the
	 * listener with removeListener() when finished.
	 * 
	 * @see removeListener()
	 * @param repository
	 */
	public void listenToRepository(Repository repository) {
		removeListener();
		listener = new SplashListener(repository, this);
		repository.addRepositoryListener(listener);
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

	public synchronized void requestClose() {
		canClose = true;
		if (timedOut) {
			closeMe();
		}
	}

	public synchronized void setClosable() {
		canClose = true;
	}

	public void setProgress(int percentage) {
		if (percentage > 0) {
			progress.setValue(percentage);
		} else {
			progress.setValue(0);
		}
	}

	public void setText(String text) {
		progress.setString(text);
	}

	private synchronized void closeMe() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setVisible(false);
				dispose();
				splashscreen = null;
			}
		});
	}

	private synchronized void requestCloseFromMouse() {
		setClosable();
		timedOut = true;
		closeMe();
	}

	private synchronized void requestCloseFromTimeout() {
		timedOut = true;
		if (canClose) {
			closeMe();
		}
	}

	private static class SplashListener implements RepositoryListener {

		private final Repository repository;
		private final SplashScreen splash;

		private SplashListener(Repository repository, SplashScreen splash) {
			this.repository = repository;
			this.splash = splash;
		}

		public void statusChanged(Artifact a, ArtifactStatus oldStatus,
				ArtifactStatus newStatus) {
			splash.setText(a.getArtifactId() + "-" + a.getVersion() + " : "
					+ newStatus.toString());
			if (newStatus.isError()) {
				// logger.debug(a.toString()+" :: "+newStatus.toString());
			}
			if (!newStatus.equals(ArtifactStatus.JarFetching)) {
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
						int progress = Math.min(100, (dls.getReadBytes() * 100)
								/ dls.getTotalBytes());
						splash.setProgress(progress);
						if (dls.isFinished()) {
							return;
						}
					}
				}
			}, "Splashscreen progress bar");
			progressThread.start();
		}
	}
}
