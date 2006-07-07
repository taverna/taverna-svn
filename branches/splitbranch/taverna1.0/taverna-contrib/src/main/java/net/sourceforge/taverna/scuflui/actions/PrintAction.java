package net.sourceforge.taverna.scuflui.actions;

import java.awt.event.ActionEvent;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;

import javax.swing.Action;

/**
 * 
 * @author Mark
 * 
 */
public class PrintAction extends DefaultAction {

	private static final String ACTION_COMMAND_KEY_ABOUT = "print-command";

	private static final String NAME_ABOUT = "Print...";

	private static final String SMALL_ICON_ABOUT = "etc/icons/stock_print-16.png";

	private static final String LARGE_ICON_ABOUT = "etc/icons/stock_print.png";

	private static final String SHORT_DESCRIPTION_ABOUT = "Print File";

	private static final String LONG_DESCRIPTION_ABOUT = "Print A File";

	private static final int MNEMONIC_KEY_ABOUT = 'P';

	private static final Character ACCELERATOR_KEY = new Character('P');

	public PrintAction() {
		putValue(Action.NAME, NAME_ABOUT);
		putValue(Action.SMALL_ICON, getIcon(SMALL_ICON_ABOUT));
		putValue(LARGE_ICON, getIcon(LARGE_ICON_ABOUT));
		putValue(Action.SHORT_DESCRIPTION, SHORT_DESCRIPTION_ABOUT);
		putValue(Action.LONG_DESCRIPTION, LONG_DESCRIPTION_ABOUT);
		putValue(Action.MNEMONIC_KEY, new Integer(MNEMONIC_KEY_ABOUT));
		putValue(Action.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY_ABOUT);
		putValue(Action.ACCELERATOR_KEY, getKeyStroke(ACCELERATOR_KEY));

	}

	/**
	 * actionPerformed
	 * 
	 * @param e
	 *            ActionEvent
	 */
	public void actionPerformed(ActionEvent e) {
		PrinterJob printJob = PrinterJob.getPrinterJob();

		printJob.setPrintable(printPage);
		if (printJob.printDialog()) {
			try {
				printJob.print();
			} catch (Exception PrinterExeption) {
			}
		}

	}

	/**
	 * This method sets the printable document to be used.
	 * 
	 * @param printable
	 */
	public void setPrintable(Printable printable) {
		this.printPage = printable;
	}

	Printable printPage = null;
}
