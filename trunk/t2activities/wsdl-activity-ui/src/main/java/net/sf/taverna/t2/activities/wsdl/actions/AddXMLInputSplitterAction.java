package net.sf.taverna.t2.activities.wsdl.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.taverna.t2.activities.wsdl.WSDLActivity;
import net.sf.taverna.t2.activities.wsdl.WSDLActivityConfigurationBean;
import net.sf.taverna.t2.activities.wsdl.xmlsplitter.AddXMLSplitterEdit;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.wsdl.parser.TypeDescriptor;
import net.sf.taverna.wsdl.parser.UnknownOperationException;
import net.sf.taverna.wsdl.parser.WSDLParser;

import org.xml.sax.SAXException;

/**
 * Pops up a {@link JOptionPane} with the names of all the wsdl ports. The one
 * that is selected is added as an input splitter to the currently open dataflow
 * using the {@link AddXMLSplitterEdit}
 * 
 * @author Ian Dunlop
 * 
 */
public class AddXMLInputSplitterAction extends AbstractAction {

	private WSDLActivity activity;
	private JComponent owner;

	public AddXMLInputSplitterAction(Activity<?> activity2, JComponent mainFrame) {
		this.activity = (WSDLActivity) activity2;
		this.owner = mainFrame;

	}

	public void actionPerformed(ActionEvent e) {
		WSDLParser parser = null;
		try {
			WSDLActivityConfigurationBean configuration = activity
					.getConfiguration();
			parser = new WSDLParser(activity.getConfiguration().getWsdl());
		} catch (ParserConfigurationException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (WSDLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (SAXException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		List<TypeDescriptor> inputDescriptors = null;
		try {
			inputDescriptors = parser
					.getOperationInputParameters(((WSDLActivityConfigurationBean) activity
							.getConfiguration()).getOperation());
		} catch (UnknownOperationException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		String[] possibilities = new String[inputDescriptors.size()];
		int i = 0;
		for (TypeDescriptor descriptor : inputDescriptors) {
			possibilities[i] = descriptor.getName();
			i++;
		}
		String s = (String) JOptionPane.showInputDialog(owner,
				"Select the port to add the splitter to",
				"Add input XML splitter", JOptionPane.PLAIN_MESSAGE, null,
				possibilities, possibilities[0]);

		AddXMLSplitterEdit edit = new AddXMLSplitterEdit(FileManager
				.getInstance().getCurrentDataflow(), activity, s, true);
		try {
			edit.doEdit();
		} catch (EditException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
