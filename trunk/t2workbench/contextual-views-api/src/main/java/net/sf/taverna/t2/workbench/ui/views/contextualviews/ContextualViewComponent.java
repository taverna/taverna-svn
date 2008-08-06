package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.lang.ui.ModelMap.ModelMapEvent;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionMessage;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionModel;
import net.sf.taverna.t2.workbench.ui.impl.DataflowSelectionManager;
import net.sf.taverna.t2.workbench.ui.impl.Workbench;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactoryRegistry;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

@SuppressWarnings("serial")
public class ContextualViewComponent extends JPanel implements UIComponentSPI {

	private Observer<DataflowSelectionMessage> dataflowSelectionListener = new DataflowSelectionListener();
	private ModelMapObserver modelMapObserver = new ModelMapObserver();
	private EditManagerObserver editManagerObserver = new EditManagerObserver();
	private DataflowSelectionManager dataflowSelectionManager = DataflowSelectionManager
			.getInstance();
	private ContextualView view;
	private FileManager fileManager = FileManager.getInstance();
	private JButton configureButton = new JButton("Configure");

	/** Keep list of views in case you want to go back or forward between them */
	private List<ContextualView> views = new ArrayList<ContextualView>();
	private JPanel panel;

	public ContextualViewComponent() {
		Dataflow currentDataflow = fileManager.getCurrentDataflow();

		DataflowSelectionModel selectionModel = dataflowSelectionManager
				.getDataflowSelectionModel(currentDataflow);
		selectionModel.addObserver(dataflowSelectionListener);

		ModelMap.getInstance().addObserver(modelMapObserver);
		EditManager.getInstance().addObserver(editManagerObserver);
		initialise();
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return "Contextual View";
	}

	private void initialise() {

		setLayout(new BorderLayout());

		panel = new JPanel(new BorderLayout());
		add(panel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(configureButton);
		configureButton.setEnabled(false);
		configureButton.setVisible(false);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	public void onDisplay() {
		// TODO Auto-generated method stub

	}

	public void onDispose() {
		// TODO Auto-generated method stub

	}

	private void updateContextualView(ContextualView view) {
		if (this.view != null) {
			panel.remove(this.view);
		}
		this.view = view;
		views.add(view);

		panel.add(view, BorderLayout.CENTER);
		if (view.getConfigureAction(Workbench.getInstance()) != null) {
			configureButton.setAction(view.getConfigureAction(Workbench
					.getInstance()));
			configureButton.setText("Configure");
			configureButton.setEnabled(true);
			configureButton.setVisible(true);
		} else {
			configureButton.setEnabled(false);
			configureButton.setVisible(false);
		}
		revalidate();
	}

	public void updateSelection(Object selectedItem) {

		if (selectedItem instanceof Processor) {
			Processor processor = (Processor) selectedItem;
			Activity<?> activity = processor.getActivityList().get(0);
			//handleProcessor(processor);
			findContextualView(activity);
		} else {
			findContextualView(selectedItem);
		}
		
	}

//	private void handleProcessor(Processor processor) {
//		updateContextualView(new ProcessorContextualView(processor));
//	}

	public void updateSelection() {
		Dataflow dataflow = fileManager.getCurrentDataflow();
		DataflowSelectionModel selectionModel = dataflowSelectionManager
				.getDataflowSelectionModel(dataflow);
		Set<Object> selection = selectionModel.getSelection();
		if (selection.isEmpty()) {
			return;
		}
		Iterator<Object> iterator = selection.iterator();

		// TODO multiple selections, dataflow contextual view, datalink
		// contextual view
		updateSelection(iterator.next());
	}

	@SuppressWarnings("unchecked")
	private void findContextualView(Object selection) {
		ContextualViewFactoryRegistry reg = ContextualViewFactoryRegistry
				.getInstance();
		ContextualViewFactory viewFactoryForBeanType = reg
				.getViewFactoryForObject(selection);
		ContextualView viewType = viewFactoryForBeanType.getView(selection);
		updateContextualView(viewType);
	}

	private final class DataflowSelectionListener implements
			Observer<DataflowSelectionMessage> {

		public void notify(Observable<DataflowSelectionMessage> sender,
				DataflowSelectionMessage message) throws Exception {
			updateSelection();
		}

	}

	private final class ModelMapObserver implements Observer<ModelMapEvent> {
		public void notify(Observable<ModelMapEvent> sender,
				ModelMapEvent message) throws Exception {
			if (message.getModelName().equals(
					ModelMapConstants.CURRENT_DATAFLOW)) {
				Dataflow oldFlow = (Dataflow) message.getOldModel();
				Dataflow newFlow = (Dataflow) message.getNewModel();
				if (oldFlow != null) {
					dataflowSelectionManager.getDataflowSelectionModel(oldFlow)
							.removeObserver(dataflowSelectionListener);
				}
				if (newFlow != null) {
					dataflowSelectionManager.getDataflowSelectionModel(newFlow)
							.addObserver(dataflowSelectionListener);
				}
				updateSelection();
			}

		}
	}

	private final class EditManagerObserver implements
			Observer<EditManagerEvent> {

		public void notify(Observable<EditManagerEvent> sender,
				EditManagerEvent message) throws Exception {

			refreshView();
		}

	}

	public void refreshView() {
		if (view != null) {
			view.refreshView();
		}
	}
}
