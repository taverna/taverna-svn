package net.sf.taverna.t2.workbench.ui.views.contextualviews.activity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicTreeUI.SelectionModelPropertyChangeHandler;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.lang.ui.ModelMap.ModelMapEvent;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionMessage;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionModel;
import net.sf.taverna.t2.workbench.ui.impl.DataflowSelectionManager;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class ContextualViewComponent extends JPanel implements UIComponentSPI {

	private Observer<DataflowSelectionMessage> dataflowSelectionListener = new DataflowSelectionListener();
	private ModelMapObserver modelMapObserver = new ModelMapObserver();
	private JPanel panel;
	private DataflowSelectionManager dataflowSelectionManager = DataflowSelectionManager
			.getInstance();
	private JComponent view;
	private FileManager fileManager = FileManager.getInstance();

	/** Keep list of views in case you want to go back or forward between them */
	private List<JPanel> views = new ArrayList<JPanel>();

	public ContextualViewComponent() {
		Dataflow currentDataflow = fileManager.getCurrentDataflow();

		DataflowSelectionModel selectionModel = dataflowSelectionManager
				.getDataflowSelectionModel(currentDataflow);
		selectionModel.addObserver(dataflowSelectionListener);

		ModelMap.getInstance().addObserver(modelMapObserver);
		initialise();
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return "Contextual View";
	}

	private void initialise() {
		add(new JLabel("this is a contextual view!"));
		panel = new JPanel();
		add(panel);
	}

	public void onDisplay() {
		// TODO Auto-generated method stub

	}

	public void onDispose() {
		// TODO Auto-generated method stub

	}

	public void updateContextualView(JPanel view) {
		remove(this.view);
		views.add(view);
		add(view);
		revalidate();
	}

	public void updateSelection() {
		Dataflow dataflow = fileManager.getCurrentDataflow();
		DataflowSelectionModel selectionModel = dataflowSelectionManager.getDataflowSelectionModel(dataflow);
		Set<Object> selection = selectionModel.getSelection();
		if (selection.isEmpty()) {
			return;
		}
		Iterator<Object> iterator = selection.iterator();
		//TODO multiple selections, dataflow contextual view, datalink contextual view
		Object clickedOnObject = iterator.next();
		
		if (clickedOnObject instanceof Processor) {
			Activity<?> activity = ((Processor)clickedOnObject).getActivityList().get(0);
			ActivityViewFactory viewFactoryForBeanType = ActivityViewFactoryRegistry
			.getInstance()
			.getViewFactoryForBeanType(activity);
			ActivityContextualView viewType = viewFactoryForBeanType
			.getView(activity);
			updateContextualView(viewType);
		}
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
}
