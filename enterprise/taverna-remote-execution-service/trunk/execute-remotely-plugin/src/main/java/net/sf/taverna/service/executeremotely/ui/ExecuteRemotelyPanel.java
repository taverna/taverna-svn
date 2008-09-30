package net.sf.taverna.service.executeremotely.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.sf.taverna.service.executeremotely.ExecuteRemotelyConf;
import net.sf.taverna.service.executeremotely.RESTService;
import net.sf.taverna.service.executeremotely.UILogger;
import net.sf.taverna.service.rest.client.RESTContext;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.shared.ShadedLabel;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;

public class ExecuteRemotelyPanel extends JPanel implements
        WorkflowModelViewSPI {
        
        private static final long serialVersionUID = -3941327167079803885L;
        
        private static Logger logger = Logger.getLogger(ExecuteRemotelyPanel.class);
        
        private ExecuteRemotelyConf conf = ExecuteRemotelyConf.getInstance();
        
        public ScuflModel model;
        
        public RESTContext context;
        
        private LogPanel uiLog = new LogPanel();
        
        public JobsPanel jobs = new JobsPanel(uiLog);
        
        public JComboBox services;
        
        private RESTService service;
        
        private int row = -1;
        
        private JButton runButton;
        
        private JButton refreshButton;
        
        private JButton editButton;
        
        private JButton removeButton;
        
        private JButton connectButton;
        
        public ExecuteRemotelyPanel() {
                super(new GridBagLayout());
                addHeader();
                addConnection();
                addRunButton();
                addRefreshButton();
                addJobs();
                //addFiller();
                addLogs();
                updateServiceList();
                checkButtons();
        }
        
        public ImageIcon getIcon() {
                return TavernaIcons.runIcon;
        }
        
        @Override
        public String getName() {
                return "Execute remotely";
        }
        
        public void onDisplay() {
                updateServiceList();
        }
        
        public void attachToModel(ScuflModel model) {
                this.model = model;
        }
        
        public void detachFromModel() {
                model = null;
        }
        
        public void onDispose() {
        }
        
        public void addService(String name, String uri, String username,
                String password) {
                RESTContext service = new RESTContext(uri, username, password);
                service.setName(name);
                conf.addService(service);
                updateServiceList();
                services.setSelectedItem(service);
        }
        
        public void updateService(RESTContext service, String name, String uri,
                String username, String password) {
                if (!(service.getBaseURI().toString().equals(uri) && service.getUsername().equals(
                        username))) {
                        // Changed uri/username means we have to make a new service
                        removeService(service);
                        addService(name, uri, username, password);
                        return;
                }
                if (!password.equals(service.getPassword())) {
                        service.setPassword(password);
                }
                if (!name.equals(service.getName())) {
                        service.setName(name);
                        // updateServiceList();
                        services.setSelectedItem(service);
                }
        }
        
        public void removeService(RESTContext service) {
                conf.removeService(service);
                services.removeItem(service);
                updateServiceList();
        }
        
        public void updateServiceList() {
                DefaultComboBoxModel servicelist =
                        new DefaultComboBoxModel(conf.getServices());
                services.setModel(servicelist);
                connectButton.setEnabled(conf.getServices().length>0);
        }
        
        protected void addHeader() {
                GridBagConstraints c = new GridBagConstraints();
                c.gridx = 0;
                c.gridy = ++row;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.weightx = 0.1;
                c.anchor = GridBagConstraints.NORTHWEST;
                c.gridwidth = GridBagConstraints.REMAINDER;
                add(new ShadedLabel("Execute remotely", ShadedLabel.TAVERNA_GREEN), c);
        }
        
        protected void addConnection() {
                GridBagConstraints c = new GridBagConstraints();
                c.gridx = 0;
                c.gridy = ++row;
                c.anchor = GridBagConstraints.LINE_END;
                c.ipadx = 5;
                c.ipady = 5;
                add(new JLabel("Taverna server:"), c);
                
                c.weightx = 0.1;
                c.anchor = GridBagConstraints.LINE_START;
                c.fill = GridBagConstraints.HORIZONTAL;
                c.gridx = GridBagConstraints.RELATIVE;
                
                services = new JComboBox(conf.getServices());
                services.addActionListener(new ServiceSelectionListener());
                add(services, c);
                c.weightx = 0;
        
                Action connectService = new ConnectServiceAction();
                connectButton = new JButton(connectService);
                add(connectButton,c);
                
                Action addService = new NewServiceAction();
                add(new JButton(addService), c);
                
                Action editService = new EditServiceAction();
                editButton = new JButton(editService);
                add(editButton, c);
                
                Action removeService = new RemoveServiceAction();
                removeButton = new JButton(removeService);
                add(removeButton, c);
        }
        
        protected void addRunButton() {
                GridBagConstraints c = new GridBagConstraints();
                c.gridx = 0;
                c.gridy = ++row;
                runButton = new JButton(new RunWorkflowAction());
                runButton.setEnabled(false);
                add(runButton, c);
        }
        
        private void checkButtons() {
                //runButton.setEnabled(context != null && model != null);
                //refreshButton.setEnabled(context != null);
                removeButton.setEnabled(context != null);
                editButton.setEnabled(context != null);
                
                refreshButton.invalidate();
                removeButton.invalidate();
                editButton.invalidate();
        }
        
        private void addRefreshButton() {
                GridBagConstraints c = new GridBagConstraints();
                c.gridx = 1;
                c.gridy = row;
                c.anchor = GridBagConstraints.WEST;
                refreshButton = new JButton(new RefreshAction());
                refreshButton.setEnabled(false);
                add(refreshButton, c);
        }
        
        
        protected void addJobs() {
                GridBagConstraints c = new GridBagConstraints();
                c.gridx = 0;
                c.gridwidth = GridBagConstraints.REMAINDER;
                c.gridy = ++row;
                c.fill = GridBagConstraints.BOTH;
                c.weightx = 0.1;
                c.weighty = 0.1;
                JScrollPane scrollPane = new JScrollPane(jobs);
                add(scrollPane, c);
        }
        
        protected void addFiller() {
                GridBagConstraints c = new GridBagConstraints();
                c.gridx = 0;
                c.gridy = ++row;
                c.fill = GridBagConstraints.BOTH;
                c.weightx = 0.01;
                c.weighty = 0.01;
                c.anchor = GridBagConstraints.SOUTHEAST;
                c.gridwidth = GridBagConstraints.REMAINDER;
                add(new JPanel(), c);
        }
        
        protected void addLogs() {
                GridBagConstraints c = new GridBagConstraints();
                c.anchor = GridBagConstraints.CENTER;
                c.fill = GridBagConstraints.BOTH;
                c.weightx = 0.1;
                c.weighty = 0.0;
                c.gridwidth = GridBagConstraints.REMAINDER;
                c.gridx = 0;
                c.gridy = ++row;
                JScrollPane scrollPane = new JScrollPane(uiLog);
                scrollPane.setMinimumSize(new Dimension(0, 100));
                add(scrollPane, c);
        }
        
        protected void setContext(RESTContext context,boolean connect) {
                this.service = new RESTService(context); // copy
                this.context = context;
                conf.setSelected(context);
                if (connect) {
                        jobs.setContext(context);
                        runButton.setEnabled(model != null);
                        refreshButton.setEnabled(true);
                }
                else {
                        runButton.setEnabled(false);
                        refreshButton.setEnabled(false);
                        jobs.setContext(null);
                }
                checkButtons();
        }
        
        public class ServiceSelectionListener implements ActionListener {
                public void actionPerformed(ActionEvent e) {
                        setContext((RESTContext) services.getSelectedItem(),false);
                }
        }
        
        public class NewServiceAction extends AbstractAction {
                public NewServiceAction() {
                        super("New", TavernaIcons.newIcon);
                }
                
                public void actionPerformed(ActionEvent e) {
                        AddEditServiceFrame addFrame =
                                new AddEditServiceFrame(ExecuteRemotelyPanel.this);
                        addFrame.setVisible(true);
                }
        }
        
        public class EditServiceAction extends AbstractAction {
                public EditServiceAction() {
                        super("Edit", TavernaIcons.editIcon);
                }
                
                public void actionPerformed(ActionEvent e) {
                        AddEditServiceFrame addFrame =
                                new AddEditServiceFrame(ExecuteRemotelyPanel.this,
                                (RESTContext) services.getSelectedItem());
                        addFrame.setVisible(true);
                }
        }
        
        public class RemoveServiceAction extends AbstractAction {
                public RemoveServiceAction() {
                        super("Remove", TavernaIcons.deleteIcon);
                }
                
                public void actionPerformed(ActionEvent e) {
                        RESTContext service = (RESTContext) services.getSelectedItem();
                        int response=JOptionPane.showConfirmDialog((Component)ExecuteRemotelyPanel.this,"Are you sure you wish to delete "+service.getName()+" ?","Delete remote host?",JOptionPane.YES_NO_OPTION);
                        if (response==JOptionPane.OK_OPTION) {
                                removeService(service);
                        }
                }
        }
        
        public class ConnectServiceAction extends AbstractAction {
                public ConnectServiceAction() {
                        putValue(SMALL_ICON,TavernaIcons.dataLinkIcon);
                        putValue(NAME,"Connect");
                        putValue(SHORT_DESCRIPTION,"Connect to the selected service location");
                }
           
                public void actionPerformed(ActionEvent actionEvent) {
                                if (services.getSelectedItem()!=null) {
                                        setContext((RESTContext) services.getSelectedItem(),true);
                                }
                }
        }
        
        public class RunWorkflowAction extends AbstractAction {
                
                public RunWorkflowAction() {
                        putValue(SMALL_ICON, TavernaIcons.runIcon);
                        putValue(NAME, "Run workflow remotely...");
                        putValue(SHORT_DESCRIPTION, "Run the current workflow remotely");
                }
                
         
				public void actionPerformed(ActionEvent ev) {
                        if (context == null || model == null) {
                                logger.info("Can't run workflow without connection or current workflow");
                                checkButtons();
                                return;
                        }
                        RemoteWorkflowInputPanel.run(model, service, uiLog, ExecuteRemotelyPanel.this);
                        jobs.refresh();
                }
        }
        
        
        public class RefreshAction extends AbstractAction {
                
                private static final long serialVersionUID = -4718304414344585132L;
                
                public RefreshAction() {
                        putValue(SMALL_ICON, TavernaIcons.refreshIcon);
                        putValue(NAME, "Refresh");
                        putValue(SHORT_DESCRIPTION, "Refresh list of jobs from server");
                }
                
                public void actionPerformed(ActionEvent e) {
                        jobs.refresh();
                }
        }
        
        public class LogPanel extends JPanel implements UILogger {
                
                public LogPanel() {
                        super();
                        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                }
                
                public void log(Exception ex) {
                        log(ex.toString());
                }
                
                public synchronized void log(String msg) {
                        DateFormat dateformat =
                                DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.UK);
                        String time = dateformat.format(new Date());
                        
                        JLabel logLabel =
                                new JLabel("<html><small> " + time + ": " + msg
                                + "</small></html");
                        add(logLabel, 0);
                        revalidate();
                }
                
        }
}
