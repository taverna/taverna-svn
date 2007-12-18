package uk.org.mygrid.logbook.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.sf.taverna.utils.MyGridConfiguration;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.LogLevel;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ProvenanceGenerator;
import uk.org.mygrid.provenance.LogBookException;
import uk.org.mygrid.provenance.util.LogBookConfigurationNotFoundException;
import uk.org.mygrid.provenance.util.ProvenanceConfigurator;

public class PropertiesPanel extends Box implements UIComponentSPI {

	public enum MetadataDBType {
		// MYSQL, DERBY,
		JENA_MYSQL;

		public String toKaveType() {
			switch (this) {
			// case MYSQL:
			// return "BocaMySQL";
			// case DERBY:
			// return "BocaDerby";
			case JENA_MYSQL:
				return ProvenanceConfigurator.JENA_MYSQL;
			}
			throw new IllegalStateException();
		}

		public static MetadataDBType fromKaveType(String type) {
			// if (type.equals("BocaMySQL"))
			// return MYSQL;
			// if (type.equals("BocaDerby"))
			// return DERBY;
			if (type.equals(ProvenanceConfigurator.JENA_MYSQL))
				return JENA_MYSQL;
			throw new IllegalArgumentException(type);
		}

		public String toStringType() {
			switch (this) {
			// case MYSQL:
			// return "mysql";
			// case DERBY:
			// return "derby";
			case JENA_MYSQL:
				return "mysql";
			}
			throw new IllegalStateException();
		}

	}

	public enum DataDBType {
		MYSQL;
	}

	public static final String DEFAULT_METADATASERVICE_DB_NAME = "logbook_metadata";

	public static final String DEFAULT_DATASERVICE_DB_NAME = "logbook_data";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String LOG_BOOK_SETTINGS = "Log Book Settings";

	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger
			.getLogger(PropertiesPanel.class);

	public static final String MYSQL = "mysql";

	public static final String DERBY = "derby";

	final static Border VERTICAL_EMPTY_BORDER = BorderFactory
			.createEmptyBorder(10, 0, 10, 0);

	final static Border EMPHASISED_BORDER = BorderFactory
			.createRaisedBevelBorder();

	final static Border ETCHED_BORDER = BorderFactory.createEtchedBorder();

	final static Border COMPOUND_BORDER = BorderFactory.createCompoundBorder(
			VERTICAL_EMPTY_BORDER, EMPHASISED_BORDER);

	private ButtonGroup logLevelsGroup;

	private JCheckBox sameAsMetadata;

	private SaveAction saveAction = new SaveAction();

	Properties properties;

	JPanel metaPanel;

	JPanel dataPanel;

	JPanel experimenterPanel;

	JLabel provenanceSettingsLabel;

	final JTextField dataHostField = new JTextField();

	final JTextField dataDatabaseNameField = new JTextField();

	final JTextField dataUserField = new JTextField();

	final JPasswordField dataPasswordField = new JPasswordField();

	final JTextField metadataHostField = new JTextField();

	final JTextField metadataDatabaseNameField = new JTextField();

	final JTextField metadataUserField = new JTextField();

	final JPasswordField metadataPasswordField = new JPasswordField();

	private JPopupMenu logLevelMenu;

	private JComboBox metadataDbTypes = new JComboBox(MetadataDBType.values());

	private JComboBox dataDbTypes = new JComboBox(DataDBType.values());

	public static void main(String[] args) {
		MyGridConfiguration.getInstance();
		JFrame frame = new JFrame();
		PropertiesPanel propertiesPanel = new PropertiesPanel();
		frame.getContentPane().add(propertiesPanel);
		frame.setTitle(LOG_BOOK_SETTINGS);

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});
		frame.setSize(new Dimension(300, 300));
		frame.setVisible(true);

	}

	public PropertiesPanel() {
		super(BoxLayout.PAGE_AXIS);
		buildPropertiesPanel();
	}

	// private List<ProvenancePropertiesChangeListener> listeners;
	//    
	//    
	// public void addSavePerformedListener(ProvenancePropertiesChangeListener
	// listener) {
	// listeners.add(listener);
	// }
	//    
	// public void fireProvenancePropertyChange() {
	// for (ProvenancePropertiesChangeListener listener : listeners) {
	// listener.propertyChange(new ProvenancePropertiesChangeEvent());
	// }
	// }
	//    
	// public PropertiesPanel(ProvenancePropertiesChangeListener listener) {
	// this();
	// addSavePerformedListener(listener);
	// }

	public void buildPropertiesPanel() {
		Cursor hourglass = new Cursor(Cursor.WAIT_CURSOR);
		setCursor(hourglass);
		try {
			properties = ProvenanceConfigurator.getConfiguration();
		} catch (LogBookConfigurationNotFoundException e) {
			logger.debug("Using default donfiguration", e);
			properties = ProvenanceConfigurator.createDefaultConfiguration();
		}
		add(propertiesPanel());
		// getParent().setSize(new Dimension(1000, 1000));
		setMinimumSize(new Dimension(200, 200));
		setPreferredSize(new Dimension(300, 300));
		validate();
		Cursor normal = new Cursor(Cursor.DEFAULT_CURSOR);
		setCursor(normal);
	}

	public JPanel propertiesPanel() {
		final JPanel wholePanel = new JPanel();
		wholePanel.setLayout(new BorderLayout());
		wholePanel.setBorder(BorderFactory.createEtchedBorder());
		final JToolBar logLevels = buildLogLevelButtons();
		wholePanel.add(logLevels, BorderLayout.PAGE_START);

		final Box panel = new Box(BoxLayout.PAGE_AXIS);

		MetadataDBType type = MetadataDBType.fromKaveType(properties
				.getProperty(ProvenanceConfigurator.KAVE_TYPE_KEY,
						ProvenanceConfigurator.DEFAULT_KAVE_TYPE));
		final Box dataNonStretchPanel = buildPanel(DEFAULT_DATASERVICE_DB_NAME,
				ProvenanceConfigurator.MYSQL_PASSWORD,
				ProvenanceConfigurator.MYSQL_USER,
				ProvenanceConfigurator.MYSQL_CONNECTION_URL, dataHostField,
				dataDatabaseNameField, dataUserField, dataPasswordField, true,
				type);

		final Box metadataNonStretchPanel = buildPanel(
				DEFAULT_METADATASERVICE_DB_NAME,
				ProvenanceConfigurator.METADATA_MYSQL_PASSWORD,
				ProvenanceConfigurator.METADATA_MYSQL_USER,
				ProvenanceConfigurator.METADATA_MYSQL_CONNECTION_URL,
				metadataHostField, metadataDatabaseNameField,
				metadataUserField, metadataPasswordField, false, null);

		metadataHostField.getDocument().addDocumentListener(
				new TextListener(metadataHostField, dataHostField));
		metadataUserField.getDocument().addDocumentListener(
				new TextListener(metadataUserField, dataUserField));
		metadataPasswordField.getDocument().addDocumentListener(
				new TextListener(metadataPasswordField, dataPasswordField));

		final JTabbedPane databasesTabs = new JTabbedPane();
		final JTabbedPane tabs = new JTabbedPane();

		databasesTabs.addTab("Metadata", metadataNonStretchPanel);
		databasesTabs.addTab("Data", dataNonStretchPanel);

		final JPanel databasesPanel = new JPanel();
		databasesPanel.setLayout(new BorderLayout());
		databasesPanel.add(databasesTabs, BorderLayout.CENTER);
		databasesPanel.add(saveAndCancelPanel(), BorderLayout.SOUTH);

		tabs.addTab("Databases", TavernaIcons.databaseIcon, databasesPanel);

		panel.add(tabs);

		final JPanel externalPanel = new JPanel();
		externalPanel.setLayout(new BorderLayout());

		externalPanel.add(panel, BorderLayout.NORTH);

		externalPanel.add(new JPanel(), BorderLayout.CENTER);

		JScrollPane scrollPane = new JScrollPane(externalPanel);
		scrollPane.setPreferredSize(new Dimension(500, 200));
		scrollPane.setMinimumSize(new Dimension(500, 120));
		scrollPane.getViewport().setBackground(java.awt.Color.WHITE);

		wholePanel.add(scrollPane);
		return wholePanel;
	}

	private JPanel saveAndCancelPanel() {
		final JPanel savePanel = new JPanel();
		savePanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		// final JButton cancel = new JButton(new CancelAction());
		// savePanel.add(cancel);
		final JButton save = new JButton(saveAction);
		savePanel.add(save);
		return savePanel;
	}

	void setProperties() {
		properties.setProperty(ProvenanceConfigurator.KAVE_TYPE_KEY,
				((MetadataDBType) metadataDbTypes.getSelectedItem())
						.toKaveType());
		setConnectionProperty(
				ProvenanceConfigurator.METADATA_MYSQL_CONNECTION_URL,
				getMetadataDbType(), metadataHostField,
				metadataDatabaseNameField);
		setProperty(ProvenanceConfigurator.METADATA_MYSQL_USER,
				metadataUserField);
		setProperty(ProvenanceConfigurator.METADATA_MYSQL_PASSWORD,
				metadataPasswordField);
		setConnectionProperty(ProvenanceConfigurator.MYSQL_CONNECTION_URL,
				getDataDbType(), dataHostField, dataDatabaseNameField);
		setProperty(ProvenanceConfigurator.MYSQL_USER, dataUserField);
		setProperty(ProvenanceConfigurator.MYSQL_PASSWORD, dataPasswordField);
		String level = logLevelsGroup.getSelection().getActionCommand();
		properties.setProperty(ProvenanceConfigurator.LOGBOOK_LEVEL, level);
	}

	void setConnectionProperty(String key, String dbType, JTextField hostField,
			JTextField databaseField) {
		String host = hostField.getText();
		String databaseName = databaseField.getText();
		String connectionUrl = createConnectionUrl(dbType, host, databaseName);
		properties.setProperty(key, connectionUrl);
	}

	private String createConnectionUrl(String dbType, String host,
			String databaseName) {
		String hostString = host == null || host.equals("") ? "" : "//" + host
				+ "/";
		String connectionUrl = "jdbc:" + dbType + ":" + hostString
				+ databaseName;
		return connectionUrl;
	}

	void setProperty(String key, JTextField field) {
		properties.setProperty(key, field.getText());
	}

	private String getMetadataDbType() {
		return ((MetadataDBType) metadataDbTypes.getSelectedItem())
				.toStringType();
	}

	private String getDataDbType() {
		return dataDbTypes.getSelectedItem().toString().toLowerCase();
	}

	private Box buildPanel(String database, String password, String user,
			String connectionUrlKey, JTextField hostField,
			JTextField databaseNameField, JTextField userField,
			JPasswordField passwordField, boolean isData, MetadataDBType type) {

		String url = properties.getProperty(connectionUrlKey, MYSQL
				+ "localhost/" + database);
		final Box databasePanel = new Box(BoxLayout.PAGE_AXIS);
		databasePanel.setBorder(COMPOUND_BORDER);

		String[] urlDomain = url.split(":");
		String[] hostAndName = null;
		String host = null;
		String databaseName = null;
		if (type != null)
			metadataDbTypes.setSelectedItem(type);
		if (urlDomain != null && urlDomain.length == 3) {
			if (isData)
				dataDbTypes.setSelectedItem(urlDomain[1]);
			databaseName = urlDomain[2];
			String[] urlDomain2 = databaseName.split("//");
			if (urlDomain2 != null && urlDomain2.length == 2)
				hostAndName = urlDomain2[1].split("/", 2);

			if (hostAndName != null && hostAndName.length > 1) {
				host = hostAndName[0];
				databaseName = hostAndName[1];
			} else {
				if (database == null) {
					host = "localhost";
					databaseName = database;
				}
			}
		}

		databaseNameField.setText(databaseName);
		final JPanel innerPanel = new JPanel();
		innerPanel.setLayout(new GridLayout(0, 2, 0, 0));
		innerPanel.setBorder(ETCHED_BORDER);
		innerPanel.add(new SpacedLabel("Database Type "));
		innerPanel.add(isData ? dataDbTypes : metadataDbTypes);
		innerPanel.add(new SpacedLabel("Database Name "));
		innerPanel.add(databaseNameField);
		// innerPanel.setLayout(new BorderLayout());
		// innerPanel.add(twoComponentsPanel(new SpacedLabel("Database Type "),
		// isData ? dataDbTypes : metadataDbTypes, ETCHED_BORDER),
		// BorderLayout.NORTH);
		// innerPanel.add(twoComponentsPanel(new SpacedLabel("Database Name "),
		// databaseNameField, ETCHED_BORDER), BorderLayout.CENTER);
		final JPanel dbNamePanel = new JPanel();
		dbNamePanel.setLayout(new BorderLayout());
		dbNamePanel.add(innerPanel, BorderLayout.NORTH);
		databasePanel.add(dbNamePanel);
		dbNamePanel.add(dbPanel(host, user, password, hostField, userField,
				passwordField, isData), BorderLayout.CENTER);

		return databasePanel;
	}

	private JPanel dbPanel(String host, String user, String password,
			JTextField hostField, JTextField userField,
			JPasswordField passwordField, boolean isData) {
		final JPanel dbPanel = new JPanel();
		dbPanel.setLayout(new BorderLayout());
		final JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(0, 2, 0, 0));
		panel.setBorder(ETCHED_BORDER);
		dbPanel.add(panel, BorderLayout.NORTH);

		if (isData) {
			sameAsMetadata = new JCheckBox();
			sameAsMetadata.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					if (sameAsMetadata.isSelected()) {
						dataHostField.setText(metadataHostField.getText());
						dataHostField.setEnabled(false);
						dataUserField.setText(metadataUserField.getText());
						dataUserField.setEnabled(false);
						dataPasswordField.setText(new String(
								metadataPasswordField.getPassword()));
						dataPasswordField.setEnabled(false);
					} else {
						dataHostField.setEnabled(true);
						dataUserField.setEnabled(true);
						dataPasswordField.setEnabled(true);
					}
				}

			});
			sameAsMetadata.setText("Same as Metadata");
			panel.add(sameAsMetadata);
			panel.add(new JLabel(""));
		}
		hostField.setText(host);
		panel.add(new SpacedLabel("Host "));
		panel.add(hostField);

		userField.setText(properties.getProperty(user, "root"));
		panel.add(new SpacedLabel("User "));
		panel.add(userField);

		passwordField.setText(properties.getProperty(password, ""));
		panel.add(new SpacedLabel("Password "));
		panel.add(passwordField);
		return dbPanel;
	}

	private JToolBar buildLogLevelButtons() {
		final JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setRollover(true);
		toolbar.setMaximumSize(new Dimension(2000, 30));
		toolbar.setBorderPainted(true);

		logLevelMenu = new JPopupMenu("Log Level");
		String selectedLevel = properties.getProperty(
				ProvenanceConfigurator.LOGBOOK_LEVEL, LogLevel.DEFAULT_LEVEL);
		String[] levels = LogLevel.VISIBLE_LEVELS_LIST;
		logLevelsGroup = new ButtonGroup();
		for (int i = 0; i < levels.length; i++) {
			String levelString = levels[i];
			JRadioButtonMenuItem button = new JRadioButtonMenuItem(levelString);
			button.setMnemonic(KeyEvent.VK_B);
			button.setActionCommand(levelString);
			button.setText(LogLevel.getLabel(levelString));
			if (levelString.equals(selectedLevel))
				button.setSelected(true);

			button.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
					switch (e.getStateChange()) {
					case ItemEvent.SELECTED:
						activateLogLevel();
						break;
					default:
						break;
					}

				}
			});

			logLevelsGroup.add(button);
			logLevelMenu.add(button);
		}

		toolbar.add(new JButton(new LogLevelsAction()));

		return toolbar;

		// final JPanel container = new JPanel();
		// container.setLayout(new BorderLayout());
		// container.add(panel, BorderLayout.NORTH);
		// return container;
	}

	// public class CancelAction extends AbstractAction {
	//
	// public CancelAction() {
	// putValue(NAME, "Cancel");
	// }
	//        
	// public void actionPerformed(ActionEvent e) {
	//             
	// }
	//        
	// }

	public class LogLevelsAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public LogLevelsAction() {
			putValue(SMALL_ICON, LogBookIcons.levelsIcon);
			putValue(NAME, "Log Level");
			putValue(SHORT_DESCRIPTION, "Log Level...");
		}

		public void actionPerformed(ActionEvent e) {
			Component sourceComponent = (Component) e.getSource();
			logLevelMenu.show(sourceComponent, 0, sourceComponent.getHeight());
		}

	}

	JPanel twoComponentsPanel(JComponent c1, JComponent c2, Border border) {
		final JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		GridBagConstraints gridBagConstraintsLeft = new GridBagConstraints();
		gridBagConstraintsLeft.gridx = 0;
		gridBagConstraintsLeft.anchor = GridBagConstraints.WEST;
		gridBagConstraintsLeft.insets = new Insets(0, 2, 0, 2);
		gridBagConstraintsLeft.weighty = 0.0;
		gridBagConstraintsLeft.gridy = 1;

		GridBagConstraints gridBagConstraintsRight = new GridBagConstraints();
		gridBagConstraintsRight.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraintsRight.gridy = 1;
		gridBagConstraintsRight.weightx = 1.0;
		gridBagConstraintsRight.weighty = 0.0;
		gridBagConstraintsRight.anchor = GridBagConstraints.WEST;
		gridBagConstraintsRight.gridx = 1;
		panel.setBorder(border);
		panel.add(c1, gridBagConstraintsLeft);
		panel.add(c2, gridBagConstraintsRight);
		return panel;
	}

	public void attachToModel(ScuflModel model) {
		// open = true;
		// Properties p = EnactorInvocationBrowserModel
		// .getProperties("provenance");
		// if (p == null) {
		// Object[] options = { "Ok" };
		// JOptionPane
		// .showOptionDialog(
		// null,
		// "The Required properties file provenance.properties does not exist\n"
		// + "you will be unable to view or store provenance data",
		// "Disabled", JOptionPane.YES_OPTION,
		// JOptionPane.QUESTION_MESSAGE, null, options,
		// options[0]);
		//
		// return;
		// }
		//
		// if (p.getProperty("mygrid.kave.type") == null) {
		// Object[] options = { "Ok" };
		// JOptionPane
		// .showOptionDialog(
		// null,
		// "The Required property mygrid.kave.type in the file
		// provenance.properties does not exist \n"
		// + "you will be unable to view or store provenance data",
		// "Disabled", JOptionPane.YES_OPTION,
		// JOptionPane.QUESTION_MESSAGE, null, options,
		// options[0]);
		//
		// return;
		// }

		Cursor hourglass = new Cursor(Cursor.WAIT_CURSOR);
		setCursor(hourglass);

		new Thread() {
			public void run() {
				try {
					buildPropertiesPanel();
				} catch (Exception ex) {
					logger.error(ex);
				}
			}
		}.start();

	}

	public void detachFromModel() {
		// do nothing
	}

	public ImageIcon getIcon() {
		return TavernaIcons.databaseIcon;
	}

	// private void validate(Properties properties)
	// throws StoreValidationException {
	// MySQLDataService.validate(properties);
	// JenaMetadataService metadataService = new JenaMetadataService();
	// properties.setProperty(ProvenanceConfigurator.KAVE_TYPE_KEY,
	// ProvenanceConfigurator.DEFAULT_KAVE_TYPE);
	// metadataService.setConfiguration(properties);
	// try {
	// Connection connection = metadataService.connect();
	// connection.close();
	// } catch (Exception e) {
	// throw new StoreValidationException(e);
	// }
	// }

	private void activateLogLevel() {
		try {
			ProvenanceGenerator provenanceGenerator = ProvenanceGenerator
					.getInstance();
			String level = logLevelsGroup.getSelection().getActionCommand();
			int logLevel = LogLevel.toLogLevel(level);
			if (logLevel > LogLevel.IO) {
				Object[] options = { "Ok" };
				JOptionPane
						.showOptionDialog(
								null,
								"Log levels higher than input/output might slow down Taverna for large workflows or large data.",
								"Warning", JOptionPane.OK_OPTION,
								JOptionPane.WARNING_MESSAGE, null, options,
								options[0]);
				provenanceGenerator.setLogLevel(logLevel);
				updateConfigurationFile(ProvenanceConfigurator.LOGBOOK_LEVEL,
						level);
			}
		} catch (LogBookException e) {
			logger.error(e);
		} catch (FileNotFoundException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
	}

	private void save(Properties configuration) throws FileNotFoundException,
			IOException {
		File file = ProvenanceConfigurator.getConfigurationFile();
		FileOutputStream fileOutputStream;
		fileOutputStream = new FileOutputStream(file);
		configuration.store(fileOutputStream, PropertiesPanel.this.toString());
		fileOutputStream.close();
	}

	private void updateConfigurationFile(String key, String value)
			throws FileNotFoundException, IOException {
		Properties configuration = new Properties();
		File file = ProvenanceConfigurator.getConfigurationFile();
		FileInputStream fileInputStream = new FileInputStream(file);
		configuration.load(fileInputStream);
		fileInputStream.close();
		FileOutputStream fileOutputStream;
		fileOutputStream = new FileOutputStream(file);
		configuration.setProperty(key, value);
		configuration.store(fileOutputStream, PropertiesPanel.this.toString());
		fileOutputStream.close();
	}

	public void validateDatabases() throws DatabaseValidationException {
		String host = metadataHostField.getText();
		String databaseName = metadataDatabaseNameField.getText();
		String user = metadataUserField.getText();
		char[] password = metadataPasswordField.getPassword();
		validateDatabase(getMetadataDbType(), host, databaseName, user,
				password);

		host = dataHostField.getText();
		databaseName = dataDatabaseNameField.getText();
		user = dataUserField.getText();
		password = dataPasswordField.getPassword();
		validateDatabase(getDataDbType(), host, databaseName, user, password);
	}

	private void validateDatabase(String dbType, String host,
			String databaseName, String user, char[] password)
			throws DatabaseValidationException {
		String connectionURL = createConnectionUrl(dbType, host, databaseName);
		String driver = dbType.equals(MYSQL) ? ProvenanceConfigurator.MYSQL_JDBC_DRIVER
				: ProvenanceConfigurator.DERBY_JDBC_DRIVER;
		try {
			Class.forName(driver).newInstance();
		} catch (Exception e) {
			DatabaseValidationException exception = new DatabaseValidationException(
					"Can't load driver " + driver + ": " + e.getMessage());
			exception.setDatabaseName(connectionURL);
			throw exception;
		}
		try {
			DriverManager.getConnection(connectionURL, user, new String(
					password));
		} catch (SQLException e) {
			String testConnectionURL = createConnectionUrl(dbType, host, "test"); // FIXME
			try {
				Connection connection = DriverManager.getConnection(
						testConnectionURL, user, new String(password));
				Statement statement = connection.createStatement();
				Object[] options = { "Yes", "No" };
				int option = JOptionPane
						.showOptionDialog(null,
								"Could not connect to database " + databaseName
										+ ". Do you want to create it?",
								"Create database?", JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE, null, options,
								options[0]);
				if (option == JOptionPane.YES_OPTION) {
					statement.execute("create database " + databaseName);
					statement.close();
					connection.close();
				}
			} catch (SQLException e1) {
				DatabaseValidationException exception = new DatabaseValidationException(
						"Could not connect to nor create database "
								+ connectionURL + ": " + e1.getMessage());
				exception.setDatabaseName(connectionURL);
				throw exception;
			}
		}

	}

	public String getName() {
		return LOG_BOOK_SETTINGS;
	}

	public class TextListener implements DocumentListener {

		private JTextField sourceTextField;

		private JTextField targetTextField;

		/**
		 * @param sourceTextField
		 * @param targetTextField
		 */
		public TextListener(JTextField sourceTextField,
				JTextField targetTextField) {
			super();
			this.sourceTextField = sourceTextField;
			this.targetTextField = targetTextField;
		}

		public void changedUpdate(DocumentEvent e) {
			update();

		}

		public void insertUpdate(DocumentEvent e) {
			update();

		}

		public void removeUpdate(DocumentEvent e) {
			update();

		}

		private void update() {
			if (sameAsMetadata != null && sameAsMetadata.isSelected())
				targetTextField.setText(sourceTextField.getText());
		}

	}

	public class SaveAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Logger for this class
		 */
		private final Logger logger = Logger.getLogger(SaveAction.class);

		public SaveAction() {

			putValue(SMALL_ICON, TavernaIcons.saveIcon);
			putValue(NAME, "Save");
			putValue(SHORT_DESCRIPTION, "Save Settings");

		}

		public void actionPerformed(ActionEvent e) {
			Cursor hourglass = new Cursor(Cursor.WAIT_CURSOR);
			setCursor(hourglass);
			if (dataDatabaseNameField.getText().equals(
					metadataDatabaseNameField.getText())) {
				Object[] options = { "Ok" };
				JOptionPane.showOptionDialog(null,
						"Databases for metadata and data MUST differ:\n"
								+ "please change names.", "Error",
						JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE, null,
						options, options[0]);
			} else {
				setProperties();
				try {
					validateDatabases();
				} catch (DatabaseValidationException ex) {
					Object[] options = { "Yes", "No" };
					int choice = JOptionPane.showOptionDialog(null,
							"Could not validate database "
									+ ex.getDatabaseName()
									+ "\nDo you want to continue?", "Warning",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE, null, options,
							options[1]);
					switch (choice) {
					case 1:
						Cursor normal = new Cursor(Cursor.DEFAULT_CURSOR);
						setCursor(normal);
						return;
					default:
						break;
					}
				}
				try {
					save(properties);
					Object[] options = { "Ok" };
					JOptionPane
							.showOptionDialog(
									null,
									"Changes will take effect only after restarting Taverna.",
									"Warning", JOptionPane.OK_OPTION,
									JOptionPane.INFORMATION_MESSAGE, null,
									options, options[0]);
				} catch (FileNotFoundException ex) {
					logger.error(ex);
				} catch (IOException ex) {
					logger.error(ex);
				}
			}
			Cursor normal = new Cursor(Cursor.DEFAULT_CURSOR);
			setCursor(normal);
		}
	}

	// public class CancelAction extends AbstractAction {
	//
	// public CancelAction() {
	// putValue(NAME, "Cancel");
	// }
	//        
	// public void actionPerformed(ActionEvent e) {
	//             
	// }
	//        
	// }

	/**
	 * A JLabel with a little horizontal space on the left.
	 */
	static class SpacedLabel extends JLabel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		SpacedLabel(String label) {
			super(label);
			setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
		}

	}

	class MyComboBox extends JComboBox {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		String[] entries;

		public MyComboBox(String[] entries, boolean isEditable) {
			super();
			this.entries = entries;
			setModel(new DefaultComboBoxModel(entries));
			setSelectedIndex(0);
			setEditable(isEditable);
			addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (e.getActionCommand().equals("comboBoxChanged"))
						((DefaultComboBoxModel) getModel()).insertElementAt(
								(String) getSelectedItem(), 0);
				}
			});
			setBackground(Color.WHITE);
		}

		public MyComboBox(String[] entries) {
			this(entries, true);
		}

		public MyComboBox(String entry) {
			this(new String[] { entry });
		}

		public MyComboBox() {
			this(new String[] { "" });
		}

		public int getIndex(String v) {
			for (int i = 0; i < entries.length; i++)
				if (entries[i].equals(v))
					return i;
			return -1;
		}

	}

	public void onDisplay() {
		// TODO Auto-generated method stub

	}

	public void onDispose() {
		// TODO Auto-generated method stub

	}

}
