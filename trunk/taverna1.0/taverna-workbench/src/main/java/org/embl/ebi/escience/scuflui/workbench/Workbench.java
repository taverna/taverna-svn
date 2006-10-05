package org.embl.ebi.escience.scuflui.workbench;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.embl.ebi.escience.scuflui.spi.UIComponentFactorySPI;

import net.sf.taverna.raven.Loader;
import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalRepository;
import net.sf.taverna.raven.repository.impl.LocalRepository.ArtifactClassLoader;
import net.sf.taverna.zaria.ZBasePane;
import net.sf.taverna.zaria.raven.ArtifactDownloadDialog;

/**
 * Top level Zaria based UI for Taverna
 * @author Tom Oinn
 */
public class Workbench extends JFrame {
	
	private ZBasePane basePane = null;
	
	public static Workbench getWorkbench() {
		return new Workbench();
	}
	
	/**
	 * Construct a new Workbench instance with the underlying Raven
	 * repository pointing to the given directory on disc.
	 * @param localRepositoryLocation
	 */
	private Workbench() {
		super();
		try {
			UIManager.setLookAndFeel(
					"de.javasoft.plaf.synthetica.SyntheticaStandardLookAndFeel");
		}
		catch (Exception ex) {
			// Look and feel not available
		}
		/**
		 * Create and configure the ZBasePane
		 */
		basePane = new ZBasePane() {
			@Override
			public JMenuItem getMenuItem(Class theClass) {
				try {
					//System.out.println(UIComponentFactorySPI.class.getClassLoader());
					//System.out.println(theClass.getClassLoader());
					UIComponentFactorySPI factory = 
						(UIComponentFactorySPI) theClass.newInstance();
					Icon icon = factory.getIcon();
					if (icon != null) {
						return new JMenuItem(factory.getName(), factory.getIcon());
					}
					else {
						return new JMenuItem(factory.getName());
					}
				} catch (InstantiationException e) {
					return new JMenuItem("Instantiation exception!");
				} catch (IllegalAccessException e) {
					return new JMenuItem("Illegal access exception!");
				}
			}
			@Override
			public JComponent getComponent(Class theClass) {
				UIComponentFactorySPI factory;
				try {
					factory = (UIComponentFactorySPI) theClass.newInstance();
					return (JComponent)factory.getComponent();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return new JPanel();
			}
			
		};
		ArtifactClassLoader acl = 
			(ArtifactClassLoader)getClass().getClassLoader();
		basePane.setRepository(acl.getRepository());
		basePane.setKnownSPINames(new String[]{
				"org.embl.ebi.escience.scuflui.spi.UIComponentFactorySPI"});
		basePane.setEditable(true);
		setUI();
	}
	
	public void setUI() {
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(basePane, BorderLayout.CENTER);
		JMenuBar menuBar = new JMenuBar();
		JMenu ravenMenu = new JMenu("Raven...");
		menuBar.add(ravenMenu);
		JMenuItem getArtifact = new JMenuItem("Download artifact...");
		ravenMenu.add(getArtifact);
		final String[] groups = new String[] {
				"uk.org.mygrid.taverna.scufl.scufl-ui-components",
				"uk.org.mygrid.taverna.scuflui"
		};
		final String[] versions = new String[] {
				"1.5-SNAPSHOT"
		};
		getArtifact.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Artifact a = ArtifactDownloadDialog.showDialog(Workbench.this, null, "Download new artifact", "Raven downloader", groups, versions);
				if (a != null) {
					addArtifact(a);
				}
			}
		});
		setJMenuBar(menuBar);
		setSize(new Dimension(500,500));
		setVisible(true);
		addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
				System.exit(0);
			}
		});
		updateRepository();
	}
	
	public synchronized void addArtifact(Artifact a) {
		basePane.getRepository().addArtifact(a);
		updateRepository();
	}
	
	public synchronized void updateRepository() {
		basePane.lockFrame();
		basePane.getRepository().update();
		basePane.unlockFrame();
	}
}