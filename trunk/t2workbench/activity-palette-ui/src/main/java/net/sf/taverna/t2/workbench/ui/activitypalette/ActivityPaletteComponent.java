package net.sf.taverna.t2.workbench.ui.activitypalette;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.help.CSH;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeModel;

import net.sf.taverna.t2.partition.ActivityItem;
import net.sf.taverna.t2.partition.ActivityQueryFactory;
import net.sf.taverna.t2.partition.AddQueryActionHandler;
import net.sf.taverna.t2.partition.PartitionAlgorithm;
import net.sf.taverna.t2.partition.PartitionAlgorithmSetSPI;
import net.sf.taverna.t2.partition.PartitionAlgorithmSetSPIRegistry;
import net.sf.taverna.t2.partition.PropertyExtractorRegistry;
import net.sf.taverna.t2.partition.PropertyExtractorSPIRegistry;
import net.sf.taverna.t2.partition.Query;
import net.sf.taverna.t2.partition.QueryFactory;
import net.sf.taverna.t2.partition.QueryFactoryRegistry;
import net.sf.taverna.t2.partition.RootPartition;
import net.sf.taverna.t2.partition.SetModelChangeListener;
import net.sf.taverna.t2.partition.algorithms.LiteralValuePartitionAlgorithm;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;

/**
 * Contains the {@link ActivityTree} which shows the available activities
 * partitioned by their properties using a {@link PartitionAlgorithm}. Contains
 * a {@link JMenu} which allows the user to filter the activities by their
 * properties
 * 
 * @author Ian Dunlop
 * @author Stuart Owen
 * 
 */
@SuppressWarnings("serial")
public class ActivityPaletteComponent extends JPanel implements UIComponentSPI {
	/** All the available activities filtered by the selected properties */
	private JTree activityTree;
	/** The different properties for each type of activity */
	private PropertyExtractorRegistry propertyExtractorRegistry;
	/**
	 * Allows the user to filter the activities by selecting a property. This
	 * then reconfigures the tree model with a different set of
	 * {@link PartitionAlgorithm}s
	 */
	private JMenu mainMenu;
	private RootPartition<?> rootPartition;
	private List<PartitionAlgorithm<?>> selectedPartitions = new ArrayList<PartitionAlgorithm<?>>();
	JMenu firstMenu;
	JMenu secondMenu;
	Map<PartitionAlgorithm<?>,JMenuItem> firstAlgorithmToMenuItemMap = new HashMap<PartitionAlgorithm<?>,JMenuItem>();
	Map<PartitionAlgorithm<?>,JMenuItem> secondAlgorithmToMenuItemMap = new HashMap<PartitionAlgorithm<?>,JMenuItem>();
	private static ActivityPaletteComponent instance = new ActivityPaletteComponent();


	public static ActivityPaletteComponent getInstance() {
		return instance;
	}
	/**
	 * Sets the layout as {@link BorderLayout}. Then calls
	 * {@link #initialise()} to create the {@link ActivityTree}. Adds a
	 * {@link JMenuBar} to allow the user to filter the activities
	 */
	private ActivityPaletteComponent() {
		CSH
				.setHelpIDString(this,
						"net.sf.taverna.t2.workbench.ui.activitypalette.ActivityPaletteComponent");
		setLayout(new BorderLayout());
		initialise();
		createMainMenu();
		JPanel menuPanel = new JPanel();
		menuPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		JMenuBar menuBar = new JMenuBar();
		CSH
				.setHelpIDString(
						menuBar,
						"net.sf.taverna.t2.workbench.ui.activitypalette.ActivityPaletteComponent-menuBar");
		menuBar.add(mainMenu);
		
		menuBar.setVisible(true);

		menuPanel.add(menuBar);

		add(menuPanel, BorderLayout.PAGE_START);
		add(new JScrollPane(activityTree), BorderLayout.CENTER);
	}

	/**
	 * Creates the {@link ActivityTree} which displays all the activities. Gets
	 * an instance of {@link PropertyExtractorSPIRegistry} which contains all
	 * the properties which the activities can be filtered on. Creates the
	 * {@link RootPartition}, basically a {@link TreeModel} which the
	 * {@link ActivityTree} needs. Then adds the initial queries (probably one
	 * for each activity)
	 */
	private void initialise() {

		propertyExtractorRegistry = PropertyExtractorSPIRegistry.getInstance();
		rootPartition = getPartition(propertyExtractorRegistry);
		initQueries(rootPartition);
		// createSorts();
		activityTree = new ActivityTree(rootPartition);
		activityTree.setCellRenderer(new ActivityTreeCellRenderer());
		((RootPartition) activityTree.getModel().getRoot())
				.setChildPartitionOrder(new Comparator<String>() {

					public int compare(String o1, String o2) {
						// TODO Auto-generated method stub
						if (o1.compareTo(o2) > 0) {
							return 1;
						}
						return 0;
					}

				});
	}

	/**
	 * Create all the {@link Query}s for the activities and add them to the
	 * {@link SetModelChangeListener} which the original {@link RootPartition}
	 * has. This allows the {@link ActivityTree} to know about the queries and
	 * re-run them whenever the user selects a different filter. ie. when the
	 * {@link ActivityTree} is given a new model (remember that the model is a
	 * {@link RootPartition}. The actual queries do not change but the
	 * partition does
	 * 
	 * @param partition
	 */
	private void initQueries(RootPartition<?> partition) {

		List<Query<?>> queries = QueryFactoryRegistry.getInstance()
				.getQueries();

		for (Query<?> query : queries) {
			query.addSetModelChangeListener((SetModelChangeListener) partition
					.getSetModelChangeListener());
			partition.getSetModelChangeListener().addQuery(query);
		}
	}

	/**
	 * Add a menu item for each type of {@link PartitionAlgorithm}. When one is
	 * selected the {@link ActivityTree} is given a new model based on the
	 * selected {@link PartitionAlgorithm} which means it reruns the
	 * {@link Query}s. When a query is selected the type is displayed in a
	 * {@link JLabel} next to the drop down
	 */
	private void createMainMenu() {
		firstMenu = new JMenu("First grouping ...");
		secondMenu = new JMenu("Second grouping ...");
		ButtonGroup firstGroup = new ButtonGroup();
		ButtonGroup secondGroup = new ButtonGroup();

		mainMenu = new JMenu("Temp Configure Menu ...");
		mainMenu
				.setToolTipText("Open this menu and select a property to query the activities");
		mainMenu.setMnemonic(KeyEvent.VK_Q);
		for (final PartitionAlgorithm<?> algorithm : getAlgorithms()) {

			if (algorithm instanceof LiteralValuePartitionAlgorithm) {
				final LiteralValuePartitionAlgorithm litAlgorithm = (LiteralValuePartitionAlgorithm)algorithm;
				
				final JMenuItem item1 = new JRadioButtonMenuItem();
				final JMenuItem item2 = new JRadioButtonMenuItem();
				
				firstAlgorithmToMenuItemMap.put(litAlgorithm,item1);
				secondAlgorithmToMenuItemMap.put(litAlgorithm, item2);
				
				Action selectItemAction = new AbstractAction() {

					private static final long serialVersionUID = -6640292235035043107L;

					public void actionPerformed(ActionEvent e) {
						if (e.getSource() instanceof JMenuItem) {
							handleSelectedPartition(e.getSource(),litAlgorithm);
						}
					}

					private void handleSelectedPartition(Object source,PartitionAlgorithm<?>alg) {
						if (source == item1) {
							secondAlgorithmToMenuItemMap.get(selectedPartitions.get(0)).setEnabled(true);
							selectedPartitions.set(0, alg);
							secondAlgorithmToMenuItemMap.get(alg).setEnabled(false);
						}
						if (source == item2) {
							firstAlgorithmToMenuItemMap.get(selectedPartitions.get(1)).setEnabled(true);
							selectedPartitions.set(1, alg);
							firstAlgorithmToMenuItemMap.get(alg).setEnabled(false);
						}	
						
						final RootPartition<ActivityItem> root = (RootPartition<ActivityItem>) activityTree.getModel();
						final List<PartitionAlgorithm<?>> partitions = new ArrayList<PartitionAlgorithm<?>>();
						for (PartitionAlgorithm<?> selectedAlgorithm : selectedPartitions) {
							if (!(selectedAlgorithm instanceof NoneSelectedPartitionAlgorithm)) {
								partitions.add(selectedAlgorithm);
							}
						}
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								Cursor oldCursor = getCursor();
								Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
								setCursor(hourglassCursor);
								root.setPartitionAlgorithmList(partitions);
								setCursor(oldCursor);
							}
						});
						
					}
				};
				item1.setAction(selectItemAction);
				item2.setAction(selectItemAction);
				item1.setText(litAlgorithm.getPropertyName());
				item2.setText(litAlgorithm.getPropertyName());
				if (!(litAlgorithm instanceof NoneSelectedPartitionAlgorithm)) firstMenu.add(item1);
				secondMenu.add(item2);
				firstGroup.add(item1);
				secondGroup.add(item2);
				
				if (litAlgorithm.getPropertyName().equalsIgnoreCase("type")) {
					selectedPartitions.add(0,litAlgorithm);
					item1.setSelected(true);
					item2.setEnabled(false);
				}
				if (litAlgorithm instanceof NoneSelectedPartitionAlgorithm) {
					selectedPartitions.add(litAlgorithm);
					item2.setSelected(true);
				}
			}
		}
		
		mainMenu.add(createAddActivity());
		mainMenu.addSeparator();
		mainMenu.add(firstMenu);
		mainMenu.add(secondMenu);

	}

	

	private JMenu createAddActivity() {
		JMenu addQueryMenu = new JMenu("Add...");
		addQueryMenu.setToolTipText("Open this menu to add a new Query");
		for (QueryFactory factory : QueryFactoryRegistry.getInstance()
				.getInstances()) {
			if (factory instanceof ActivityQueryFactory) {
				ActivityQueryFactory af = (ActivityQueryFactory) factory;
				if (af.hasAddQueryActionHandler()) {
					AddQueryActionHandler handler = af
							.getAddQueryActionHandler();
					handler
							.setSetModelChangeListener((SetModelChangeListener<ActivityItem>) rootPartition
									.getSetModelChangeListener());
					addQueryMenu.add(handler);
				}
			}
		}
		return addQueryMenu;
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return "Activity Palette";
	}

	public void onDisplay() {
		// TODO Auto-generated method stub

	}

	public void onDispose() {
		// TODO Auto-generated method stub

	}

	/**
	 * The {@link RootPartition} represents the {@link TreeModel} in the
	 * {@link ActivityTree}. It has a list of all the ways that activities can
	 * be filtered (all the {@link PartitionAlgorithm}s) and also has a
	 * {@link PropertyExtractorRegistry} which knows all the different
	 * properties for each type of activity
	 * 
	 * @param <ActivityItem>
	 * @param reg
	 * @return {@link RootPartition} which is the {@link TreeModel} for the
	 *         {@link ActivityTree}
	 */
	private <ActivityItem extends Comparable> RootPartition<?> getPartition(
			PropertyExtractorRegistry reg) {
		return new RootPartition<ActivityItem>(DefaultPartitionAlgorithms
				.getPartitionAlgorithms(), reg);
	}

	/**
	 * Loop through all the available {@link PartitionAlgorithm}s and create a
	 * master set. This is all the possible ways that the activities can be
	 * filtered
	 * 
	 * @return A union of all the {@link PartitionAlgorithm} sets
	 */
	private List<PartitionAlgorithm<?>> getAlgorithms() {
		// TODO use the SPI instead of hard coding when there are algorithms
		// ready
		List<PartitionAlgorithmSetSPI> instances = PartitionAlgorithmSetSPIRegistry
				.getInstance().getInstances();
		Set<PartitionAlgorithm<?>> partitionAlgorithmSet = new HashSet<PartitionAlgorithm<?>>();
		
		for (PartitionAlgorithmSetSPI instance : instances) {
			Set<PartitionAlgorithm<?>> partitonAlgorithms = instance
					.getPartitionAlgorithms();
			partitionAlgorithmSet.addAll(partitonAlgorithms);
		}
		List<PartitionAlgorithm<?>> partitionAlgorithmList = new ArrayList<PartitionAlgorithm<?>>();
		partitionAlgorithmList.add(new NoneSelectedPartitionAlgorithm());
		for (PartitionAlgorithm<?> algorithm : partitionAlgorithmSet) {
			partitionAlgorithmList.add(algorithm);
		}
		
		return partitionAlgorithmList;
	}

}
