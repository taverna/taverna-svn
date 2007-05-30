package org.embl.ebi.escience.scuflworkers.apiconsumer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.ProcessorEditor;
import org.embl.ebi.escience.scuflworkers.dependency.DependenciesPanel;


public class APIConsumerEditor implements ProcessorEditor {

	public String getEditorDescription() {
		return "Configure dependencies...";
	}

	public ActionListener getListener(Processor theProcessor) {
		final APIConsumerProcessor bp = (APIConsumerProcessor) theProcessor;
		return new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				JComponent component = new DependenciesPanel(bp);
				JFrame newFrame = new JFrame("Dependencies for " + bp);
				newFrame.setContentPane(component);
				newFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				newFrame.setSize(500, 600);
				newFrame.setLocation(50, 50);
				newFrame.setVisible(true);
			}
		};
	}
}
