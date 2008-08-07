package net.sf.taverna.t2.dataflow.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.prefs.Preferences;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.activities.dataflow.views.DataflowObserver;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;
import net.sf.taverna.t2.workflowmodel.serialization.xml.DataflowXMLDeserializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializerImpl;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class DataflowActivityConfigurationAction extends
		ActivityConfigurationAction<DataflowActivity> {

	private static Logger logger = Logger
			.getLogger(DataflowActivityConfigurationAction.class);

	private final Component owner;

	private File selectedFile;

	private Processor processor;

	public DataflowActivityConfigurationAction(DataflowActivity activity,
			JComponent owner) {
		super(activity);
		this.owner = owner;
	}

	/**
	 * Pop up a {@link JFileChooser} and let the user select a {@link Dataflow}
	 * to be opened. Deserialise it when selected, do the edits to add it to the
	 * current dataflow and get eh {@link FileManager} to open it in the GUI
	 */
	public void actionPerformed(ActionEvent e) {

		if (selectDataflow()) {
			Dataflow dataflow = deserialiseSelectedDataflow();
			try {
				getActivity().configure(dataflow);
			} catch (ActivityConfigurationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			addSelectedDataflowToCurrentDataflow(dataflow);
			FileManager.getInstance().openDataflow(dataflow);
		}

	}

	/**
	 * Use the T2 deserialiser to turn the {@link #selectedFile} dataflow File
	 * into a T2 dataflow object
	 * 
	 * @return
	 */
	private Dataflow deserialiseSelectedDataflow() {
		XMLDeserializer xmlDeserializer = new XMLDeserializerImpl();
		DataflowXMLDeserializer deserializer = DataflowXMLDeserializer
				.getInstance();
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(selectedFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SAXBuilder builder = new SAXBuilder();
		Element rootElement = null;
		try {
			rootElement = builder.build(inputStream).getRootElement();
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			return xmlDeserializer.deserializeDataflow(rootElement);
		} catch (EditException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DeserializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Adds the selected dataflow to the currently opened one. Create a
	 * processor with the same name as the nested dataflow (ie. the one just
	 * opened). Add the configured dataflow activity to this processor. Then use
	 * the {@link EditManager} to add the processor to the main dataflow so that
	 * any GUI updates are forced
	 * 
	 * @param dataflow
	 */
	private void addSelectedDataflowToCurrentDataflow(Dataflow dataflow) {
		processor = EditsRegistry.getEdits().createProcessor(
				dataflow.getLocalName());
		try {
			if (!processor.getActivityList().contains(getActivity())) {
				EditsRegistry.getEdits().getAddActivityEdit(processor,
						getActivity()).doEdit();
				DataflowObserver.getInstance().addDataflowObservers(getActivity(),
						processor);
			}
			EditManager.getInstance().doDataflowEdit(
					FileManager.getInstance().getCurrentDataflow(),
					EditsRegistry.getEdits()
							.getMapProcessorPortsForActivityEdit(processor));

		} catch (EditException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Get the user to select a t2flow (ie a serialised T2 dataflow) to add as a
	 * nested dataflow
	 * 
	 * @return
	 */
	private boolean selectDataflow() {
		JFileChooser fileChooser = new JFileChooser();
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		String curDir = prefs
				.get("currentDir", System.getProperty("user.home"));
		fileChooser.setDialogTitle("Select dataflow.....");

		fileChooser.resetChoosableFileFilters();

		FileFilter filter = new FileFilter() {

			@Override
			public boolean accept(File f) {
				return f.getName().endsWith(".t2flow");
			}

			@Override
			public String getDescription() {
				return "T2 dataflow";
			}

		};

		fileChooser.addChoosableFileFilter(filter);

		fileChooser.setFileFilter(filter);

		fileChooser.setCurrentDirectory(new File(curDir));
		fileChooser.setMultiSelectionEnabled(false);

		int returnVal = fileChooser.showOpenDialog(owner);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			prefs.put("currentDir", fileChooser.getCurrentDirectory()
					.toString());
			selectedFile = fileChooser.getSelectedFile();
			return true;
		}
		return false;

	}

}
