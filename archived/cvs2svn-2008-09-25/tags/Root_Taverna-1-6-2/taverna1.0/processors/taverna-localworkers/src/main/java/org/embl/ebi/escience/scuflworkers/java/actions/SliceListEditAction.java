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
import org.embl.ebi.escience.scuflworkers.java.LocalServiceProcessor;
import org.embl.ebi.escience.scuflworkers.java.SliceList;

/**
 * Edit sliceList depth action (right click on "slice List" local worker)
 * <p>
 * 
 * @see SliceList
 * @see FlattenListEditAction
 * @author Alan Williams 
 */
public class SliceListEditAction extends AbstractProcessorAction {

	private final class DepthChangeListener implements ChangeListener {
		private final SliceList slicer;

		private final JLabel label;

		private final JSpinner spinner;

		private DepthChangeListener(SliceList slicer, JLabel label,
			JSpinner spinner) {
			this.slicer = slicer;
			this.label = label;
			this.spinner = spinner;
		}

		public void stateChanged(ChangeEvent e) {
			label.setText(makeHelp((Integer) spinner.getValue()));
			slicer.setDepth((Integer) spinner.getValue());
		}
	}

	private static Logger logger =
		Logger.getLogger(SliceListEditAction.class);

	/**
	 * Only handles {@link LocalServiceProcessor} based on {@link SliceList}
	 */
	public boolean canHandle(Processor processor) {
		if (!(processor instanceof LocalServiceProcessor)) {
			return false;
		}
		LocalServiceProcessor proc = (LocalServiceProcessor) processor;
		return proc.getWorker() instanceof SliceList;
	}

	public String getDescription() {
		return "Set slice list depth";
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
		final SliceList slicer = (SliceList) proc.getWorker();

		panel.add(new JLabel("Depth: "), c);
		c.gridx = 1;

		// Less than 1 is useless
		SpinnerNumberModel range =
			new SpinnerNumberModel(slicer.getDepth(), 1, 100, 1);
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
		spinner.addChangeListener(new DepthChangeListener(slicer, helpLabel,
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
		String help =
			"<html>" + "The lists of depth " + levels
				+ " will be sliced.</html>";
		return help;
	}

}
