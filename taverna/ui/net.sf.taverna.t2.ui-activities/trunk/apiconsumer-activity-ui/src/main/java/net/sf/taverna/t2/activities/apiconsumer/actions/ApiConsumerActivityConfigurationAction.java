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
package net.sf.taverna.t2.activities.apiconsumer.actions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;

import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.t2.activities.apiconsumer.ApiConsumerActivity;
import net.sf.taverna.t2.activities.apiconsumer.ApiConsumerActivityConfigurationBean;
import net.sf.taverna.t2.activities.dependencyactivity.AbstractAsynchronousDependencyActivity;
import net.sf.taverna.t2.activities.dependencyactivity.NewArtifactDialog;
import net.sf.taverna.t2.activities.dependencyactivity.AbstractAsynchronousDependencyActivity.ClassLoaderSharing;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import net.sf.taverna.t2.workbench.helper.HelpEnabledDialog;

/**
 * Provides a configurable view for {@link ApiConsumerActivity} through its
 * {@link ApiConsumerActivityConfigurationBean}, where users can set local dependencies for 
 * the API consumer activity and its classloader sharing policy. 
 * 
 * @author Alex Nenadic
 * 
 */
@SuppressWarnings("serial")
public class ApiConsumerActivityConfigurationAction extends ActivityConfigurationAction<ApiConsumerActivity, ApiConsumerActivityConfigurationBean>{

	private final Frame owner;
	
	private HelpEnabledDialog dialog;
	
	// Configuration before any changes have been done in this dialog
	private ApiConsumerActivityConfigurationBean configuration;
	
	// New configuration for classloader sharing
	private ClassLoaderSharing newClassLoaderSharing;
	
	// New configuration for local dependencies
	private LinkedHashSet<String> newLocalDependencies = new LinkedHashSet<String>();

	public static final String CONFIGURE_APICONSUMER_ACTIVITY = "Configure Api Consumer";
		
	public ApiConsumerActivityConfigurationAction(ApiConsumerActivity activity, Frame owner) {
		super(activity);
		putValue(Action.NAME, CONFIGURE_APICONSUMER_ACTIVITY);
		this.owner = owner;
		this.configuration = activity.getConfiguration();

		// Initialise new configuration fields to the values of the previous ones
		newClassLoaderSharing = configuration.getClassLoaderSharing();
		newLocalDependencies.addAll(configuration.getLocalDependencies());
	}
	
	public void actionPerformed(ActionEvent e) {
		
		dialog = new HelpEnabledDialog(owner,
				getRelativeName(),
				true,
				null);
		dialog.setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.PAGE_AXIS));

		// Create panel with classloading options
		JPanel classloadingPanel = new ClassloadingPanel();
		// Create panel for selecting jar files
		JPanel jarFilesPanel = new JarFilesPanel();
		// Create panel with OK/Cancel buttons
        ButtonsPanel buttonsPanel = new ButtonsPanel();
        
		dialog.getContentPane().add(classloadingPanel);
		dialog.add(Box.createRigidArea(new Dimension(0,10)));
		dialog.getContentPane().add(jarFilesPanel);
		dialog.add(Box.createRigidArea(new Dimension(0,10)));
		dialog.getContentPane().add(buttonsPanel);
		
		dialog.getContentPane().setMinimumSize(new Dimension(500,280));
		dialog.getContentPane().setPreferredSize(new Dimension(500,280));
		dialog.pack();
		dialog.setModal(true);
		dialog.setVisible(true);
	}
	
	// Panel containing classloading options
	private class ClassloadingPanel extends JPanel {
		
		// Classloading option 'workflow'
		private static final String WORKFLOW = "Shared for whole workflow";
		// Classloading option 'system'
		private static final String SYSTEM = "System classloader";

		// Combobox with classloading options
		private JComboBox jcbClassloadingOption;
		// Classloading option descriptions
		private HashMap<String, String> classloadingDescriptions;
		// JLabel with classloading option description
		private JLabel jlClassloadingDescription;

		// Panel containing a list of possible classloading options which users can select from
		private ClassloadingPanel(){
			super(new GridBagLayout());
			jcbClassloadingOption = new JComboBox(
					new String[] {WORKFLOW,SYSTEM});
			// Set the current classlaoding option based on the previous configuration

			if (configuration.getClassLoaderSharing() == (AbstractAsynchronousDependencyActivity.ClassLoaderSharing.workflow)){
				jcbClassloadingOption.setSelectedItem(WORKFLOW);
			}
			else if (configuration.getClassLoaderSharing() == (AbstractAsynchronousDependencyActivity.ClassLoaderSharing.system)){
				jcbClassloadingOption.setSelectedItem(SYSTEM);
			}
			
			jcbClassloadingOption.addActionListener(new ActionListener(){
				// Fires up when combobox selection changes
				public void actionPerformed(ActionEvent e) {
					jlClassloadingDescription.setText(classloadingDescriptions
							.get(((JComboBox) e.getSource()).getSelectedItem()));
					if (((JComboBox) e.getSource()).getSelectedItem().equals(
							WORKFLOW)) {
						newClassLoaderSharing = AbstractAsynchronousDependencyActivity.ClassLoaderSharing.workflow;
					}
					else if (((JComboBox) e.getSource()).getSelectedItem().equals(
							SYSTEM)) {
						newClassLoaderSharing = AbstractAsynchronousDependencyActivity.ClassLoaderSharing.system;
					}
				}
			});
			
			classloadingDescriptions = new HashMap<String, String>();
			classloadingDescriptions.put(WORKFLOW, "<html><small>"
					+ "Classes are shared across the whole workflow (with any processor<br>"
					+ "also selecting this option), but are reinitialised for each workflow run.<br>"
					+ "This might be needed if a processor passes objects to another, or <br>"
					+ "state is shared within static members of loaded classes."
					+ "</small></html>");
			classloadingDescriptions.put(SYSTEM, "<html><small><p>"
					+ "The (global) system classloader is used, any dependencies defined here are<br>"
					+ "made available globally on the first run. Note that if you are NOT using<br>"
					+ "the defaulf Taverna BootstrapClassLoader, any settings here will be disregarded."
					+ "</p><p>"
					+ "This option is mainly useful if you are using JNI-based libraries. Note that <br>"
					+ "for JNI you also have to specify <code>-Djava.library.path</code> and <br>"
					+ "probably your operating system's dynamic library search path<br>"
					+ "<code>LD_LIBRARY_PATH</code> / <code>DYLD_LIBRARY_PATH</code> / <code>PATH</code> </p></small></html>");
			
			// Set the current classlaoding description based on the item selected in the combobox
			jlClassloadingDescription = new JLabel(classloadingDescriptions
					.get(jcbClassloadingOption.getSelectedItem()));
			
			// Add components to the ClassloadingPanel
			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			c.insets = new Insets(10,0,0,0);
			add(new JLabel("Classloader persistence"), c);
			c.insets = new Insets(0,0,0,0);
			add(jcbClassloadingOption, c);
			c.insets = new Insets(0,30,0,0);
			add(jlClassloadingDescription, c);

		}
	}
	
	// Panel containing a list of jar files which users can select from
	private class JarFilesPanel extends JPanel {
		private JLabel warning =
			new JLabel(
				"<html><center<font color='red'>"
					+ "Warning: Depending on local libraries makes this workflow<br>"
					+ "difficult or impossible to run for other users. Try depending<br>"
					+ "on artifacts from a public repository if possible.</font></center></html>");

		private JarFilesPanel() {
			super();
			setMinimumSize(new Dimension(490, 180));
			setMaximumSize(new Dimension(490, 230));

			setLayout(new BorderLayout());
			setBorder(new EmptyBorder(0,10,0,10));
			
			JPanel labelPanel = new JPanel();
			labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.PAGE_AXIS));
			JLabel label = new JLabel("Local JAR files");
			if (!ApiConsumerActivity.libDir.exists())
				ApiConsumerActivity.libDir.mkdir();
			JLabel libLabel = new JLabel("<html><small>" + ApiConsumerActivity.libDir.getAbsolutePath()
					+ "</small></html>");
			labelPanel.add(label);
			labelPanel.add(libLabel);

			add(labelPanel, BorderLayout.NORTH);
			add(new JScrollPane(jarFiles(),
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

			warning.setVisible(false);
			// We'll skip the warning until we actually have support 
			// for artifacts
			//add(warning);
			updateWarning();
		}

		private void updateWarning() {
			// Show warning if there is any local dependencies
			warning.setVisible(!configuration.getLocalDependencies().isEmpty());
		}

		public JPanel jarFiles() {
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

			// Make so it's there so the user can add stuff to it
			// List of all jar files in the lib directory
			List<String> jarFiles =
				Arrays.asList(ApiConsumerActivity.libDir.list(new ApiConsumerActivity.FileExtFilter(".jar")));
			// We also add the list of jars that may have been configured sometime before
			// but are now not present in the lib directory for some reason
			Set<String> missingLocalDeps =
				new HashSet<String>(configuration.getLocalDependencies());
			missingLocalDeps.removeAll(jarFiles);
			// jarFiles and missingLocalDeps now contain two sets of files that do not intersect
			List<String> jarFilesList = new ArrayList<String>();
			// Put them all together
			jarFilesList.addAll(jarFiles);
			jarFilesList.addAll(missingLocalDeps);		
			Collections.sort(jarFilesList);
						
			if (jarFilesList.isEmpty()) {
				panel.add(new JLabel("<html><small>To depend on a JAR file, "
					+ "copy it to the above-mentioned folder.</small></html>"));
				return panel;
			}

			for (String jarFile : jarFilesList) {
				JCheckBox checkBox = new JCheckBox(jarFile);
				// Has it already been selected in some previous configuring?
				checkBox.setSelected(configuration.getLocalDependencies().contains(jarFile));
				checkBox.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent e) {
						JCheckBox box = (JCheckBox) e.getSource();
						if (e.getStateChange() == ItemEvent.SELECTED) {
							newLocalDependencies.add(box.getText());
						} else if (e.getStateChange() == ItemEvent.DESELECTED) {
							newLocalDependencies.remove(box.getText());
						}
						updateWarning();
					}
				});
				panel.add(checkBox);
				// The jar may not be in the lib directory, so warn the user
				if (!new File(ApiConsumerActivity.libDir, jarFile).exists()) {
					checkBox.setForeground(Color.RED);
					checkBox.setText(checkBox.getText() + " (missing file!)");
				}
			}
			return panel;
		}
	}
	
	private class ButtonsPanel extends Panel{
		
		private ButtonsPanel(){
			super(new FlowLayout(FlowLayout.RIGHT));
			
			// 'OK'  button
	        JButton jbOK = new JButton("OK");
	        jbOK.addActionListener(new ActionListener()
	        {
	            public void actionPerformed(ActionEvent evt)
	            {	            			            	
	            	// Set the configuration bean to reflect any changes done in the dialog
					if (isConfigurationChanged()) {
						configuration.setClassLoaderSharing(newClassLoaderSharing);
						configuration.setLocalDependencies(newLocalDependencies);
						configuration.setArtifactDependencies(new LinkedHashSet<BasicArtifact>());
		            	configureActivity(configuration);
					}
	            	dialog.setVisible(false);
	            	dialog.dispose();     
	            }

				private boolean isConfigurationChanged() {
					// The only things that can be configured are local and artifact dependencies and 
					// classloader sharing policy, so we check if these have changed
					if (configuration.getClassLoaderSharing()
									.equals(newClassLoaderSharing)
							&& configuration.getLocalDependencies()
									.equals(newLocalDependencies)) {
						return false;
					}
					else {
						return true;
					}
				}
	        });
	        // 'Cancel' button
	        JButton jbCancel = new JButton("Cancel");
	        jbCancel.addActionListener(new ActionListener()
	        {
	            public void actionPerformed(ActionEvent evt)
	            {
	            	dialog.setVisible(false);
	            	dialog.dispose();         
	            }
	        });
	        add(jbOK);
	        add(jbCancel);
		}
	}
	
}
