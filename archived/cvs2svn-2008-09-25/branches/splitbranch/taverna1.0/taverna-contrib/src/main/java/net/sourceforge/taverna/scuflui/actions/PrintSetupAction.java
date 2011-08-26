package net.sourceforge.taverna.scuflui.actions;

import java.awt.event.ActionEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;

import javax.swing.Action;
import javax.swing.KeyStroke;

public class PrintSetupAction extends DefaultAction {
	public PrintSetupAction() {
		init("Page Setup", "etc/icons/stock_print-preview-16.png", "etc/icons/stock_print-preview.png",
				"Print Options...", "Print Options");

		// Set a mnemonic character. In most look and feels, this causes the
		// specified character to be underlined This indicates that if the
		// component
		// using this action has the focus and In some look and feels, this
		// causes
		// the specified character in the label to be underlined and
		putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_P));

		// Set an accelerator key; this value is used by menu items
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("meta shift p"));

	}

	/**
	 * actionPerformed
	 * 
	 * @param e
	 *            ActionEvent
	 */
	public void actionPerformed(ActionEvent e) {
		PrinterJob job = PrinterJob.getPrinterJob();
		// Ask user for page format (e.g., portrait/landscape)
		PageFormat pf = job.pageDialog(job.defaultPage());

	}
}
