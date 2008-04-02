package uk.org.mygrid.datalineage;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListModel;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.store.NoSuchLSIDException;
import org.embl.ebi.escience.scuflui.ResultItemPanel;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.ProvenanceOntologyConstants;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.JenaProvenanceOntology;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.ProvenanceVocab;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataService;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceCreationException;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceException;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceFactory;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ProvenanceGenerator;
import uk.org.mygrid.datalineage.model.AbstractDataVertex;
import uk.org.mygrid.datalineage.model.DataCollectionVertexImpl;
import uk.org.mygrid.datalineage.model.DataItemVertexImpl;
import uk.org.mygrid.datalineage.model.DataLineageVertexShapeFunction;
import uk.org.mygrid.datalineage.model.DataVertex;
import uk.org.mygrid.datalineage.model.DataVertexPaintFunction;
import uk.org.mygrid.datalineage.model.EmptyVertexImpl;
import uk.org.mygrid.datalineage.model.InputCollectionVertexImpl;
import uk.org.mygrid.datalineage.model.InputItemVertexImpl;
import uk.org.mygrid.datalineage.model.OutputCollectionVertexImpl;
import uk.org.mygrid.datalineage.model.OutputItemVertexImpl;
import uk.org.mygrid.logbook.util.DataProvenance;
import uk.org.mygrid.logbook.util.Utils;
import uk.org.mygrid.provenance.dataservice.DataService;
import uk.org.mygrid.provenance.dataservice.DataServiceException;
import uk.org.mygrid.provenance.dataservice.DataServiceFactory;
import uk.org.mygrid.provenance.util.LogBookConfigurationNotFoundException;
import uk.org.mygrid.provenance.util.ProvenanceConfigurator;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;

import edu.uci.ics.jung.graph.ArchetypeVertex;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.decorators.EdgePaintFunction;
import edu.uci.ics.jung.graph.decorators.EdgeStrokeFunction;
import edu.uci.ics.jung.graph.decorators.StringLabeller;
import edu.uci.ics.jung.graph.decorators.VertexShapeFunction;
import edu.uci.ics.jung.graph.decorators.StringLabeller.UniqueLabelException;
import edu.uci.ics.jung.graph.impl.DirectedSparseEdge;
import edu.uci.ics.jung.graph.impl.DirectedSparseGraph;
import edu.uci.ics.jung.utils.Pair;
import edu.uci.ics.jung.utils.UserData;
import edu.uci.ics.jung.visualization.DefaultGraphLabelRenderer;
import edu.uci.ics.jung.visualization.PluggableRenderer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.contrib.DAGLayout;

public class DataLineageVisualiser extends JPanel implements UIComponentSPI {

	private static final long serialVersionUID = 1L;

	public static final Dimension FRAME_DIMENSION = new Dimension(1200, 800);

	public static final String TITLE = "Data Lineage";

	public static final String OUTPUT_PREFIX = "output";

	private static final Object EDGE_COLOR_KEY = "EDGE COLOR";

	private static Logger logger = Logger
			.getLogger(DataLineageVisualiser.class);

	private OntModel ontModel;

	private VisualizationViewer visualizationViewer;

	private Graph graph;

	private DataLineageStringLabeller labeller;

	private JenaProvenanceOntology ontology;

	private Map<String, Set<String>> dataGraph = new HashMap<String, Set<String>>();

	private MetadataService metadataService;

	private DataService dataService;

	private String workflowRunId;

	private Map<String, DataThing> dataThings = new HashMap<String, DataThing>();

	private JPanel resultPanel;

	private String selectedData;

	private SimilarDataAction similarDataAction;

	private JPanel similarDataListPanel;

	public Graph getGraph() {
		return graph;
	}

	public VisualizationViewer getVisualizationViewer() {
		return visualizationViewer;
	}

	public DataLineageVisualiser(String workflowRunId)
			throws DataLineageException, MetadataServiceCreationException,
			LogBookConfigurationNotFoundException, DataServiceException {
		this(MetadataServiceFactory.getInstance(ProvenanceConfigurator
				.getConfiguration()), DataServiceFactory
				.getInstance(ProvenanceConfigurator.getConfiguration()),
				workflowRunId);
	}

	public DataLineageVisualiser(MetadataService metadataService,
			DataService dataService, String workflowRunId)
			throws DataLineageException {
		this(metadataService, dataService, workflowRunId, null);
	}

	public DataLineageVisualiser(MetadataService metadataService,
			DataService dataService, String workflowRunId,
			String selectedDataLSID) throws DataLineageException {
		super();
		this.metadataService = metadataService;
		this.dataService = dataService;
		this.workflowRunId = workflowRunId;
		try {
			String workflowRun = metadataService.getWorkflowRun(workflowRunId);
			ontology = new JenaProvenanceOntology(workflowRun);
			ontModel = ontology.getOntModel();

			graph = new DirectedSparseGraph();

			labeller = new DataLineageStringLabeller(graph);
			// ToStringLabeller.setLabellerTo(g);

			PluggableRenderer pr = createRenderer();

			populateDataGraph();
			populateGraph();

			downlightEdges();

			visualizationViewer = new VisualizationViewer(new DAGLayout(graph),
					pr);
			visualizationViewer.getPickedState().addItemListener(
					new VertexListener());

			createPanel(workflowRunId);

			if (selectedDataLSID != null) {
				String localId = getLocalId(selectedDataLSID);
				DataVertex dataVertex = (DataVertex) labeller
						.getVertex(localId);
				focusOn(dataVertex);
			}

		} catch (MetadataServiceException e) {
			throw new DataLineageException(e);
		} catch (UniqueLabelException e) {
			throw new DataLineageException(e);
		} catch (NoSuchLSIDException e) {
			throw new DataLineageException(e);
		} catch (DataServiceException e) {
			throw new DataLineageException(e);
		}
	}

	private void createPanel(String workflowRunId) {
		setLayout(new BorderLayout());
		JSplitPane outermostPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		outermostPanel.setDividerLocation(0.5);
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		JPanel titlePanel = createTitlePanel(workflowRunId);
		add(titlePanel, BorderLayout.NORTH);
		// JButton resetEdges = new JButton(new ResetAction());
		// panel.add(resetEdges);
		panel.add(new JScrollPane(visualizationViewer));
		outermostPanel.add(panel);
		JPanel dataPanel = new JPanel();
		dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.PAGE_AXIS));
		resultPanel = new JPanel();
		resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.PAGE_AXIS));
		resultPanel.add(new JLabel(
				"Click on a node to display associated data here."));
		JPanel similarDataPanel = new JPanel();
		similarDataPanel.setLayout(new BoxLayout(similarDataPanel,
				BoxLayout.PAGE_AXIS));

		JToolBar toolBarPanel = new JToolBar();
		toolBarPanel.setFloatable(false);
		toolBarPanel.setRollover(true);
		toolBarPanel.setMaximumSize(new Dimension(2000, 30));
		toolBarPanel.setBorderPainted(true);
		similarDataAction = new SimilarDataAction();
		similarDataAction.setEnabled(false);
		toolBarPanel.add(new JButton(similarDataAction));
		toolBarPanel.addSeparator();
		similarDataPanel.add(toolBarPanel);
		similarDataListPanel = new JPanel();
		similarDataListPanel.setLayout(new BoxLayout(similarDataListPanel,
				BoxLayout.PAGE_AXIS));
		similarDataPanel.add(similarDataListPanel);
		dataPanel.add(similarDataPanel);
		dataPanel.add(resultPanel);
		outermostPanel.add(new JScrollPane(dataPanel));
		add(new JScrollPane(outermostPanel), BorderLayout.CENTER);
	}

	private void focusOn(DataVertex dataVertex) throws NoSuchLSIDException,
			DataServiceException {
		selectedData = dataVertex.getDataLsid();
		resultPanel.removeAll();
		DataThing d = getDataThing(selectedData);
		resultPanel.add(new ResultItemPanel(d));
		resultPanel.revalidate();
		highlightEdgesFrom(dataVertex);
	}

	private void similarData(String data) throws MetadataServiceException {
		final Map<String, DataProvenance> similarData = metadataService
				.getSimilarData(data);
		final SortedMap<Date, DataProvenance> sortedDataProvenance = new TreeMap<Date, DataProvenance>(
				new ReverseDateComparator());
		Collection<DataProvenance> values = similarData.values();
		for (DataProvenance provenance : values) {
			Date date = provenance.getDate();
			if (date != null) {
				sortedDataProvenance.put(date, provenance);
			}
		}
		final Vector<Date> dates = new Vector<Date>(sortedDataProvenance
				.keySet());
		ListModel listModel = new AbstractListModel() {
			private static final long serialVersionUID = 1L;

			public int getSize() {
				return dates.size();
			}

			public Object getElementAt(int index) {
				Date date = dates.get(index);
				return sortedDataProvenance.get(date);
			}
		};
		final JList list = new JList(listModel);
		MouseListener mouseListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int index = list.locationToIndex(e.getPoint());
					Date date = dates.get(index);
					DataProvenance dataProvenance = sortedDataProvenance
							.get(date);
					String similarWorkflowRun = dataProvenance.getWorkflowRun();
					String dataLSID = dataProvenance.getDataLSID();
					try {
						DataLineageVisualiser dataLineageVisualiser = new DataLineageVisualiser(
								metadataService, dataService,
								similarWorkflowRun, dataLSID);
						JFrame frame = new JFrame(TITLE);
						frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
						frame.getContentPane().add(dataLineageVisualiser);
						frame.setSize(FRAME_DIMENSION);
						frame.setVisible(true);
					} catch (DataLineageException ex) {
						logger.error(ex);
					}

					// display(sortedDataProvenance.get(date).getDataLSID());
				}
			}

		};

		list.addMouseListener(mouseListener);

		showSimilarData(list);
	}

	private void showSimilarData(final JList list) {
		similarDataListPanel.removeAll();
		similarDataListPanel.add(new JScrollPane(list));
		similarDataListPanel.revalidate();

		// JFrame frame = new JFrame();
		// frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		// frame.setTitle("Similar data");
		// frame.getContentPane().add(list);
		// frame.setSize(new Dimension(400, 200));
		// frame.setVisible(true);
	}

	private void highlightEdgesFrom(DataVertex dataVertex) {
		Set edges = graph.getEdges();
		for (Iterator it = edges.iterator(); it.hasNext();) {
			Edge edge = (Edge) it.next();
			Pair endpoints = edge.getEndpoints();
			if (endpoints.getFirst().equals(dataVertex))
				edge.setUserDatum(EDGE_COLOR_KEY, Color.BLACK, UserData.REMOVE);
			else
				edge.setUserDatum(EDGE_COLOR_KEY, Color.LIGHT_GRAY,
						UserData.REMOVE);
		}
	}

	private void downlightEdges() {
		Set edges = graph.getEdges();
		for (Iterator it = edges.iterator(); it.hasNext();) {
			Edge edge = (Edge) it.next();
			edge
					.setUserDatum(EDGE_COLOR_KEY, Color.LIGHT_GRAY,
							UserData.REMOVE);
		}
	}

	private PluggableRenderer createRenderer() {
		PluggableRenderer pr = new PluggableRenderer();
		pr.setVertexStringer(labeller);

		VertexShapeFunction vsf = new DataLineageVertexShapeFunction();
		pr.setVertexShapeFunction(vsf);
		pr.setVertexLabelCentering(true);

		DataVertexPaintFunction dataVertexPaintFunction = new DataVertexPaintFunction(
				pr);
		pr.setVertexPaintFunction(dataVertexPaintFunction);
		pr.setGraphLabelRenderer(new DefaultGraphLabelRenderer(Color.cyan,
				Color.cyan));

		pr.setEdgePaintFunction(new EdgePaintFunction() {
			public Paint getDrawPaint(Edge e) {
				Color k = (Color) e.getUserDatum(EDGE_COLOR_KEY);
				if (k != null)
					return k;
				return Color.blue;
			}

			public Paint getFillPaint(Edge e) {
				return null;
			}
		});

		pr.setEdgeStrokeFunction(new EdgeStrokeFunction() {
			protected final Stroke THIN = new BasicStroke(1);

			protected final Stroke THICK = new BasicStroke(2);

			public Stroke getStroke(Edge e) {
				Color c = (Color) e.getUserDatum(EDGE_COLOR_KEY);
				if (c == Color.LIGHT_GRAY)
					return THIN;
				else
					return THICK;
			}
		});
		return pr;
	}

	private DataThing getDataThing(String lsid) throws NoSuchLSIDException,
			DataServiceException {
		DataThing dataThing = dataThings.get(lsid);
		if (dataThing == null) {
			dataThing = dataService.fetchDataThing(lsid);
			dataThings.put(lsid, dataThing);
		}
		return dataThing;
	}

	private void populateDataGraph() {
		List<String> workflowOutputs = ontology
				.getWorkflowOutputs(workflowRunId);
		for (String output : workflowOutputs) {
			populateDataGraph(output);
		}
	}

	private void populateDataGraph(String id) {
		if (dataGraph.containsKey(id))
			return;
		Set<String> dataOrigins = getDataOrigins(id);
		dataGraph.put(id, dataOrigins);
		for (String dataOrigin : dataOrigins) {
			populateDataGraph(dataOrigin);
		}

	}

	private Set<AbstractDataVertex> populateGraph() throws UniqueLabelException {
		List<String> workflowOutputs = ontology
				.getWorkflowOutputs(workflowRunId);
		Set<AbstractDataVertex> roots = new HashSet<AbstractDataVertex>();
		for (String output : workflowOutputs) {
			AbstractDataVertex source = (AbstractDataVertex) labeller
					.getVertex(OUTPUT_PREFIX + getLocalId(output));
			if (source == null) {
				source = createDataVertex(output, DataType.OUTPUT);
				String finalOutputName = getOutputName(source.getDataLsid());
				source.setName(finalOutputName);
				graph.addVertex(source);
				labeller.setLabel(source, OUTPUT_PREFIX + source.getDataId());
				roots.add(source);
			}
			AbstractDataVertex target = (AbstractDataVertex) labeller
					.getVertex(getLocalId(output));
			if (target == null) {
				StringDataTypePair dataNameAndType = getDataNameAndType(source
						.getDataLsid());
				target = createDataVertex(output, dataNameAndType.getDataType());
				target.setName(dataNameAndType.getString());
				graph.addVertex(target);
				labeller.setLabel(target, target.getDataId());
			}
			graph.addEdge(new DirectedSparseEdge(source, target));
			// populateGraph(source);
			populateGraph(target);
		}
		return roots;
	}

	private void populateGraph(AbstractDataVertex source)
			throws UniqueLabelException {
		Set<String> dataOrigins = dataGraph.remove(source.getDataLsid());
		if (dataOrigins == null)
			return;
		for (String dataOrigin : dataOrigins) {
			AbstractDataVertex target = (AbstractDataVertex) labeller
					.getVertex(getLocalId(dataOrigin));
			if (target == null) {
				StringDataTypePair dataNameAndType = getDataNameAndType(dataOrigin);
				String dataName = dataNameAndType.getString();
				target = createDataVertex(dataOrigin, dataNameAndType
						.getDataType());
				target.setName(dataName);
				// target.setInputNames(getDataInputNames(dataOrigin));
				graph.addVertex(target);
				labeller.setLabel(target, target.getDataId());
			}
			graph.addEdge(new DirectedSparseEdge(source, target));
			populateGraph(target);
		}
	}

	private String getLocalId(String dataObject) {
		if (dataObject.startsWith(DataLineageConstants.DATA_ITEM_PREFIX))
			return dataObject.substring(DataLineageConstants.DATA_ITEM_PREFIX
					.length());
		if (dataObject.startsWith(DataLineageConstants.DATA_COLLECTION_PREFIX))
			return dataObject
					.substring(DataLineageConstants.DATA_COLLECTION_PREFIX
							.length());
		throw new IllegalArgumentException(dataObject);
	}

	private AbstractDataVertex createDataVertex(String data, DataType type) {
		if (data.startsWith(DataLineageConstants.DATA_ITEM_PREFIX)) {
			AbstractDataVertex dataItemVertex;
			switch (type) {
			case OUTPUT:
				dataItemVertex = new OutputItemVertexImpl(data
						.substring(DataLineageConstants.DATA_ITEM_PREFIX
								.length()));
				break;
			case INTERMEDIATE:
				dataItemVertex = new DataItemVertexImpl(data
						.substring(DataLineageConstants.DATA_ITEM_PREFIX
								.length()));
				break;
			case INPUT:
				dataItemVertex = new InputItemVertexImpl(data
						.substring(DataLineageConstants.DATA_ITEM_PREFIX
								.length()));
				break;
			case EMPTY:
				dataItemVertex = new EmptyVertexImpl(data
						.substring(DataLineageConstants.DATA_ITEM_PREFIX
								.length()),
						DataLineageConstants.DATA_ITEM_PREFIX);
				break;
			default:
				throw new IllegalArgumentException();
			}
			return dataItemVertex;
		}
		if (data.startsWith(DataLineageConstants.DATA_COLLECTION_PREFIX)) {
			AbstractDataVertex dataCollectionVertex;
			switch (type) {
			case OUTPUT:
				dataCollectionVertex = new OutputCollectionVertexImpl(data
						.substring(DataLineageConstants.DATA_COLLECTION_PREFIX
								.length()));
				break;
			case INTERMEDIATE:
				dataCollectionVertex = new DataCollectionVertexImpl(data
						.substring(DataLineageConstants.DATA_COLLECTION_PREFIX
								.length()));
				break;
			case INPUT:
				dataCollectionVertex = new InputCollectionVertexImpl(data
						.substring(DataLineageConstants.DATA_COLLECTION_PREFIX
								.length()));
				break;
			case EMPTY:
				dataCollectionVertex = new EmptyVertexImpl(data
						.substring(DataLineageConstants.DATA_COLLECTION_PREFIX
								.length()),
						DataLineageConstants.DATA_COLLECTION_PREFIX);
				break;
			default:
				throw new IllegalArgumentException();
			}
			return dataCollectionVertex;
		}
		throw new IllegalArgumentException(data);
	}

	private JPanel createTitlePanel(String workflowRunId) {
		JPanel panel = new JPanel(new BorderLayout());
		JPanel leftPanel = new JPanel(new GridLayout(0, 1, 0, 0));
		JPanel rightPanel = new JPanel(new GridLayout(0, 1, 0, 0));
		rightPanel.setBorder(new EmptyBorder(0, 10, 0, 0));
		panel.add(leftPanel, BorderLayout.WEST);
		panel.add(rightPanel, BorderLayout.CENTER);
		String queryString = "SELECT * " + "WHERE { <" + workflowRunId + "> <"
				+ ProvenanceVocab.START_TIME.getURI() + "> ?date "
				+ ". ?workflowRunId <" + ProvenanceVocab.RUNS_WORKFLOW.getURI()
				+ "> ?workflow . ?workflow <"
				+ ProvenanceVocab.WORKFLOW_INITIAL_LSID.getURI()
				+ "> ?workflowInitialId . OPTIONAL { ?workflow   <"
				+ ProvenanceVocab.WORKFLOW_AUTHOR.getURI()
				+ "> ?author } . OPTIONAL { ?workflow  <"
				+ ProvenanceVocab.WORKFLOW_TITLE.getURI()
				+ "> ?title } . OPTIONAL { ?workflow <"
				+ ProvenanceVocab.WORKFLOW_DESCRIPTION.getURI()
				+ "> ?description  } " + " }";

		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.create(query, ontModel);
		try {
			ResultSet results = qexec.execSelect();
			while (results.hasNext()) {
				QuerySolution sol = results.nextSolution();
				Literal date = sol.getLiteral("date");
				String dateString = date.getString();
				try {
					Date dateObject = Utils.parseDateLiteral(dateString);
					JTextField dateField = new JTextField(DateFormat
							.getDateTimeInstance().format(dateObject));
					dateField.setEditable(false);
					leftPanel.add(new JLabel("Date"));
					rightPanel.add(dateField);
				} catch (ParseException e) {
					logger.warn(e);
				}
				Literal optionalValue = sol.getLiteral("title");
				if (optionalValue != null) {
					String title = optionalValue.getString();
					JTextField titleField = new JTextField(title);
					titleField.setEditable(false);
					leftPanel.add(new JLabel("Title"));
					rightPanel.add(titleField);
				}
				optionalValue = sol.getLiteral("author");
				if (optionalValue != null) {
					String author = optionalValue.getString();
					JTextField authorField = new JTextField(author);
					authorField.setEditable(false);
					leftPanel.add(new JLabel("Author"));
					rightPanel.add(authorField);
				}
				optionalValue = sol.getLiteral("description");
				if (optionalValue != null) {
					String description = optionalValue.getString();
					JTextArea descriptionField = new JTextArea(description);
					descriptionField.setEditable(false);
					leftPanel.add(new JLabel("Description"));
					rightPanel.add(descriptionField);

				}
			}
		} finally {
			qexec.close();
		}
		return panel;
	}

	// private Set<String> getOutputDataOrigins(String outputLSID) {
	// String queryString = "PREFIX p: <"
	// + ProvenanceOntologyConstants.NS
	// + "> "
	// + "SELECT ?data "
	// + "WHERE { "
	// + "<"
	// + outputLSID
	// + "> "
	// + JenaProvenanceOntology
	// .bracketify(ProvenanceVocab.DATA_DERIVED_FROM.getURI())
	// + "?data . }";
	// Query query = QueryFactory.create(queryString);
	// QueryExecution qexec = QueryExecutionFactory.create(query, ontModel);
	// Set<String> dataOrigins = new HashSet<String>();
	// try {
	// ResultSet results = qexec.execSelect();
	// while (results.hasNext()) {
	// QuerySolution soln = results.nextSolution();
	// RDFNode data = soln.get("data");
	// String dataOrigin = data.asNode().getURI();
	// dataOrigins.add(dataOrigin);
	// }
	// } finally {
	// qexec.close();
	// }
	// return dataOrigins;
	// }

	private Set<String> getDataOrigins(String dataLSID) {
		String queryString = "SELECT ?data "
				+ "WHERE { "
				+ "<"
				+ dataLSID
				+ "> "
				+ JenaProvenanceOntology
						.bracketify(ProvenanceVocab.DATA_DERIVED_FROM.getURI())
				+ "?data . }";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.create(query, ontModel);
		Set<String> dataOrigins = new HashSet<String>();
		try {
			ResultSet results = qexec.execSelect();
			while (results.hasNext()) {
				QuerySolution soln = results.nextSolution();
				RDFNode data = soln.get("data");
				String dataOrigin = data.asNode().getURI();
				dataOrigins.add(dataOrigin);
			}
		} finally {
			qexec.close();
		}
		return dataOrigins;
	}

	private String getOutputName(String dataLSID) {
		return getDataName(dataLSID, false).getString();
	}

	private StringDataTypePair getDataNameAndType(String dataLSID) {
		return getDataName(dataLSID, true);
	}

	private StringDataTypePair getDataName(String dataLSID,
			boolean isIntermediate) {
		String dataName = null;
		String queryString = "PREFIX p: <"
				+ ProvenanceOntologyConstants.NS
				+ "> "
				+ "SELECT ?output "
				+ "WHERE { "
				+ "<"
				+ dataLSID
				+ "> "
				+ JenaProvenanceOntology
						.bracketify(ProvenanceVocab.OUTPUT_DATA_HAS_NAME
								.getURI()) + "?output . }";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.create(query, ontModel);
		try {
			ResultSet results = qexec.execSelect();
			while (results.hasNext()) {
				QuerySolution soln = results.nextSolution();
				RDFNode output = soln.get("output");
				dataName = output.asNode().getURI();
				dataName = parse(dataName, isIntermediate);
				if (dataName != null)
					break;
			}
		} finally {
			qexec.close();
		}
		if (dataName == null) {
			dataName = getWorkflowInputName(dataLSID);
			if (dataName == null)
				return new StringDataTypePair(null, DataType.EMPTY);
			return new StringDataTypePair(dataName, DataType.INPUT);
		}
		return new StringDataTypePair(dataName,
				isIntermediate ? DataType.INTERMEDIATE : DataType.OUTPUT);
	}

	private String getWorkflowInputName(String dataLSID) {
		String inputName = null;
		String queryString = "PREFIX p: <"
				+ ProvenanceOntologyConstants.NS
				+ "> "
				+ "SELECT ?input "
				+ "WHERE { "
				+ "<"
				+ dataLSID
				+ "> "
				+ JenaProvenanceOntology
						.bracketify(ProvenanceVocab.INPUT_DATA_HAS_NAME
								.getURI()) + "?input . }";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.create(query, ontModel);
		try {
			ResultSet results = qexec.execSelect();
			while (results.hasNext()) {
				QuerySolution soln = results.nextSolution();
				RDFNode input = soln.get("input");
				inputName = input.asNode().getURI();
				inputName = parseInput(inputName);
				if (inputName != null)
					break;
			}
		} finally {
			qexec.close();
		}
		return inputName;
	}

	private String parseInput(String dataName) {
		if (!dataName.startsWith(ProvenanceGenerator.WORKFLOW_NS))
			return null;
		dataName = dataName.split("#")[1];
		String[] split = dataName.split(Utils.INPUT_DIVIDER);
		dataName = split[1];
		return dataName;
	}

	private String parse(String dataName, boolean isIntermediate) {
		if (isIntermediate) {
			if (dataName.startsWith(ProvenanceGenerator.WORKFLOW_NS))
				return null;
		} else {
			if (dataName.startsWith(ProvenanceGenerator.PROCESS_NS))
				return null;
		}
		dataName = dataName.split("#")[1];

		String[] split = dataName.split(Utils.OUTPUT_DIVIDER);
		dataName = isIntermediate ? split[0] + ":" + split[1] : split[1];
		return dataName;
	}

	// private List<String> getDataInputNames(String dataLSID) {
	// List<String> inputNames = new ArrayList<String>();
	// String queryString = "PREFIX p: <"
	// + ProvenanceOntologyConstants.NS
	// + "> "
	// + "SELECT ?input "
	// + "WHERE { "
	// + "<"
	// + dataLSID
	// + "> "
	// + JenaProvenanceOntology
	// .bracketify(ProvenanceVocab.INPUT_DATA_HAS_NAME
	// .getURI()) + "?input . }";
	// Query query = QueryFactory.create(queryString);
	// QueryExecution qexec = QueryExecutionFactory.create(query, ontModel);
	// try {
	// ResultSet results = qexec.execSelect();
	// while (results.hasNext()) {
	// QuerySolution soln = results.nextSolution();
	// RDFNode input = soln.get("input");
	// String inputName = input.asNode().getURI();
	// inputName = Utils.inputLocalName(inputName);
	// inputNames.add(inputName);
	// }
	// } finally {
	// qexec.close();
	// }
	// return inputNames;
	// }

	public ImageIcon getIcon() {
		// TODO: change icon
		return TavernaIcons.windowDiagram;
	}

	public void onDisplay() {
		// empty body
	}

	public void onDispose() {
		// empty body
	}

	public class ReverseDateComparator implements Comparator<Date> {

		public int compare(Date date1, Date date2) {
			return date2.compareTo(date1);
		}

	}

	public class ResetAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public ResetAction() {
			putValue(SMALL_ICON, TavernaIcons.refreshIcon);
			putValue(NAME, "Reset");
			putValue(SHORT_DESCRIPTION, "Reset Edges");
		}

		public void actionPerformed(ActionEvent e) {
			downlightEdges();
		}

	}

	public class SimilarDataAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public SimilarDataAction() {
			putValue(SMALL_ICON, TavernaIcons.openIcon);
			putValue(NAME, "Similar Data");
			putValue(SHORT_DESCRIPTION, "Finds similar data in the logbook.");
		}

		public void actionPerformed(ActionEvent e) {
			Cursor hourGlass = new Cursor(Cursor.WAIT_CURSOR);
			setCursor(hourGlass);
			try {
				similarData(selectedData);
			} catch (MetadataServiceException ex) {
				logger.error(ex);
			}
			hourGlass = Cursor.getDefaultCursor();
			setCursor(hourGlass);
		}

	}

	public static class DataLineageStringLabeller extends StringLabeller {

		protected DataLineageStringLabeller(Graph g) {
			super(g);
		}

		@Override
		public String getLabel(ArchetypeVertex v) {
			return ((DataVertex) v).getName();
		}

	}

	public class VertexListener implements ItemListener {

		public void itemStateChanged(ItemEvent e) {
			Object item = e.getItem();
			if (e.getStateChange() != ItemEvent.SELECTED)
				return;
			if (item instanceof DataVertex) {
				DataVertex dataVertex = (DataVertex) item;
				similarDataAction.setEnabled(true);
				try {
					focusOn(dataVertex);
				} catch (NoSuchLSIDException ex) {
					logger.error(ex);
				} catch (DataServiceException ex) {
					logger.error(ex);
				}
			} else {
				similarDataAction.setEnabled(false);
			}
		}

	}

	public enum DataType {
		INPUT, OUTPUT, INTERMEDIATE, EMPTY
	}

	public class StringDataTypePair {
		private String string;

		private DataType dataType;

		public StringDataTypePair(String string, DataType dataType) {
			this.string = string;
			this.dataType = dataType;
		}

		public DataType getDataType() {
			return dataType;
		}

		public String getString() {
			return string;
		}

	}

}
