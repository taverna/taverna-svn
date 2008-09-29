/**
 * 
 */
package net.sf.taverna.t2.workbench.iterationstrategy.contextview;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.iterationstrategy.editor.IterationStrategyEditorControl;
import net.sf.taverna.t2.workbench.iterationstrategy.editor.IterationStrategyTree;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.iteration.IterationStrategy;
import net.sf.taverna.t2.workflowmodel.processor.iteration.IterationStrategyStack;
import net.sf.taverna.t2.workflowmodel.processor.iteration.impl.IterationStrategyImpl;
import net.sf.taverna.t2.workflowmodel.processor.iteration.impl.IterationStrategyStackImpl;

import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class IterationStrategyContextualView extends ContextualView {

	private EditManager editManager = EditManager.getInstance();

	private FileManager fileManager = FileManager.getInstance();

	private IterationStrategyTree strategyTree = new IterationStrategyTree();

	private static Logger logger = Logger
			.getLogger(IterationStrategyContextualView.class);

	private final class ConfigureIterationStrategyAction extends AbstractAction {
		private final Frame owner;

		private ConfigureIterationStrategyAction(Frame owner) {
			super("Configure iteration strategy");
			this.owner = owner;
		}

		public void actionPerformed(ActionEvent e) {
			String title = "Iteration strategy for " + processor.getLocalName();
			final JDialog dialog = new JDialog(owner, title, true);
			IterationStrategyImpl iterationStrategy = getIterationStrategy();
			IterationStrategyEditorControl iterationStrategyEditorControl = new IterationStrategyEditorControl(
					iterationStrategy);
			dialog.add(iterationStrategyEditorControl, BorderLayout.CENTER);

			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new FlowLayout());

			JButton okButton = new JButton(new OKAction(dialog));
			buttonPanel.add(okButton);

			JButton resetButton = new JButton(new ResetAction(
					iterationStrategyEditorControl));
			buttonPanel.add(resetButton);

			JButton cancelButton = new JButton(new CancelAction(dialog));
			buttonPanel.add(cancelButton);

			dialog.add(buttonPanel, BorderLayout.SOUTH);
			dialog.setSize(400, 400);
			dialog.setVisible(true);
		}

		private final class ResetAction extends AbstractAction {
			private final IterationStrategyEditorControl strategyEditorControl;

			private ResetAction(
					IterationStrategyEditorControl strategyEditorControl) {
				super("Reset");
				this.strategyEditorControl = strategyEditorControl;
			}

			public void actionPerformed(ActionEvent e) {
				refreshView();
				strategyEditorControl
						.setIterationStrategy(getIterationStrategy());
			}

		}

		private final class CancelAction extends AbstractAction {
			private final JDialog dialog;

			private CancelAction(JDialog dialog) {
				super("Cancel");
				this.dialog = dialog;
			}

			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
				refreshView();
			}

		}

		private final class OKAction extends AbstractAction {
			private final JDialog dialog;

			private OKAction(JDialog dialog) {
				super("OK");
				this.dialog = dialog;
			}

			public void actionPerformed(ActionEvent e) {
				Edits edits = editManager.getEdits();
				try {
					Edit<?> edit = edits.getSetIterationStrategyStackEdit(
							processor,
							copyIterationStrategyStack(iterationStack));
					editManager.doDataflowEdit(
							fileManager.getCurrentDataflow(), edit);
					dialog.setVisible(false);
					refreshView();
				} catch (RuntimeException ex) {
					logger.warn("Could not set iteration strategy", ex);
					JOptionPane.showMessageDialog(owner,
							"Can't set iteration strategy",
							"An error occured when setting iteration strategy: "
									+ ex.getMessage(),
							JOptionPane.ERROR_MESSAGE);
				} catch (EditException ex) {
					logger.warn("Could not set iteration strategy", ex);
					JOptionPane.showMessageDialog(owner,
							"Can't set iteration strategy",
							"An error occured when setting iteration strategy: "
									+ ex.getMessage(),
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	private IterationStrategyStackImpl iterationStack;
	private final Processor processor;

	public IterationStrategyContextualView(Processor processor) {
		if (processor == null || processor.getIterationStrategy() == null) {
			throw new NullPointerException(
					"Iteration strategy stack can't be null");
		}
		this.processor = processor;
		refreshIterationStrategyStack();
		initView();
	}

	@Override
	protected JComponent getMainFrame() {
		refreshView();
		return strategyTree;
	}

	private IterationStrategyStackImpl copyIterationStrategyStack(
			IterationStrategyStackImpl stack) {
		Element asXML = stack.asXML();
		IterationStrategyStackImpl copyStack = new IterationStrategyStackImpl();
		copyStack.configureFromElement(asXML);
		return copyStack;
	}

	private IterationStrategyImpl getIterationStrategy() {
		List<? extends IterationStrategy> strategies = iterationStack
				.getStrategies();
		if (strategies.isEmpty()) {
			throw new IllegalStateException("Empty iteration stack");
		}
		IterationStrategy strategy = strategies.get(0);
		if (!(strategy instanceof IterationStrategyImpl)) {
			throw new IllegalStateException(
					"Can't edit unknown iteration strategy implementation "
							+ strategy);
		}
		return (IterationStrategyImpl) strategy;
	}

	@Override
	public Action getConfigureAction(final Frame owner) {
		return new ConfigureIterationStrategyAction(owner);
	}

	@Override
	protected String getViewTitle() {
		return "Iteration strategy";
	}

	@Override
	public void refreshView() {
		refreshIterationStrategyStack();
		strategyTree.setIterationStrategy(getIterationStrategy());
	}

	private void refreshIterationStrategyStack() {
		IterationStrategyStack originalIterationStrategy = processor
				.getIterationStrategy();
		if (!(originalIterationStrategy instanceof IterationStrategyStackImpl)) {
			throw new IllegalStateException(
					"Unknown iteration strategy implementation "
							+ originalIterationStrategy);
		}
		this.iterationStack = copyIterationStrategyStack((IterationStrategyStackImpl) originalIterationStrategy);
	}
}