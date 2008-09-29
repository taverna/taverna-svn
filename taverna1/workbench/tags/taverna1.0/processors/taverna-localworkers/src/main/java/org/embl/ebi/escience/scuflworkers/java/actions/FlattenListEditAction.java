package org.embl.ebi.escience.scuflworkers.java.actions;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflui.actions.AbstractProcessorAction;
import org.embl.ebi.escience.scuflworkers.java.FlattenList;
import org.embl.ebi.escience.scuflworkers.java.LocalServiceProcessor;

/**
 * Edit flattener depth action (right click on "Flatten List" local worker)
 * <p>
 * 
 * @see FlattenList
 * @author Stian Soiland
 */
public class FlattenListEditAction extends AbstractProcessorAction {

	private final class DepthChangeListener implements ChangeListener {
		private final FlattenList flattener;

		private final JLabel label;

		private final JSpinner spinner;

		private DepthChangeListener(FlattenList flattener, JLabel label,
			JSpinner spinner) {
			this.flattener = flattener;
			this.label = label;
			this.spinner = spinner;
		}

		public void stateChanged(ChangeEvent e) {
			label.setText(makeHelp((Integer) spinner.getValue()));
			flattener.setDepth((Integer) spinner.getValue());
		}
	}

	private static Logger logger =
		Logger.getLogger(FlattenListEditAction.class);

	/**
	 * Only handles {@link LocalServiceProcessor} based on {@link FlattenList}
	 */
	public boolean canHandle(Processor processor) {
		if (!(processor instanceof LocalServiceProcessor)) {
			return false;
		}
		LocalServiceProcessor proc = (LocalServiceProcessor) processor;
		return proc.getWorker() instanceof FlattenList;
	}

	public String getDescription() {
		return "Set list flattener depth";
	}

	public ImageIcon getIcon() {
		return null;
	}

	public Dimension getFrameSize() {
		// Big enough to show the makeHelp() at up to 20 lines or so
		return new Dimension(280, 340);
	}

	@Override
	public JComponent getComponent(Processor processor) {
		JPanel panel = new JPanel(new GridBagLayout());
		if (!(processor instanceof LocalServiceProcessor)) {
			logger.error("Processor not a LocalServiceProcessor");
			return panel;
		}
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTH;
		c.gridx = 0;
		c.gridy = 0;

		LocalServiceProcessor proc = (LocalServiceProcessor) processor;
		final FlattenList flattener = (FlattenList) proc.getWorker();

		panel.add(new JLabel("Depth: "), c);
		c.gridx = 1;

		// Less than 2 is useless
		SpinnerNumberModel range =
			new SpinnerNumberModel(flattener.getDepth(), 2, 100, 1);
		final JSpinner spinner = new JSpinner(range);
		panel.add(spinner, c);
		String help = makeHelp((Integer) spinner.getValue());
		final JLabel helpLabel = new JLabel(help);
		c.gridy = 1;
		c.gridx = 0;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 0.1;
		panel.add(helpLabel, c);
		spinner.addChangeListener(new DepthChangeListener(flattener, helpLabel,
			spinner));
		return panel;
	}

	/**
	 * Generate the help text, with example if the current level is less than 10
	 * 
	 * @param levels The current level
	 * @return A HTML help string with example
	 */
	private String makeHelp(int levels) {
		String example;
		if (levels < 0 || levels > 10) {
			// If you do more than 10, you get the picture
			example = "";
		} else {
			example = " Example: <pre><small>";
			for (int i = 0; i < levels; i++) {
				example += "\n";
				for (int j = 0; j < i; j++) {
					example += "  ";
				}
				example += "[";
			}
			example += " 0 1 2 ";
			for (int i = levels - 1; i >= 0; i--) {
				example += "]\n";
				for (int j = 1; j < i; j++) {
					example += "  ";
				}
			}
			example += "</small></pre>\n flattens to: \n";
			example += "<pre><small>[ 0 1 2 ]</small></pre>";
		}
		String help =
			"<html>" + "The " + levels + " outermost levels "
				+ "will be <br> flattened to one list. " + example + "</html>";
		return help;
	}

}
