package net.sf.taverna.dalec.configurator;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;


/**
 * Created by IntelliJ IDEA. User: Tony Burdett Date: 04-Sep-2005 Time: 11:31:41 To change this template use File |
 * <p/>
 * Settings | File Templates.
 */
public class ConfiguratorUI
{
    private static final File ROOTDIR = new File("dalec-webapp");
    private static final String DAZZLECFG = "dalec-webapp" + File.separator + "dazzlecfg.xml";
    private static final DalecConfig DALECCONFIG = new DalecConfig(new File(DAZZLECFG));

    private static final DalecListModel DLM = new DalecListModel(DALECCONFIG.getDalecs());
    private static final JList LIST = new JList(DLM);

    private static DalecArchiver da;

    public static void main(String[] args)
    {
        File dasFile;
        // check that TOMCAT_HOME exists
        if (System.getenv("TOMCAT_HOME") == null)
        {
            // TOMCAT_HOME not defined - need to generate an error message
            System.err.println("No TOMCAT_HOME variable defined - unable to check for existing das.war file");
            System.err.println("Also, the archived webapp will be saved as deployment\\das.war");

            dasFile = new File("deployment" + File.separator + "das.war");
            da = new DalecArchiver(dasFile, ROOTDIR);
        }
        else
        {
            // use the tomcat webapps dir to define the location to archive files to
            dasFile = new File(System.getenv("TOMCAT_HOME") + File.separator + "webapps" + File.separator + "das.war");
            da = new DalecArchiver(dasFile, ROOTDIR);

            // Pre-existing das.war archive - so retrieve and unpack to the dalec-webapp dir
            if (dasFile.exists())
            {
                // unpack the existing archive
                try
                {
                    da.inflateArchive();
                }
                catch (IOException e)
                {
                    System.err.println("das.war exists - but cannot be accessed");
                }
            }
        }

        // Now we can set up the UI
        JFrame dcf = new DalecConfigFrame();
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(dcf);
            dcf.setVisible(true);
            dcf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
        catch (Exception e)
        {
            Exception e1 = new Exception("An error occurred whilst trying to open the Dalec Configurator", e.getCause());
            e1.printStackTrace();
        }
    }

    private static class DalecConfigFrame extends JFrame
    {
        public DalecConfigFrame()
        {
            // set some appearance stuff
            setTitle("Dalec Configurator");
            setSize(550, 300);
            setLocationRelativeTo(null);

            // configure the LIST display properties
            LIST.setFixedCellWidth(400);
            LIST.addListSelectionListener(new ListSelectionListener()
            {
                public void valueChanged(ListSelectionEvent e)
                {
                    // grey out the edit box if 0 or more than one selected???
                }
            });
            JScrollPane scrollPane = new JScrollPane(LIST);
            JPanel listPanel = new JPanel();
            listPanel.setSize(400, 200);
            listPanel.add(scrollPane);

            // make the buttons
            JButton addButton = new JButton("Add");
            addButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    // need to pop up an AddDalecDialog
                    JDialog addDialog = new AddDalecDialog(DalecConfigFrame.this);
                    addDialog.setVisible(true);
                }
            });
            JButton removeButton = new JButton("Remove");
            removeButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    try
                    {
                        String dalecName = (String) LIST.getSelectedValue();
                        popupRemoveBox(DLM.getDalecByName(dalecName));
                    }
                    catch (NullPointerException e1)
                    {
                        // if there's nothing selected to remove
                    }
                }
            });
            JButton editButton = new JButton("Edit");
            editButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    // pop up a copy of the add net.sf.taverna.dalec.configurator.DalecModel dialog box but fill in the details already
                    try
                    {
                        String name = LIST.getSelectedValue().toString();
                        DalecModel dalec = DLM.getDalecByName(name);

                        JDialog editDialog = new AddDalecDialog(DalecConfigFrame.this, dalec);
                        editDialog.setVisible(true);
                    }
                    catch (NullPointerException e1)
                    {
                        // if nothing is selected
                    }
                }
            });
            JButton doneButton = new JButton("Done");
            doneButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    try
                    {
                        DALECCONFIG.clearDalecs();
                        for (Iterator it = DLM.getDalecs().iterator(); it.hasNext();)
                        {
                            DalecModel d = (DalecModel) it.next();
                            DALECCONFIG.addDalec(d);
                        }
                        DALECCONFIG.writeXML();

                        // now XML is written, archive and deploy webapp
                        archiveAndDeploy();
                    }
                    catch (NullPointerException e1)
                    {
                        // if there's no dalecs left!
                    }
                    finally
                    {
                        DalecConfigFrame.this.setVisible(false);
                        System.exit(0);
                    }
                }
            });
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(addButton);
            buttonPanel.add(removeButton);
            buttonPanel.add(editButton);
            buttonPanel.add(doneButton);
            buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
            // add the LIST and buttons to the frame
            add(new JLabel("<html><br><p><font size=4><i>&nbsp;&nbsp;Configure Dalecs exposed by this DAS server:<i></font></p><br></html>"), BorderLayout.NORTH);
            add(listPanel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);
        }

        private static void popupRemoveBox(final DalecModel dalecToRemove)
        {
            String message = "Are you sure you want to remove the Dalec \"" + (String) dalecToRemove.getAttributes().get(DalecModel.NAME) + "\" ?";
            int response = JOptionPane.showConfirmDialog(null, message, "Remove Dalec", JOptionPane.OK_CANCEL_OPTION);
            if (response == JOptionPane.OK_OPTION)
            {
                DLM.removeElement(dalecToRemove);
            }
        }
    }

    private static class DalecListModel extends DefaultListModel
    {
        public DalecListModel(ArrayList dalecsList)
        {
            for (Iterator it = dalecsList.iterator(); it.hasNext();)
            {
                DalecModel dalec = (DalecModel) it.next();

                addElement(dalec);
            }
        }

        public Object getElementAt(int index)
        {
            // return the name so the JList displays dalec names
            return ((DalecModel) get(index)).getAttributes().get(DalecModel.NAME);
        }

        public DalecModel getDalecByName(String dalecName)
        {
            DalecModel d = null;
            for (int i = 0; i < size(); i++)
            {
                d = (DalecModel) get(i);
                String name = (String) d.getAttributes().get(DalecModel.NAME);
                if (name.matches(dalecName))
                {
                    break;
                }
            }
            return d;
        }

        public ArrayList getDalecs()
        {
            ArrayList dalecsList = new ArrayList();
            for (int i = 0; i < size(); i++)
            {
                dalecsList.add((DalecModel) get(i));
            }
            return dalecsList;
        }
    }

    private static class AddDalecDialog extends JDialog
    {
        private static JPanel inputPanel;

        private static final JTextField NAME = new JTextField();
        private static final JTextField DESCRIPTION = new JTextField();
        private static final JTextField MAP = new JTextField();
        private static final JTextField XSCUFL = new JTextField();
        private static final JTextField DBFIELD = new JTextField();

        private AddDalecDialog(JFrame owner)
        {
            super(owner, "Add a new net.sf.taverna.dalec.configurator.DalecModel", true);
            setSize(700, 250);
            setLocationRelativeTo(null);
            // set up all boxes for adding net.sf.taverna.dalec.configurator.DalecModel data
            inputPanel = new JPanel();
            inputPanel.setLayout(new GridBagLayout());

            GridBagConstraints labelCons = new GridBagConstraints();
            labelCons.weightx = 0;
            labelCons.weighty = 100;
            labelCons.gridx = 0;
            labelCons.gridy = 0;
            labelCons.gridwidth = 1;
            labelCons.gridheight = 1;
            labelCons.anchor = GridBagConstraints.EAST;
            labelCons.insets = new Insets(3, 10, 3, 3);

            GridBagConstraints boxCons = new GridBagConstraints();
            boxCons.weightx = 100;
            boxCons.weighty = 100;
            boxCons.gridx = 1;
            boxCons.gridy = 0;
            boxCons.gridwidth = 2;
            boxCons.gridheight = 1;
            boxCons.fill = GridBagConstraints.HORIZONTAL;
            boxCons.insets = new Insets(3, 3, 3, 3);

            GridBagConstraints boxAndButtonCons = new GridBagConstraints();
            boxAndButtonCons.weightx = 100;
            boxAndButtonCons.weighty = 100;
            boxAndButtonCons.gridx = 1;
            boxAndButtonCons.gridy = 3;
            boxAndButtonCons.gridwidth = 1;
            boxAndButtonCons.gridheight = 1;
            boxAndButtonCons.fill = GridBagConstraints.HORIZONTAL;
            boxAndButtonCons.insets = new Insets(3, 3, 3, 3);

            GridBagConstraints buttonCons = new GridBagConstraints();
            buttonCons.weightx = 0;
            buttonCons.weighty = 0;
            buttonCons.gridx = 2;
            buttonCons.gridy = 3;
            buttonCons.gridwidth = 2;
            buttonCons.gridheight = 1;
            buttonCons.insets = new Insets(3, 3, 3, 10);

            inputPanel.add(new JLabel("Name (should be unique): "), labelCons);
            inputPanel.add(NAME, boxCons);
            labelCons.gridy = 1;
            boxCons.gridy = 1;

            inputPanel.add(new JLabel("Descriptions: "), labelCons);
            inputPanel.add(DESCRIPTION, boxCons);
            labelCons.gridy = 2;
            boxCons.gridy = 2;

            inputPanel.add(new JLabel("MapMaster (corresponds to reference server to annotate): "), labelCons);
            inputPanel.add(MAP, boxCons);
            labelCons.gridy = 3;

            inputPanel.add(new JLabel("Workflow File (.xscufl format): "), labelCons);
            inputPanel.add(XSCUFL, boxAndButtonCons);
            inputPanel.add(makeBrowseButton(XSCUFL, true), buttonCons);
            labelCons.gridy = 4;
            boxAndButtonCons.gridy = 4;
            buttonCons.gridy = 4;

            inputPanel.add(new JLabel("Database root location: "), labelCons);
            inputPanel.add(DBFIELD, boxAndButtonCons);
            inputPanel.add(makeBrowseButton(DBFIELD, false), buttonCons);


            JButton okButton = new JButton("OK");
            okButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    DalecModel dalec = new DalecModel();
                    dalec.setName(NAME.getText());
                    dalec.setDescription(DESCRIPTION.getText());
                    dalec.setMapMaster(MAP.getText());
                    dalec.setXScuflFile(XSCUFL.getText());
                    dalec.setDBLocation(DBFIELD.getText());

                    DLM.addElement(dalec);
                    setVisible(false);
                }
            });

            JButton canxButton = new JButton("Cancel");
            canxButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    // just close the window
                    setVisible(false);
                }
            });

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(okButton);
            buttonPanel.add(canxButton);

            add(inputPanel);
            add(buttonPanel, BorderLayout.SOUTH);
            // create a new net.sf.taverna.dalec.configurator.DalecModel and add to LIST model
        }

        private AddDalecDialog(JFrame owner, final DalecModel editingDalec)
        {
            super(owner, "Add a new net.sf.taverna.dalec.configurator.DalecModel", true);
            setSize(700, 250);
            setLocationRelativeTo(null);
            // set up all boxes for adding net.sf.taverna.dalec.configurator.DalecModel data
            inputPanel = new JPanel();
            inputPanel.setLayout(new GridBagLayout());

            GridBagConstraints labelCons = new GridBagConstraints();
            labelCons.weightx = 0;
            labelCons.weighty = 100;
            labelCons.gridx = 0;
            labelCons.gridy = 0;
            labelCons.gridwidth = 1;
            labelCons.gridheight = 1;
            labelCons.anchor = GridBagConstraints.EAST;
            labelCons.insets = new Insets(3, 10, 3, 3);

            GridBagConstraints boxCons = new GridBagConstraints();
            boxCons.weightx = 100;
            boxCons.weighty = 100;
            boxCons.gridx = 1;
            boxCons.gridy = 0;
            boxCons.gridwidth = 2;
            boxCons.gridheight = 1;
            boxCons.fill = GridBagConstraints.HORIZONTAL;
            boxCons.insets = new Insets(3, 3, 3, 3);

            GridBagConstraints boxAndButtonCons = new GridBagConstraints();
            boxAndButtonCons.weightx = 100;
            boxAndButtonCons.weighty = 100;
            boxAndButtonCons.gridx = 1;
            boxAndButtonCons.gridy = 3;
            boxAndButtonCons.gridwidth = 1;
            boxAndButtonCons.gridheight = 1;
            boxAndButtonCons.fill = GridBagConstraints.HORIZONTAL;
            boxAndButtonCons.insets = new Insets(3, 3, 3, 3);

            GridBagConstraints buttonCons = new GridBagConstraints();
            buttonCons.weightx = 0;
            buttonCons.weighty = 0;
            buttonCons.gridx = 2;
            buttonCons.gridy = 3;
            buttonCons.gridwidth = 2;
            buttonCons.gridheight = 1;
            buttonCons.insets = new Insets(3, 3, 3, 10);

            inputPanel.add(new JLabel("Name (should be unique): "), labelCons);
            inputPanel.add(NAME, boxCons);
            NAME.setText((String) editingDalec.getAttributes().get(DalecModel.NAME));
            labelCons.gridy = 1;
            boxCons.gridy = 1;

            inputPanel.add(new JLabel("Descriptions: "), labelCons);
            inputPanel.add(DESCRIPTION, boxCons);
            DESCRIPTION.setText((String) editingDalec.getAttributes().get(DalecModel.DESCRIPTION));
            labelCons.gridy = 2;
            boxCons.gridy = 2;

            inputPanel.add(new JLabel("MapMaster (corresponds to reference server to annotate): "), labelCons);
            inputPanel.add(MAP, boxCons);
            MAP.setText((String) editingDalec.getAttributes().get(DalecModel.MAPMASTER));
            labelCons.gridy = 3;

            inputPanel.add(new JLabel("Workflow File (.xscufl format): "), labelCons);
            inputPanel.add(XSCUFL, boxAndButtonCons);
            inputPanel.add(makeBrowseButton(XSCUFL, true), buttonCons);
            XSCUFL.setText((String) editingDalec.getAttributes().get(DalecModel.XSCUFLFILE));
            labelCons.gridy = 4;
            boxAndButtonCons.gridy = 4;
            buttonCons.gridy = 4;

            inputPanel.add(new JLabel("Database root location: "), labelCons);
            inputPanel.add(DBFIELD, boxAndButtonCons);
            DBFIELD.setText((String) editingDalec.getAttributes().get(DalecModel.DBLOCATION));
            inputPanel.add(makeBrowseButton(DBFIELD, false), buttonCons);

            JButton okButton = new JButton("OK");
            okButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    DalecModel dalec = new DalecModel();
                    dalec.setName(NAME.getText());
                    dalec.setDescription(DESCRIPTION.getText());
                    dalec.setMapMaster(MAP.getText());
                    dalec.setXScuflFile(XSCUFL.getText());
                    dalec.setDBLocation(DBFIELD.getText());
                    // now remove the old dalec and add the new one to the LIST model
                    DLM.removeElement(editingDalec);
                    DLM.addElement(dalec);
                    // now remove all set values
                    NAME.setText(null);
                    DESCRIPTION.setText(null);
                    MAP.setText(null);
                    XSCUFL.setText(null);
                    DBFIELD.setText(null);
                    // close the window
                    setVisible(false);
                }
            });

            JButton canxButton = new JButton("Cancel");
            canxButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    // remove all set values
                    NAME.setText(null);
                    DESCRIPTION.setText(null);
                    MAP.setText(null);
                    XSCUFL.setText(null);
                    DBFIELD.setText(null);
                    // close the window
                    setVisible(false);
                }
            });

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(okButton);
            buttonPanel.add(canxButton);

            add(inputPanel);
            add(buttonPanel, BorderLayout.SOUTH);
        }

        public static JButton makeBrowseButton(final JTextField textField, final boolean workflowFile)
        {
            JButton browseButton = new JButton("Browse");
            browseButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    JFileChooser fileBrowser = new JFileChooser(new File("."));

                    if (workflowFile)
                    {
                        fileBrowser.setFileFilter(new FileFilter()
                        {
                            public boolean accept(File f)
                            {
                                return (f.getName().toLowerCase().endsWith(".xml") || f.isDirectory());
                            }

                            public String getDescription()
                            {
                                return "Workflow Files (*.xml)";
                            }
                        });
                    }
                    else
                    {
                        fileBrowser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    }

                    int result = fileBrowser.showOpenDialog(inputPanel);

                    if (result == JFileChooser.APPROVE_OPTION)
                    {
                        textField.setText(fileBrowser.getSelectedFile().getPath());
                        // add net.sf.taverna.dalec.configurator.DalecModel to LIST model
                    }
                }
            });
            return browseButton;
        }
    }

    /**
     * Once "done" button is clicked, the new version of dazzlecfg.xml is written into the dalec-webapp folder.  This
     * folder then needs to be compiled into das.war and deployed in the TOMCAT_HOME directory.
     */
    private static void archiveAndDeploy()
    {
        // pack all files in the baseDir (dalec-webapp) to the archive
        try
        {
            da.createArchive();
        }
        catch (IOException e)
        {
            System.err.println("Unable to create archive");
        }
    }
}