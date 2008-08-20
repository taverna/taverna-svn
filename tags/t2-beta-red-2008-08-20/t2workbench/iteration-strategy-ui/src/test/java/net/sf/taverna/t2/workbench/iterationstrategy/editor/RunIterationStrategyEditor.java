package net.sf.taverna.t2.workbench.iterationstrategy.editor;

import javax.swing.JFrame;

import net.sf.taverna.t2.workflowmodel.processor.iteration.NamedInputPortNode;
import net.sf.taverna.t2.workflowmodel.processor.iteration.impl.IterationStrategyImpl;

public class RunIterationStrategyEditor {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		IterationStrategyImpl iterationStrategyImpl = new IterationStrategyImpl();
		NamedInputPortNode fishPort = new NamedInputPortNode("fish", 2);
		NamedInputPortNode otherPort = new NamedInputPortNode("other", 0);
		NamedInputPortNode soupPort = new NamedInputPortNode("soup", 1);
		iterationStrategyImpl.addInput(fishPort);
		iterationStrategyImpl.addInput(soupPort);
		iterationStrategyImpl.addInput(otherPort);

		iterationStrategyImpl.connectDefault(otherPort);
		iterationStrategyImpl.connectDefault(fishPort);
		iterationStrategyImpl.connectDefault(soupPort);
		
		IterationStrategyEditorControl editorControl = new IterationStrategyEditorControl(iterationStrategyImpl);
		
		JFrame frame = new JFrame("Iteration strategy editor");
		frame.add(editorControl);
		frame.setSize(500,400);
		frame.setVisible(true);
		
		
	}

}
