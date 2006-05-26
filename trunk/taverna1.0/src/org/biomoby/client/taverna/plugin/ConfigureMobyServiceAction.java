/*
 * This file is a component of the Taverna project, and is licensed under the
 * GNU LGPL. Copyright Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.axis.wsdl.symbolTable.Parameters;
import org.biomoby.client.CentralImpl;
import org.biomoby.client.taverna.plugin.BiomobyProcessor;
import org.biomoby.service.dashboard.data.ParametersTable;
import org.biomoby.shared.Central;
import org.biomoby.shared.MobyData;
import org.biomoby.shared.MobyDataType;
import org.biomoby.shared.MobyException;
import org.biomoby.shared.MobyNamespace;
import org.biomoby.shared.MobyPrimaryDataSet;
import org.biomoby.shared.MobyPrimaryDataSimple;
import org.biomoby.shared.NoSuccessException;
import org.embl.ebi.escience.scufl.DataConstraint;
import org.embl.ebi.escience.scufl.DataConstraintCreationException;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.UnknownPortException;
import org.embl.ebi.escience.scuflui.ScuflUIComponent;
import org.embl.ebi.escience.scuflui.UIUtils;
import org.embl.ebi.escience.scuflui.processoractions.AbstractProcessorAction;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * 
 * @author Eddie An action that for BioMobyProcessors
 */
public class ConfigureMobyServiceAction extends AbstractProcessorAction {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflui.processoractions.AbstractProcessorAction#getComponent(org.embl.ebi.escience.scufl.Processor)
	 */

	public JComponent getComponent(Processor processor) {
		// variables i need
		BiomobyProcessor bp = (BiomobyProcessor) processor;

		String serviceName = bp.getMobyService().getName();
		String description = bp.getDescription();
		return bp.getParameterTable().scrollable();
	}

	public void frameClosing() {
		super.frameClosing();
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflui.processoractions.ProcessorActionSPI#canHandle(org.embl.ebi.escience.scufl.Processor)
	 */
	public boolean canHandle(Processor processor) {
		if (processor instanceof BiomobyProcessor) {
			if (((BiomobyProcessor) processor).containsSecondaries())
				return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflui.processoractions.ProcessorActionSPI#getDescription()
	 */
	public String getDescription() {
		return "Configure Moby Service";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflui.processoractions.ProcessorActionSPI#getIcon()
	 */
	public ImageIcon getIcon() {
		Class cls = this.getClass();
		URL url = cls.getClassLoader().getResource(
				"org/biomoby/client/taverna/plugin/moby_small.gif");
		return new ImageIcon(url);
	}

	/**
	 * returns the frame size as a dimension for the content pane housing this
	 * action
	 */
	public Dimension getFrameSize() {
		return new Dimension(450, 450);
	}

	/**
	 * Return an Icon to represent this action
	 * 
	 * @param loc
	 *            the location of the image to use as an icon
	 */
	public ImageIcon getIcon(String loc) {
		Class cls = this.getClass();
		URL url = cls.getClassLoader().getResource(loc);
		return new ImageIcon(url);
	}
}