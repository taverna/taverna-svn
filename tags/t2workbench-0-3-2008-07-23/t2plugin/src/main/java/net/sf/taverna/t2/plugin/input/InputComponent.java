package net.sf.taverna.t2.plugin.input;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.DataManager;
import net.sf.taverna.t2.cloudone.datamanager.EmptyListException;
import net.sf.taverna.t2.cloudone.datamanager.MalformedListException;
import net.sf.taverna.t2.cloudone.datamanager.UnsupportedObjectTypeException;
import net.sf.taverna.t2.cloudone.gui.entity.model.DataDocumentModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.EntityListModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.EntityModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.FileRefSchemeModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.HttpRefSchemeModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.LiteralModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.ReferenceSchemeModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.SingletonListModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.StringModel;
import net.sf.taverna.t2.cloudone.gui.entity.view.EntityListView;
import net.sf.taverna.t2.cloudone.gui.entity.view.LiteralView;
import net.sf.taverna.t2.cloudone.gui.entity.view.SingletonListView;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;
import net.sf.taverna.t2.cloudone.refscheme.file.FileReferenceScheme;
import net.sf.taverna.t2.cloudone.refscheme.http.HttpReferenceScheme;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;

public class InputComponent<InputType extends DataflowInputPort> extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1107966941652188905L;
	private final List<InputType> inputPorts;
	private Map<InputType, SingletonListModel> models = new HashMap<InputType, SingletonListModel>();;
	private Map<InputType, SingletonListView> views = new HashMap<InputType, SingletonListView>();
	private JTabbedPane tabbedPane;
	private DataManager dataManager;
	private DataFacade dataFacade;
	private final InputComponentCallback<InputType> runMethod;
	private InvocationContext context;;

	/**
	 * Method to be invoked
	 * 
	 */
	public interface InputComponentCallback<InputType extends DataflowInputPort> {
		public void invoke(Map<InputType, EntityIdentifier> entities);

		/**
		 * Text to be shown on the button that will invoke {@link #invoke(Map)}.
		 * 
		 * @return A {@link String} to be shown on the button
		 */
		public String getButtonText();
	}

	public InputComponent(List<InputType> inputPorts,
			InputComponentCallback<InputType> runMethod,
			InvocationContext context) {
		this.inputPorts = inputPorts;
		this.runMethod = runMethod;
		this.context = context;
		initialise();
	}

	private void initialise() {
		setLayout(new BorderLayout());
		CreateDataAction createDataAction = new CreateDataAction();
		ClearAction clearAction = new ClearAction();
		// add(new JButton(clearAction));
		add(new JButton(createDataAction), BorderLayout.SOUTH);
		add(new JLabel("Inputs"), BorderLayout.NORTH);
		tabbedPane = new JTabbedPane();
		add(tabbedPane, BorderLayout.CENTER);
		register(inputPorts);
		this.dataManager = getDataManager();
		this.dataFacade = new DataFacade(dataManager);
	}

	public void register(List<InputType> inputPorts) {
		for (InputType dataflowInputPort : inputPorts) {
			String portName = dataflowInputPort.getName();
			Component entityPanel = createEntityPanel(dataflowInputPort);
			tabbedPane.add(portName, new JScrollPane(entityPanel));
		}
	}

	private DataManager getDataManager() {
		return context.getDataManager();
	}

	private Component createEntityPanel(InputType dataflowInputPort) {
		SingletonListModel model = new SingletonListModel(dataflowInputPort
				.getDepth());
		SingletonListView view = new SingletonListView(model);
		models.put(dataflowInputPort, model);
		views.put(dataflowInputPort, view);
		return view;
	}

	public void clear() {
		tabbedPane.removeAll();
		register(inputPorts);
	}

	private Object prepareForFacacde(EntityModel model) {
		// ignore if EntityRootModel
		if (model instanceof EntityListModel) {
			EntityListModel listModel = (EntityListModel) model;
			List<Object> children = new ArrayList<Object>();
			for (EntityModel childModel : listModel.getEntityModels()) {
				children.add(prepareForFacacde(childModel));
			}
			return children;
		} else if (model instanceof DataDocumentModel) {
			return registerDataDocument((DataDocumentModel) model);
		} else if (model instanceof LiteralModel) {
			return ((LiteralModel) model).getLiteral();
		} else if (model instanceof StringModel) {
			return ((StringModel) model).getString();
		} else {
			throw new IllegalArgumentException("Unsupported model: " + model);
		}
	}

	@SuppressWarnings("unchecked")
	private DataDocumentIdentifier registerDataDocument(DataDocumentModel model) {
		Set<ReferenceScheme> refSet = new HashSet<ReferenceScheme>();
		for (ReferenceSchemeModel refModel : model.getReferenceSchemeModels()) {
			refModel.getStringRepresentation();
			// SPI stuff needed?
			if (refModel instanceof HttpRefSchemeModel) {
				URL url = ((HttpRefSchemeModel) refModel).getURL();
				HttpReferenceScheme ref = new HttpReferenceScheme(url);
				refSet.add(ref);
			} else if (refModel instanceof FileRefSchemeModel) {
				File file = ((FileRefSchemeModel) refModel).getFile();
				FileReferenceScheme ref = new FileReferenceScheme(file);
				refSet.add(ref);
			} else {
				// fail because we don't recognise the type
			}
		}
		DataDocumentIdentifier doc = dataManager.registerDocument(refSet);
		return doc;
	}

	public class CreateDataAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 7600977055252635059L;

		public CreateDataAction() {
			super(runMethod.getButtonText());
		}

		public void actionPerformed(ActionEvent e) {
			
			for (Entry<InputType, SingletonListView> entry:views.entrySet()) {
				try {
					entry.getValue().setEdit(false);
				} catch (IllegalStateException ex) {
					// OK
					return;
				}
			}
			
			try {
				Map<InputType, EntityIdentifier> entities = registerModels();
				runMethod.invoke(entities);
			} catch (EntityValueMissingException ex) {
				JOptionPane.showMessageDialog(InputComponent.this,
						"There is no value for '" + ex.getPort().getName()
								+ "'", "Missing value",
						JOptionPane.WARNING_MESSAGE);
			} catch (InputPortException ex) {
				String msg = "Could not register '" + ex.getPort().getName()
						+ "'";
				if (ex.getCause() != null) {
					msg += ":\n" + ex.getCause().getLocalizedMessage();
				}
				JOptionPane.showMessageDialog(InputComponent.this, msg,
						"Could not register port", JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	public class ClearAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = -2823990029337404456L;

		public ClearAction() {
			super("Clear All Data");
		}

		public void actionPerformed(ActionEvent e) {
			clear();
		}

	}

	public Map<InputType, EntityIdentifier> registerModels()
			throws EntityValueMissingException, InputPortException {
		Map<InputType, EntityIdentifier> portEntities = new HashMap<InputType, EntityIdentifier>();
		for (Entry<InputType, SingletonListModel> entry : models.entrySet()) {
			InputType port = entry.getKey();
			SingletonListModel singletonModel = entry.getValue();
			EntityModel model = singletonModel.getSingleton();
			if (model != null) {
				EntityIdentifier entity;
				try {
					entity = registerModel(model);
				} catch (EmptyListException e) {
					throw new InputPortException(port, e);
				} catch (MalformedListException e) {
					throw new InputPortException(port, e);
				}
				portEntities.put(port, entity);
			} else {
				throw new EntityValueMissingException(port);
			}
		}
		return portEntities;
	}

	public EntityIdentifier registerModel(EntityModel model)
			throws EmptyListException, MalformedListException {
		Object obj = prepareForFacacde(model);
		EntityIdentifier identifier;
		try {
			identifier = dataFacade.register(obj);
		} catch (UnsupportedObjectTypeException e) {
			// All EntityModel's should be supported by prepareForFacacde
			throw new RuntimeException(
					"Unexpected UnsupportedObjectTypeException", e);
		}
		return identifier;
	}

}
