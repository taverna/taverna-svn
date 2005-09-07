import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;

/**
 * Created by IntelliJ IDEA. User: Tony Burdett Date: 04-Sep-2005 Time: 11:31:41 To change this template use File |
 * Settings | File Templates.
 */
public class ConfiguratorUI
{
    private static final String DAZZLECFG = "dalec-webapp\\dazzlecfg.xml";
    private static final DalecConfig DALECCONFIG = new DalecConfig(new File(DAZZLECFG));

    private static final DalecListModel DLM = new DalecListModel(DALECCONFIG.getDalecs());
    private static final JList LIST = new JList(DLM);

    public static void main(String[] args)
    {
        JFrame dcf = new DalecConfigFrame();
        dcf.setVisible(true);
        dcf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

//        ConfiguratorFrame.setLayout(new BorderLayout());
//
//        ConfiguratorFrame.add(panel);
//        ConfiguratorFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        ConfiguratorFrame.setSize(400, 400);
//        ConfiguratorFrame.setVisible(true);
    }

    private static class DalecConfigFrame extends JFrame
    {
        public DalecConfigFrame()
        {
            // set some appearance stuff
            setTitle("Dalec Configurator");
            setSize(550, 250);

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
                        // pop up the remove dialog - are you sure? plus OK / Cancel
                        JDialog removeDialog = new RemoveDalecDialog(DalecConfigFrame.this, DLM.getDalecByName(dalecName));
                        removeDialog.setVisible(true);
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
                    // pop up a copy of the add Dalec dialog box but fill in the details already
                    try
                    {
                        String name = LIST.getSelectedValue().toString();
                        Dalec dalec = DLM.getDalecByName(name);

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
                            Dalec d = (Dalec) it.next();
                            DALECCONFIG.addDalec(d);
                        }
                        DALECCONFIG.writeXML();
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
            add(listPanel, BorderLayout.NORTH);
            add(buttonPanel, BorderLayout.SOUTH);
        }
    }

    private static class DalecListModel extends DefaultListModel
    {
        public DalecListModel(ArrayList dalecsList)
        {
            for (Iterator it = dalecsList.iterator(); it.hasNext();)
            {
                Dalec dalec = (Dalec) it.next();
                addElement(dalec);
            }
        }

        public Object getElementAt(int index)
        {
            // return the name so the JList displays dalec names
            return ((Dalec) get(index)).getAttributes().get(Dalec.NAME);
        }

        public Dalec getDalecByName(String dalecName)
        {
            Dalec d = null;
            for (int i = 0; i < size(); i++)
            {
                d = (Dalec) get(i);
                String name = (String) d.getAttributes().get(Dalec.NAME);
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
                dalecsList.add((Dalec) get(i));
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
            super(owner, "Add a new Dalec", true);
            setSize(600, 250);

            // set up all boxes for adding Dalec data
            inputPanel = new JPanel();
            inputPanel.setLayout(new GridLayout(5, 3, 3, 3));

            NAME.setColumns(50);
            addField("Name (should be unique): ", NAME);
            inputPanel.add(new JLabel(""));

            DESCRIPTION.setColumns(50);
            addField("Description: ", DESCRIPTION);
            inputPanel.add(new JLabel(""));

            MAP.setColumns(50);
            addField("MapMaster (corresponds to reference server to annotate): ", MAP);
            inputPanel.add(new JLabel(""));

            XSCUFL.setColumns(50);
            addFieldWithBrowseButton("Workflow File (.xscufl format): ", XSCUFL, true);

            DBFIELD.setColumns(50);
            addFieldWithBrowseButton("Database root location: ", DBFIELD, false);

            JButton okButton = new JButton("OK");
            okButton.addActionListener(new ActionListener()
            {

                public void actionPerformed(ActionEvent e)
                {
                    Dalec dalec = new Dalec();
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
            // create a new Dalec and add to LIST model
        }

        private AddDalecDialog(JFrame owner, final Dalec editingDalec)
        {
            super(owner, "Add a new Dalec", true);
            setSize(600, 250);

            // set up all boxes for adding Dalec data
            inputPanel = new JPanel();
            inputPanel.setLayout(new GridLayout(5, 3, 3, 3));

            NAME.setColumns(50);
            addField("Name (should be unique): ", NAME, (String) editingDalec.getAttributes().get(Dalec.NAME));
            inputPanel.add(new JLabel(""));

            DESCRIPTION.setColumns(50);
            addField("Description: ", DESCRIPTION, (String) editingDalec.getAttributes().get(Dalec.DESCRIPTION));
            inputPanel.add(new JLabel(""));

            MAP.setColumns(50);
            addField("MapMaster (corresponds to reference server to annotate): ", MAP, (String) editingDalec.getAttributes().get(Dalec.MAPMASTER));
            inputPanel.add(new JLabel(""));

            XSCUFL.setColumns(50);
            addFieldWithBrowseButton("Workflow File (.xscufl format): ", XSCUFL, true, (String) editingDalec.getAttributes().get(Dalec.XSCUFLFILE));

            DBFIELD.setColumns(50);
            addFieldWithBrowseButton("Database root location: ", DBFIELD, false, (String) editingDalec.getAttributes().get(Dalec.DBLOCATION));

            JButton okButton = new JButton("OK");
            okButton.addActionListener(new ActionListener()
            {

                public void actionPerformed(ActionEvent e)
                {
                    Dalec dalec = new Dalec();
                    dalec.setName(NAME.getText());
                    dalec.setDescription(DESCRIPTION.getText());
                    dalec.setMapMaster(MAP.getText());
                    dalec.setXScuflFile(XSCUFL.getText());
                    dalec.setDBLocation(DBFIELD.getText());

                    // now remove the old dalec and add the new one to the LIST model
                    DLM.removeElement(editingDalec);
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
        }

        public static void addField(String label, JTextField textField)
        {
            // add the label and the text box
            inputPanel.add(new JLabel(label));
            inputPanel.add(textField);
        }

        public static void addFieldWithBrowseButton(String label, final JTextField textField, final boolean workflowFile)
        {
            addField(label, textField);
            // add a button to open the file chooser dialog
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

                        // add Dalec to LIST model
                    }
                }
            });

            inputPanel.add(browseButton);
        }

        public static void addField(String label, JTextField textField, String defaultValue)
        {
            // add the label and the text box
            inputPanel.add(new JLabel(label));
            inputPanel.add(textField);
            textField.setText(defaultValue);
        }

        public static void addFieldWithBrowseButton(String label, final JTextField textField, final boolean workflowFile, String defaultValue)
        {
            addField(label, textField);
            // add a button to open the file chooser dialog
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

                        // add Dalec to LIST model
                    }
                }
            });

            inputPanel.add(browseButton);
            textField.setText(defaultValue);
        }
    }

    private static class RemoveDalecDialog extends JDialog
    {
        private RemoveDalecDialog(JFrame owner, final Dalec dalecToRemove)
        {
            super(owner, "Remove Dalecs", true);
            setSize(300, 150);

            add(new JLabel("Are you sure you want to remove " + (String) dalecToRemove.getAttributes().get(Dalec.NAME) + "?"), BorderLayout.CENTER);

            JPanel buttons = new JPanel();
            JButton ok = new JButton("OK");
            ok.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    DLM.removeElement(dalecToRemove);
                    setVisible(false);
                }
            });
            JButton cancel = new JButton("Cancel");
            cancel.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    setVisible(false);
                }
            });
            buttons.add(ok);
            buttons.add(cancel);
            add(buttons, BorderLayout.SOUTH);
        }
    }
}
