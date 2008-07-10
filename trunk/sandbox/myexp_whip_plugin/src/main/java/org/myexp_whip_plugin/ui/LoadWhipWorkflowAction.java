package org.myexp_whip_plugin.ui;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.WorkflowDescription;
import org.embl.ebi.escience.scufl.XScufl;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.embl.ebi.escience.scufl.view.XScuflView;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.actions.ScuflModelActionSPI;
import org.embl.ebi.escience.scuflui.shared.ModelMap;
import org.embl.ebi.escience.scuflui.shared.ScuflModelSet;
import org.embl.ebi.escience.scuflui.shared.WorkflowChanges;
import org.whipplugin.app.AppService;
import org.whipplugin.app.ArtefactReceiver;
import org.whipplugin.data.bundle.DataBundle;
import org.whipplugin.data.bundle.DataUtils;
import org.whipplugin.data.bundle.TempFileManager;
import org.whipplugin.data.description.Author;
import org.whipplugin.data.description.MetadataDocument;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;

@SuppressWarnings("serial")
public class LoadWhipWorkflowAction extends ScuflModelActionSPI implements ArtefactReceiver, PropertyChangeListener {

    private static Logger logger = Logger.getLogger(LoadWhipWorkflowAction.class);

    private PublishWorkflowAction publish = new PublishWorkflowAction();

    private AppService owner;

    private HashMap<String, DataBundle> bundles = new HashMap<String, DataBundle>();

    public static final String WORKFLOW_OPENED = "org.whipplugin.taverna.workflow-opened";
    public static final String WORKFLOW_FAILED = "org.whipplugin.taverna.workflow-failed";
    public static final String WORKFLOW_STATUS = "org.whipplugin.taverna.workflow-status";

    public LoadWhipWorkflowAction() {
        putValue(SMALL_ICON, TavernaIcons.updateIcon);
        putValue(NAME, "Whip Actions");
        putValue(SHORT_DESCRIPTION, "Handles Whip Actions");
        owner = new AppService(this);
        owner.update();
    }

    public void actionPerformed(ActionEvent e) {
        JPopupMenu menu = new JPopupMenu("Handle Whip Actions");
        menu.add(new JMenuItem(publish));
        if (bundles.size() > 0) {
            menu.addSeparator();
        }
        for (String s : bundles.keySet()) {
            LoadWorkflowAction load = new LoadWorkflowAction(s);
            load.addPropertyChangeListener(this);
            menu.add(new JMenuItem(load));
        }
        Component sourceComponent = (Component) e.getSource();
        menu.show(sourceComponent, 0, sourceComponent.getHeight());
    }

    public String getLabel() {
        return "Whip Actions";
    }

    public void openWorkflow(final Component sourceComponent, final String name) throws Exception {

        final ScuflModel model = updateModel();
        try {
            DataBundle db = bundles.get(name);
            if (db == null) {
                return;
            }
            InputStream in = db.getEntryPoint();
            if (in == null) {
                return;
            }
            XScuflParser.populate(in, model, null);
            ScuflModelSet.getInstance().addModel(model);
            WorkflowChanges.getInstance().synced(model);
        } catch (Exception ex) {
            logger.warn("Can't open in online mode " + name, ex);
            model.clear();
            JOptionPane.showMessageDialog(
                    sourceComponent,
                    "Problem opening workflow: \n\n"
                            + ex.getMessage(),
                    "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void artefactArrived(String s, DataBundle bundle) {
        if (bundle != null) {
            final String entry = bundle.getMetadatDocument().getEntryPoint();
            if (entry == null) {
                owner.dispose(((File) bundle.getContent()).getName());
                return;
            }
            bundles.put(entry, bundle);
            putValue(SMALL_ICON, TavernaIcons.updateRecommendedIcon);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        openWorkflow(null, entry);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    postProcessOpen(entry);
                }
            });
        }
    }

    public String getName() {
        return "taverna-1.7";
    }


    public void postProcessOpen(String name) {
        DataBundle db = bundles.remove(name);
        if (db != null) {
            //owner.setProcessed(((File) db.getContent()).getName());
            String fname = ((File) db.getContent()).getName();
            System.out.println("trying to delete a file with the name " + fname);
            owner.dispose(fname);
        }
        if (bundles.size() == 0) {
            putValue(SMALL_ICON, TavernaIcons.updateIcon);
        }
    }

    public void propertyChange(PropertyChangeEvent event) {
        logger.info("LoadWhipWorkflowAction.propertyChange bundles size=" + bundles.size());

        String key = event.getPropertyName();
        if (key.equals(WORKFLOW_STATUS)) {
            logger.info("LoadWhipWorkflowAction.propertyChange key is workflow status");
            String op = (String) event.getNewValue();
            if (op.equals(WORKFLOW_OPENED) || op.equals(WORKFLOW_FAILED)) {
                logger.info("LoadWhipWorkflowAction.propertyChange op=" + op);
                String name = (String) event.getOldValue();
                postProcessOpen(name);
            }
        }
    }

    public class LoadWorkflowAction extends AbstractAction {

        public LoadWorkflowAction(String workflow) {
            putValue(SMALL_ICON, TavernaIcons.openIcon);
            putValue(NAME, workflow);
            putValue(SHORT_DESCRIPTION, "Load a workflow");
        }

        /*
           * (non-Javadoc)
           *
           * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
           */
        public void actionPerformed(ActionEvent e) {
            final Component sourceComponent = (Component) e.getSource();
            if (bundles.size() == 0) {
                return;
            }
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        openWorkflow(sourceComponent, (String) getValue(NAME));
                        firePropertyChange(WORKFLOW_STATUS, getValue(NAME), WORKFLOW_OPENED);
                    } catch (Exception e1) {
                        firePropertyChange(WORKFLOW_STATUS, getValue(NAME), WORKFLOW_FAILED);
                    }
                }
            });
        }
    }

    public class PublishWorkflowAction extends AbstractAction {

        public PublishWorkflowAction() {
            putValue(SMALL_ICON, TavernaIcons.uninstallIcon);
            putValue(NAME, "Publish Workflow");
            putValue(SHORT_DESCRIPTION, "Publish a workflow");
        }

        /*
           * (non-Javadoc)
           *
           * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
           */
        public void actionPerformed(ActionEvent e) {
            final ActionEvent evt = e;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    File f = null;
                    ScuflModel currentModel = (ScuflModel) ModelMap.getInstance().getNamedModel(ModelMap.CURRENT_WORKFLOW);
                    if (currentModel == null) {
                        logger.warn("Can't save null model");
                        JOptionPane.showMessageDialog(
                                (Component) evt.getSource(),
                                "No workflow to publish",
                                "Information", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    try {
                        WorkflowDescription desc = currentModel.getDescription();
                        String title = DataUtils.createFileNameFromName(desc.getTitle());
                        f = TempFileManager.createTempFile(title, ".xml");

                        saveToFile(currentModel, f);
                        Frame frame = getFrame((Component) evt.getSource());
                        MetadataDocument doc = new MetadataDocument(desc.getLSID());
                        doc.setDescription(desc.getText());
                        doc.setName(desc.getTitle());
                        doc.addDataType(XScufl.XScuflNS.getURI());
                        Author a = new Author(desc.getAuthor());
                        doc.addAuthor(a);
                        owner.publish(frame, f, doc, false);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        JOptionPane.showMessageDialog(
                                (Component) evt.getSource(),
                                "Problem publishing workflow: \n\n"
                                        + e1.getMessage(),
                                "Warning", JOptionPane.WARNING_MESSAGE);
                    }
                }
            });
        }

    }

    private Frame getFrame(Component c) {
        Frame f = null;
        while (c != null) {
            if (c instanceof Frame) {
                f = (Frame) c;
                break;
            }
            c = c.getParent();
        }
        return f;
    }

    private void saveToFile(ScuflModel model, File file)
            throws FileNotFoundException, SecurityException {
        OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(file), Charset.forName("UTF-8"));
        PrintWriter out = new PrintWriter(writer);
        out.print(XScuflView.getXMLText(model));
        out.flush();
        out.close();
        logger.info("Saved " + model + " to " + file);
    }

    protected ScuflModel updateModel() {
        //model.clear();
        return new ScuflModel();
        //return model;
    }

}