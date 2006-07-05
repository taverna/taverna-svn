/**
 * 
 */
package net.sourceforge.taverna.scuflui.workbench;

import javax.swing.JButton;
import javax.swing.JToolBar;

import net.sourceforge.taverna.scuflui.actions.ImportWorkflowAction;
import net.sourceforge.taverna.scuflui.actions.OpenWorkflowAction;
import net.sourceforge.taverna.scuflui.actions.OpenWorkflowFromWebAction;
import net.sourceforge.taverna.scuflui.actions.RunWorkflowAction;


/**
 * 
 * @author Mark
 *
 */
public class Toolbar extends JToolBar {
	
	public Toolbar(){
		this.add(new JButton(new OpenWorkflowAction()));
		this.add(new JButton(new OpenWorkflowFromWebAction()));
		this.add(new JButton(new ImportWorkflowAction()));
		this.add(new JButton(new RunWorkflowAction()));
	}

}
