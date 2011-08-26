package net.sf.taverna.t2.activities.dataflow.views;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.AbstractDataflowEditEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.events.ClosedDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.Processor;

/**
 * Tracks changes to {@link DataflowActivity}s through observing the
 * {@link EditManager} and the {@link FileManager}
 * 
 * @author Ian Dunlop
 * 
 */
public class DataflowObserver {

	private static DataflowObserver instance;

	private List<List> observerList;

	private DataflowObserver() {

		observerList = new ArrayList<List>();
		createObserver();
	}

	public static DataflowObserver getInstance() {
		if (instance == null) {
			instance = new DataflowObserver();
		}
		return instance;
	}

	/**
	 * Add a {@link List} containing the {@link DataflowActivity} and the
	 * {@link Processor} to the {@link #observerList}
	 * 
	 * @param dataflowActivity
	 * @param processor
	 */
	public void addDataflowObservers(DataflowActivity dataflowActivity,
			Processor processor) {
		List<Object> list = new ArrayList<Object>();
		list.add(dataflowActivity);
		list.add(processor);
		observerList.add(list);

	}

	/**
	 * Add {@link Observer}s to the {@link EditManager} and the
	 * {@link FileManager}. If the event is about a {@link Dataflow} being
	 * edited then configure the appropriate {@link DataflowActivity} and remap
	 * the appropriate {@link Processor} ports. If the {@link Dataflow} has been
	 * closed then remove the appropriate object from the list
	 */
	private void createObserver() {
		Observer<EditManagerEvent> editManagerObserver = new Observer<EditManagerEvent>() {

			public void notify(Observable<EditManagerEvent> sender,
					EditManagerEvent message) throws Exception {
				if (message instanceof AbstractDataflowEditEvent) {
					AbstractDataflowEditEvent dataflowEdit = (AbstractDataflowEditEvent) message;

					for (List list : observerList) {
						if (((DataflowActivity) list.get(0)).getConfiguration()
								.equals(dataflowEdit.getDataFlow())) {
							((DataflowActivity) list.get(0))
									.configure(dataflowEdit.getDataFlow());
							// remap the processor
							EditsRegistry.getEdits()
									.getMapProcessorPortsForActivityEdit(
											(Processor) list.get(1));
						}
					}

				}

			}

		};

		Observer<FileManagerEvent> fileManagerObserver = new Observer<FileManagerEvent>() {

			public void notify(Observable<FileManagerEvent> sender,
					FileManagerEvent message) throws Exception {
				if (message instanceof ClosedDataflowEvent) {
					ClosedDataflowEvent closedDataflowEvent = (ClosedDataflowEvent) message;
					closedDataflowEvent.getDataflow();
					for (List list : observerList) {
						if (((DataflowActivity) list.get(0)).getConfiguration()
								.equals(closedDataflowEvent.getDataflow())) {
							observerList.remove(list);
						}
					}
				}

			}

		};

		EditManager.getInstance().addObserver(editManagerObserver);
		FileManager.getInstance().addObserver(fileManagerObserver);
	}

}
