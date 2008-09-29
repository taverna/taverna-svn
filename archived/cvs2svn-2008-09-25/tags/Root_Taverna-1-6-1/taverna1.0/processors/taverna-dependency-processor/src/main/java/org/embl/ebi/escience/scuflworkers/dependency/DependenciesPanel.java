package org.embl.ebi.escience.scuflworkers.dependency;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.sf.taverna.raven.repository.BasicArtifact;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scuflworkers.dependency.DependencyProcessor.ClassLoaderSharing;

/**
 * Panel for specifying dependencies. Usable for {@link DependencyProcessor}s,
 * such as the
 * {@link org.embl.ebi.escience.scuflworkers.beanshell.BeanshellProcessor;}.
 * 
 * @author Stian Soiland
 */
public class DependenciesPanel extends JPanel {

	private static Logger logger = Logger.getLogger(DependenciesPanel.class);

	DependencyProcessor processor;

	ClassloaderOptions classLoaderOptions;
	
	JarFiles jarFilesPanel;
	
	Artifacts artifactsPanel;
	
	public DependenciesPanel(DependencyProcessor processor) {
		super(new GridBagLayout());
		this.processor = processor;
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.gridx = 0;
		c.weighty = 0.3;
		artifactsPanel = new Artifacts();
		jarFilesPanel = new JarFiles();
		
		classLoaderOptions = new ClassloaderOptions();
		add(classLoaderOptions, c);
		add(jarFilesPanel, c);
		add(artifactsPanel, c);
	}
	


	public class ClassloaderOptions extends JPanel implements ActionListener {
		private static final String FRESH = "Always fresh";

		private static final String ITERATION = "Shared over iteration";

		private static final String WORKFLOW = "Shared for whole workflow";
		
		private static final String SYSTEM = "System classloader";

		private JLabel description = new JLabel();;

		public ClassloaderOptions() {
			super(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			c.insets = new Insets(10,0,0,0);
			add(new JLabel("Classloader persistence"), c);
			c.insets = new Insets(0,0,0,0);
			JComboBox classLoaderPersistence =
				new JComboBox(new String[] { FRESH, ITERATION, WORKFLOW, SYSTEM });
			classLoaderPersistence.addActionListener(this);
			ClassLoaderSharing sharing = processor.getClassLoaderSharing();
			if (sharing == ClassLoaderSharing.fresh) {
				classLoaderPersistence.setSelectedItem(FRESH);
			} else if (sharing == ClassLoaderSharing.iteration) {
				classLoaderPersistence.setSelectedItem(ITERATION);
			} else if (sharing == ClassLoaderSharing.workflow) {
				classLoaderPersistence.setSelectedItem(WORKFLOW);
			} else if (sharing == ClassLoaderSharing.system) {
				classLoaderPersistence.setSelectedItem(SYSTEM);
			} else {
				logger.error("Unknown classloader sharing: " + sharing);
			}
			add(classLoaderPersistence, c);
			c.insets = new Insets(0,30,0,0);
			add(description, c);
		}

		// For classLoaderPersistence changes
		public void actionPerformed(ActionEvent e) {
			JComboBox cb = (JComboBox) e.getSource();
			String classLoaderStyle = (String) cb.getSelectedItem();			
			if (classLoaderStyle.equals(FRESH)) {
				description.setText("<html><small>"
					+ "Classes will be reloaded for each processor and each invocation; <br>"
					+ "state will not even be shared across iterations. This can be slow, <br>"
					+ "but ensures each iteration starts from freshly initialised classes."
					+ "</small></html>");
				processor.setClassLoaderSharing(ClassLoaderSharing.fresh);
			} else if (classLoaderStyle.equals(ITERATION)) {
				description.setText("<html><small>"
					+ "Classes will be reloaded for each processor and workflow run, but<br>"
					+ "will remain the same throughout an iteration. This is the recommended<br>"
					+ "option for most cases as processors won't interfere with each other."
					+ "</small></html>");
				processor.setClassLoaderSharing(ClassLoaderSharing.iteration);
			} else if (classLoaderStyle.equals(WORKFLOW)) {
				description.setText("<html><small>"
					+ "Classes are shared across the whole workflow (with any processor<br>"
					+ "also selecting this option), but are reinitialised for each workflow run.<br>"
					+ "This might be needed if a processor passes objects to another, or <br>"
					+ "state is shared within static members of loaded classes."
					+ "</small></html>");
				processor.setClassLoaderSharing(ClassLoaderSharing.workflow);
			} else if (classLoaderStyle.equals(SYSTEM)) {
				description.setText("<html><small>"
					+ "The (global) system classloader is used, dependencies defined here are<br>"
					+ "made available globally on first run."
					+ "<p>"
					+ "This is mainly useful if you are using JNI-based libraries. <br>"
					+ "Note that for JNI you also have to specify <code>-Djava.library.path</code><br>"
					+ "and probably your operating system's dynamic library search path<br>"
					+ "<code>LD_LIBRARY_PATH</code> / <code>DYLD_LIBRARY_PATH</code> / <code>PATH</code> </small></html>");
				processor.setClassLoaderSharing(ClassLoaderSharing.system);
			} else {
				logger.error("Selected unknown classloader style: "
					+ classLoaderStyle);
				description.setText("<html><font color='red'>"
					+ "Unknown classloader style</font></html>");
			}
		}
	}

	public class JarFiles extends JPanel {
		private JLabel warning =
			new JLabel(
				"<html><center<font color='red'>"
					+ "Warning: Depending on local libraries makes this workflow<br>"
					+ "difficult or impossible to run for other users. Try depending<br>"
					+ "on artefacts from a public repository if possible.</font></center></html>");

		public JarFiles() {
			super(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			add(new JLabel("Local JAR files"), c);
			add(new JLabel("<html><small><code>" + processor.libDir.getAbsolutePath()
				+ "</code></small></html>"), c);
			c.fill = GridBagConstraints.BOTH;
			c.weightx = 0.2;
			add(new JScrollPane(jarFiles(),
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), c);

			c.fill = GridBagConstraints.VERTICAL;
			c.anchor = GridBagConstraints.CENTER;
			warning.setVisible(false);
			// We'll skip the warning until we actually have support 
			// for artefacts
			//add(warning, c);
			updateWarning();
		}

		private void updateWarning() {
			// Show warning if there is any local dependencies
			warning.setVisible(!processor.localDependencies.isEmpty());

		}

		public JPanel jarFiles() {
			JPanel panel = new JPanel(new GridBagLayout());

			List<String> files =
				Arrays.asList(processor.libDir.list(new FileExtFilter(".jar")));
			Set<String> missing =
				new HashSet<String>(processor.localDependencies);
			missing.removeAll(files);

			List<String> allJars = new ArrayList<String>();
			allJars.addAll(files);
			allJars.addAll(missing);		
			Collections.sort(allJars);

			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			if (allJars.isEmpty()) {
				panel.add(new JLabel("<html><small>To depend on a JAR file, "
					+ "copy it to the above-mentioned folder.</small></html>"), c);
				return panel;
			}

			c.gridy = -1;
			for (String jarFile : allJars) {
				c.gridy++;
				JCheckBox checkBox = new JCheckBox(jarFile);
				checkBox.setSelected(processor.localDependencies.contains(jarFile));
				checkBox.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent e) {
						JCheckBox box = (JCheckBox) e.getSource();
						if (e.getStateChange() == ItemEvent.SELECTED) {
							processor.localDependencies.add(box.getText());
						} else if (e.getStateChange() == ItemEvent.DESELECTED) {
							processor.localDependencies.remove(box.getText());
						}
						updateWarning();
					}
				});
				panel.add(checkBox, c);
				// It might exist, but was not named .jar
				if (!new File(processor.libDir, jarFile).exists()) {
					checkBox.setForeground(Color.RED);
					c.gridx = 1;
					panel.add(new JLabel("Missing file"), c);
					c.gridx = 0;
				}
			}
			return panel;
		}
	}

	public class Artifacts extends JPanel {
		public Artifacts() {
			super(new GridBagLayout());
			if (processor.artifactDependencies.isEmpty()) {
				return; // Don't show anything
			}
			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			c.gridy = GridBagConstraints.RELATIVE;
			add(new JLabel("<html><b>Artifacts:</b></html>"), c);
			for (BasicArtifact artifact : processor.artifactDependencies) {
				add(new JLabel(artifact.toString()), c);
			}
		}
	}
	
	public class FileExtFilter implements FilenameFilter {

		public FileExtFilter(String ext) {
			this.ext = ext;
		}

		String ext = null;

		public boolean accept(File dir, String name) {
			return name.endsWith(ext);
		}
	}
}
