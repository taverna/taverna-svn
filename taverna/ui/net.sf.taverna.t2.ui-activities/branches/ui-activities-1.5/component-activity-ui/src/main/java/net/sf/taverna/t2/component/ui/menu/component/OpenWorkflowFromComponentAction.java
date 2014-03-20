/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.component;

import static java.awt.Color.RED;
import static javax.swing.SwingUtilities.invokeLater;
import static net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceIcon.getIcon;
import static net.sf.taverna.t2.workbench.views.graph.GraphViewComponent.graphControllerMap;
import static org.apache.log4j.Logger.getLogger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.component.api.Component;
import net.sf.taverna.t2.component.api.ComponentFileType;
import net.sf.taverna.t2.component.api.Family;
import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.component.registry.ComponentVersionIdentification;
import net.sf.taverna.t2.component.ui.panel.ComponentChoiceMessage;
import net.sf.taverna.t2.component.ui.panel.ComponentVersionChooserPanel;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.models.graph.GraphController;
import net.sf.taverna.t2.workbench.models.graph.svg.SVGGraph;
import net.sf.taverna.t2.workbench.ui.impl.Workbench;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
public class OpenWorkflowFromComponentAction extends AbstractAction {
	private static final long serialVersionUID = 7382677337746318211L;
	private static final Logger logger = getLogger(OpenWorkflowFromComponentAction.class);
	private static final String ACTION_NAME = "Open component...";
	private static final String ACTION_DESCRIPTION = "Open the workflow that implements a component";
	private static final FileManager fm = FileManager.getInstance();

	public OpenWorkflowFromComponentAction(final java.awt.Component component) {
		putValue(SMALL_ICON, getIcon());
		putValue(NAME, ACTION_NAME);
		putValue(SHORT_DESCRIPTION, ACTION_DESCRIPTION);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		
		final ComponentVersionChooserPanel panel = new ComponentVersionChooserPanel();	
		
		final JButton okay = new JButton("OK");
		okay.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {	        
                JOptionPane pane = getOptionPane((JComponent)e.getSource());
                pane.setValue(JOptionPane.OK_OPTION);
		        doOpen(panel.getChosenRegistry(), panel.getChosenFamily(),
						panel.getChosenComponent(),
						panel.getChosenComponentVersion());
		    }
		});
		okay.setEnabled(false);
		// Only enable the OK button of a component is not null
		panel.getComponentChooserPanel().addObserver(new Observer<ComponentChoiceMessage>() {
			
			@Override
			public void notify(Observable<ComponentChoiceMessage> sender,
					ComponentChoiceMessage message) throws Exception {
				if (message.getChosenComponent() != null){
					okay.setEnabled(true);
				}
				else{
					okay.setEnabled(false);
				}				
			}
		});

		final JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
                JOptionPane pane = getOptionPane((JComponent)e.getSource());
                pane.setValue(JOptionPane.CANCEL_OPTION);
		    }
		});

		JOptionPane.showOptionDialog(Workbench.getInstance(), panel,
				"Component version choice",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
				new Object[] { okay, cancel }, okay);
				
	}
	
    protected JOptionPane getOptionPane(JComponent parent) {
        JOptionPane pane = null;
        if (!(parent instanceof JOptionPane)) {
            pane = getOptionPane((JComponent)parent.getParent());
        } else {
            pane = (JOptionPane) parent;
        }
        return pane;
    }

	private void doOpen(Registry registry, Family family, Component component,
			Version version) {
		Version.ID ident = new ComponentVersionIdentification(
				registry.getRegistryBase(), family.getName(),
				component.getName(), version.getVersionNumber());

		try {
			Dataflow d = fm.openDataflow(ComponentFileType.instance, ident);

			final GraphController gc = graphControllerMap.get(d);
			invokeLater(new Runnable() {
				@Override
				public void run() {
					if (gc != null) {
					SVGGraph g = (SVGGraph) gc.getGraph();
					g.setFillColor(RED);
					gc.redraw();
					}
				}
			});
		} catch (OpenException e) {
			logger.error("Failed to open component definition", e);
		}
	}
}
