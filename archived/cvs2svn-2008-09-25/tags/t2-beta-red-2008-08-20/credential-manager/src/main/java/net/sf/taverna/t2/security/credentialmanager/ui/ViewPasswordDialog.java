package net.sf.taverna.t2.security.credentialmanager.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

/**
 * Dialog used for viewing password.
 * 
 * @author Alexandra Nenadic
 */
public class ViewPasswordDialog
    extends JDialog
{
	private static final long serialVersionUID = 935722761118204590L;

	/** Password field */
    private JTextField jtfPassword;
    
    /** Password value*/
    private String sPassword;

   /**
     * Creates new ViewPasswordDialog dialog where the parent is a frame.
     *
     * @param parent Parent frame
     * @param bModal Is dialog modal?
     * @param password Password value
     */
    public ViewPasswordDialog(JFrame parent, boolean bModal, String password)
    {
        this(parent, "View password", bModal, password);
    }

    /**
     * Creates new ViewPasswordDialog dialog where the parent is a frame.
     *
     * @param parent Parent frame
     * @param sTitle The dialog's title
     * @param bModal Is dialog modal?
     * @param password Password value
     */
    public ViewPasswordDialog(JFrame parent, String sTitle, boolean bModal, String password)
    {
        super(parent, sTitle, bModal);  
        sPassword = password;
        initComponents();
    }

    /**
     * Creates new ViewPasswordDialog dialog where the parent is a dialog.
     *
     * @param parent Parent dialog
     * @param bModal Is dialog modal?
     * @param password Password value
     */
    public ViewPasswordDialog(JDialog parent, boolean bModal, String password)
    {
        this(parent, "View password", bModal, password);
    }

    /**
     * Creates new ViewPasswordDialog dialog where the parent is a dialog.
     *
     * @param parent Parent dialog
     * @param sTitle The dialog's title
     * @param bModal Is dialog modal?
     * @param password Password value
     */
    public ViewPasswordDialog(JDialog parent, String sTitle, boolean bModal, String password)
    {
        super(parent, sTitle, bModal);
        sPassword = password;
        initComponents();
    }

    /**
     * Initialise the dialog's GUI components.
     */
    private void initComponents()
    {
        getContentPane().setLayout(new BorderLayout());

        JLabel jlPassword = new JLabel("Password value");

        //Populate the password field
        jtfPassword = new JTextField(15);
        jtfPassword.setText(sPassword);
        jtfPassword.setEditable(false);
        
        JButton jbOK = new JButton("OK");
        jbOK.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
            	closeDialog();
            }
        });
        
        JPanel jpPassword = new JPanel(new BorderLayout());
        jpPassword.add(jlPassword, BorderLayout.NORTH);
        jpPassword.add(jtfPassword, BorderLayout.CENTER);
        //jpPassword.setBorder(new EmptyBorder(5, 5, 5, 5));
        jpPassword.setBorder(new CompoundBorder(
                new EmptyBorder(5, 5, 5, 5), new EtchedBorder()));
        
        JPanel jpButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        jpButtons.add(jbOK);
        
        getContentPane().add(jpPassword, BorderLayout.CENTER);
        getContentPane().add(jpButtons, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent evt)
            {
                closeDialog();
            }
        });

        setResizable(false);

        getRootPane().setDefaultButton(jbOK);

        pack();
    }

    /**
     * Close the dialog.
     */
    private void closeDialog()
    {
        setVisible(false);
        dispose();
    }
}



