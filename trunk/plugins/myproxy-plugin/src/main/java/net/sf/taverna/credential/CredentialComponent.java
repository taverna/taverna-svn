package net.sf.taverna.credential;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.globus.myproxy.MyProxyException;
import org.globus.tools.ui.util.FileBrowser;
import org.ietf.jgss.GSSException;

/**
 * GUI class for Credential-Creator plugin. Invokes methods of class
 * {@link CredentialCreator}
 * 
 * @author Bharathi Kattamuri
 * 
 */
public class CredentialComponent extends JPanel implements
		UIComponentSPI, ActionListener {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger
			.getLogger(CredentialComponent.class);
	GlobusCredential gc = null;
	String format = null;
	CredentialCreator credentialCreator = null;
	JRadioButton pk12 = null;
	JRadioButton pem = null;
	ButtonGroup formatGroup;
	JLabel usercertLabel = null;
	JTextField usercertText = null;
	JButton certButton = null;
	JLabel userkeyLabel = null;
	JTextField userkeyText = null;
	JButton keyButton = null;

	JLabel pk12Label = null;
	JTextField pk12Text = null;
	JButton pk12Button = null;

	FileBrowser keyFileBrowser = null;
	JPasswordField gridpassText = null;
	JTextField gridcredentialTimeText = null;
	JButton gridproxyInit = null;
	JButton gridproxyClear = null;
	JButton deleteProxy = null;

	JTextField myproxyUserText = null;
	JPasswordField myproxyPassText = null;
	JTextField myproxyServerText = null;
	JTextField myproxyCredentialDaysText = null;
	JTextField myproxyCredentialHoursText = null;
	JButton myproxyClear = null;
	JButton myproxyDelete = null;
	JButton myproxyInit = null;
	JPanel mainPanel = null;
	JPanel gridPanel = null;
	JPanel myproxyPanel = null;

	public CredentialComponent() {
		super(new GridLayout(3, 3));

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setPreferredSize(new Dimension(500, 400));
		tabbedPane.setMinimumSize(new Dimension(500, 400));
		tabbedPane.setMaximumSize(new Dimension(500, 400));

		gridPanel = new JPanel(new GridLayout(6, 1));
		credentialCreator = new CredentialCreator();

		JPanel optionPanel = new JPanel(new GridLayout(1, 4));

		JLabel lab1 = new JLabel("");
		optionPanel.add(lab1);
		pk12 = new JRadioButton("PK12");
		pk12.addActionListener(this);
		pem = new JRadioButton("PEM");
		pem.addActionListener(this);
		formatGroup = new ButtonGroup();
		formatGroup.add(pk12);
		formatGroup.add(pem);

		optionPanel.add(pk12);
		optionPanel.add(pem);
		JLabel lab2 = new JLabel("");
		optionPanel.add(lab2);
		gridPanel.add(optionPanel);

		usercertLabel = new JLabel("Usercert file:");
		usercertText = new JTextField(10);
		certButton = new JButton("...");
		certButton.addActionListener(this);
		this.addGridPanel(usercertLabel, usercertText, certButton);
		userkeyLabel = new JLabel("Userkey file:");
		userkeyText = new JTextField(10);
		keyButton = new JButton("...");
		keyButton.addActionListener(this);
		this.addGridPanel(userkeyLabel, userkeyText, keyButton);

		pk12Label = new JLabel("PK12 file:   ");
		pk12Text = new JTextField(10);
		pk12Button = new JButton("...");
		pk12Button.addActionListener(this);
		this.addGridPanel(pk12Label, pk12Text, pk12Button);

		tabbedPane.addTab("GridProxy", this.getGridPanel());
		tabbedPane.addTab("MyProxy", this.getMyProxyPanel());
		tabbedPane.addTab("Help", this.getHelpPanel());

		add(tabbedPane);

		add(new JLabel(" "));
		add(new JLabel(" "));
		add(new JLabel(" "));
		add(new JLabel(" "));

		add(new JLabel(" "));
		add(new JLabel(" "));
		add(new JLabel(" "));
		add(new JLabel(" "));

	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public void onDisplay() {
		// TODO Auto-generated method stub
	}

	public void onDispose() {
		// TODO Auto-generated method stub
	}

	public void actionPerformed(ActionEvent e) {

		logger.debug("source:" + e.getActionCommand());
		if (e.getSource() == pk12) {
			format = "pk12";
			usercertLabel.setVisible(false);
			usercertText.setVisible(false);
			certButton.setVisible(false);
			userkeyLabel.setVisible(false);
			userkeyText.setVisible(false);
			keyButton.setVisible(false);
			pk12Label.setVisible(true);
			pk12Text.setVisible(true);
			pk12Button.setVisible(true);
		}
		if (e.getSource() == pem) {
			format = "pem";
			usercertLabel.setVisible(true);
			usercertText.setVisible(true);
			certButton.setVisible(true);
			userkeyLabel.setVisible(true);
			userkeyText.setVisible(true);
			keyButton.setVisible(true);
			pk12Label.setVisible(false);
			pk12Text.setVisible(false);
			pk12Button.setVisible(false);
		}
		if (e.getSource() == certButton) {
			JFileChooser filechooser = null;
			String globusdir = System.getProperty("user.home") + "/.globus";
			boolean fileExist = (new File(globusdir)).exists();
			if (fileExist == true)
				filechooser = new JFileChooser(globusdir);
			else
				filechooser = new JFileChooser();
			if (filechooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				usercertText.setText(filechooser.getSelectedFile().getPath());
			}
		}
		if (e.getSource() == keyButton) {
			JFileChooser filechooser = null;
			String globusdir = System.getProperty("user.home") + "/.globus";
			boolean fileExist = (new File(globusdir)).exists();
			if (fileExist == true)
				filechooser = new JFileChooser(globusdir);
			else
				filechooser = new JFileChooser();
			if (filechooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				userkeyText.setText(filechooser.getSelectedFile().getPath());
			}
		}
		if (e.getSource() == pk12Button) {
			JFileChooser filechooser = null;
			String globusdir = System.getProperty("user.home") + "/.globus";
			boolean fileExist = (new File(globusdir)).exists();
			if (fileExist == true)
				filechooser = new JFileChooser(globusdir);
			else
				filechooser = new JFileChooser();
			if (filechooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				pk12Text.setText(filechooser.getSelectedFile().getPath());
			}
		}
		if (e.getSource() == gridproxyClear) {
			usercertText.setText("");
			userkeyText.setText("");
			pk12Text.setText("");
			gridpassText.setText("");
		}

		if (e.getSource() == deleteProxy) {

			boolean deletefile = credentialCreator.deleteCredential();
			if (deletefile == true)
				JOptionPane.showMessageDialog(this,
						"Credential Deleted Successfully");
			else
				JOptionPane.showMessageDialog(this,
						"Credential Not Existed to Delete");
		}

		if (e.getSource() == gridproxyInit) {
			if (format == null) {
				JOptionPane.showMessageDialog(this,
						"FailFormat not selected, select PEM or PK12 !");
			}

			if (format.equals("pem")) {

				if (usercertText.getText().equals("")
						|| userkeyText.getText().equals("")) {
					JOptionPane.showMessageDialog(this,
							"usercert, userkey files not selected !");
				} else {
					try {
						gc = credentialCreator.pemGridProxyInit(new String(
								gridpassText.getPassword()), usercertText
								.getText(), userkeyText.getText(),
								gridcredentialTimeText.getText());
						if (gc != null)
							JOptionPane.showMessageDialog(this,
									"Credential created Successfully");
					} catch (Exception e1) {
						e1.printStackTrace();
						JOptionPane.showMessageDialog(this,
								"Failed to create credential !\n\""
										+ e1.getMessage() + "\"", "Error",
								JOptionPane.ERROR_MESSAGE);
						e1.printStackTrace();
					}
				}

			}
			if (format.equals("pk12")) {
				if (pk12Text.getText().equals("")) {
					JOptionPane.showMessageDialog(this,
							"PK12 file not selected !");
				} else {
					try {
						gc = credentialCreator.pk12GridProxyInit(new String(
								gridpassText.getPassword()),
								pk12Text.getText(), gridcredentialTimeText
										.getText());
						if (gc != null)
							JOptionPane.showMessageDialog(this,
									"Credential created Successfully");
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(this,
								"Failed to create credential !\n\""
										+ e1.getMessage() + "\"", "Error",
								JOptionPane.ERROR_MESSAGE);
						e1.printStackTrace();
					}
				}
			}
		}

		if (e.getSource() == myproxyInit) {
			boolean success = false;
			try {
				gc = credentialCreator.getGlobusCredentialFromFile();
			} catch (GlobusCredentialException e3) {
				// TODO Auto-generated catch block
				e3.printStackTrace();
			}
			if (gc == null) {
				JOptionPane.showMessageDialog(this,
						"Credential not existed, do GridProxyInit ");
			} else if (myproxyUserText.getText().equals("")
					|| myproxyPassText.getPassword().length == 0
					|| myproxyServerText.getText().equals("")
					|| (myproxyCredentialDaysText.getText().equals("") && myproxyCredentialHoursText
							.getText().equals(""))) {
				JOptionPane
						.showMessageDialog(this, "Insufficient MyProxy Data");
			} else {

				try {
					success = credentialCreator.createMyproxyCredential(gc,
							myproxyUserText.getText(), new String(
									myproxyPassText.getPassword()),
							myproxyCredentialDaysText.getText(),
							myproxyCredentialHoursText.getText(),
							myproxyServerText.getText());
				} catch (GSSException e1) {
					JOptionPane.showMessageDialog(this,
							"Failed to create  Myproxy credential !\n\""
									+ e1.getMessage() + "\"", "Error",
							JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				} catch (MyProxyException e2) {
					JOptionPane.showMessageDialog(this,
							"Failed to create  Myproxy credential !\n\""
									+ e2.getMessage() + "\"", "Error",
							JOptionPane.ERROR_MESSAGE);
					e2.printStackTrace();
				}
				if (success == true) {
					JOptionPane.showMessageDialog(this,
							"Myproxy Credential stored successfully");
				} else {
					JOptionPane.showMessageDialog(this,
							"Failed to store Credential in MyproxyServer");
				}
			}

		}

		if (e.getSource() == myproxyClear) {
			myproxyUserText.setText("");
			myproxyPassText.setText("");
			myproxyServerText.setText("");
			myproxyCredentialDaysText.setText("");
			myproxyCredentialHoursText.setText("");
		}

		if (e.getSource() == myproxyDelete) {

			if (gc == null) {
				JOptionPane.showMessageDialog(this,
						"Credential not existed, do GridProxyInit ");
			} else if (myproxyUserText.getText().equals("")
					|| myproxyPassText.getPassword().length == 0
					|| myproxyServerText.getText().equals("")
					|| (myproxyCredentialDaysText.getText().equals(""))) {
				JOptionPane
						.showMessageDialog(this, "Insufficient MyProxy Data");

			} else {
				try {
					credentialCreator.deleteMyProxyCredential(gc,
							myproxyUserText.getText(), new String(
									myproxyPassText.getPassword()),
							myproxyServerText.getText());
				} catch (MyProxyException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (GSSException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		}

	}

	private void setGbc(GridBagConstraints gbc, int row, int column, double wx,
			double wy, int width, int height, int fill) {
		gbc.gridy = row;
		gbc.gridx = column;
		gbc.weightx = wx;
		gbc.weighty = wy;
		gbc.gridwidth = width;
		gbc.gridheight = height;
		gbc.fill = fill;

	}

	private JPanel getGridPanel() {
		gridPanel = new JPanel(new GridLayout(6, 1));
		credentialCreator = new CredentialCreator();

		JPanel optionPanel = new JPanel(new GridLayout(1, 4));
		GridBagLayout gb = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();

		JLabel lab1 = new JLabel("");

		optionPanel.add(lab1);
		pk12 = new JRadioButton("PK12");
		pk12.addActionListener(this);
		pem = new JRadioButton("PEM");
		pem.addActionListener(this);
		formatGroup = new ButtonGroup();
		formatGroup.add(pk12);
		formatGroup.add(pem);

		optionPanel.add(pk12);
		optionPanel.add(pem);
		JLabel lab2 = new JLabel("");
		optionPanel.add(lab2);
		gridPanel.add(optionPanel);

		usercertLabel = new JLabel("Usercert file:");
		usercertText = new JTextField(10);
		certButton = new JButton("...");
		certButton.addActionListener(this);
		this.addGridPanel(usercertLabel, usercertText, certButton);
		userkeyLabel = new JLabel("Userkey file:");
		userkeyText = new JTextField(10);
		keyButton = new JButton("...");
		keyButton.addActionListener(this);
		this.addGridPanel(userkeyLabel, userkeyText, keyButton);

		pk12Label = new JLabel("PK12 file:   ");
		pk12Text = new JTextField(10);
		pk12Button = new JButton("...");
		pk12Button.addActionListener(this);
		this.addGridPanel(pk12Label, pk12Text, pk12Button);

		JPanel gridProxyPanel = new JPanel();
		gb = new GridBagLayout();
		gridProxyPanel.setLayout(gb);
		gbc = new GridBagConstraints();
		this.setGbc(gbc, 0, 0, 0.5, 0.5, 1, 1, GridBagConstraints.HORIZONTAL);
		JLabel gridpassLabel = new JLabel("Gridpassphrase:");
		gb.setConstraints(gridpassLabel, gbc);
		gridProxyPanel.add(gridpassLabel);
		this.setGbc(gbc, 0, 1, 0.5, 0.5, 1, 1, GridBagConstraints.HORIZONTAL);
		gbc.insets = new Insets(2, 10, 2, 10);
		gridpassText = new JPasswordField(10);
		gb.setConstraints(gridpassText, gbc);
		gridProxyPanel.add(gridpassText); // ............2
		this.setGbc(gbc, 0, 2, 0.5, 0.5, 1, 1, GridBagConstraints.HORIZONTAL);
		JLabel credentialTimeLabel = new JLabel("Credential Time (hrs):");
		gbc.anchor = GridBagConstraints.CENTER;
		gb.setConstraints(credentialTimeLabel, gbc);
		gridProxyPanel.add(credentialTimeLabel);
		this.setGbc(gbc, 0, 3, 0.5, 0.5, 1, 1, GridBagConstraints.REMAINDER);
		gridcredentialTimeText = new JTextField("12");
		gbc.anchor = GridBagConstraints.WEST;
		gb.setConstraints(gridcredentialTimeText, gbc);
		gridProxyPanel.add(gridcredentialTimeText);
		gridPanel.add(gridProxyPanel);

		JPanel gridButtonPanel = new JPanel(new GridLayout(1, 3));
		gridproxyClear = new JButton("Clear");
		gridproxyClear.addActionListener(this);
		deleteProxy = new JButton("DeleteProxy");
		deleteProxy.addActionListener(this);
		gridproxyInit = new JButton("GridProxyInit");
		gridproxyInit.addActionListener(this);
		gridButtonPanel.add(gridproxyClear);
		gridButtonPanel.add(deleteProxy);
		gridButtonPanel.add(gridproxyInit);
		gridPanel.add(gridButtonPanel);

		return gridPanel;
	}

	private JPanel getMyProxyPanel() {

		myproxyPanel = new JPanel(new GridLayout(6, 1));

		JLabel myproxyUserLabel = new JLabel("MyProxy Username:");
		myproxyUserText = new JTextField(10);
		myproxyUserText.setSize(10, 10);
		this.addMyproxyPanel(myproxyUserLabel, myproxyUserText);

		JLabel myproxyPassphraseLabel = new JLabel("MyProxy Passphrase:");
		myproxyPassText = new JPasswordField(10);
		this.addMyproxyPanel(myproxyPassphraseLabel, myproxyPassText);

		JLabel myproxyServer = new JLabel("MyProxy Server:");
		myproxyServerText = new JTextField("myproxy.grid-support.ac.uk");
		myproxyServerText.setSize(10, 10);
		this.addMyproxyPanel(myproxyServer, myproxyServerText);

		JLabel myproxyCredentialDaysLabel = new JLabel(
				"MyProxy Credential  (days):");
		myproxyCredentialDaysText = new JTextField(5);
		myproxyCredentialDaysText.setText("7");
		this.addMyproxyPanel(myproxyCredentialDaysLabel,
				myproxyCredentialDaysText);

		JLabel myproxyCredentialHoursLabel = new JLabel(
				"MyProxy Credential  (hrs):");
		myproxyCredentialHoursText = new JTextField(5);
		myproxyCredentialHoursText.setText("0");
		this.addMyproxyPanel(myproxyCredentialHoursLabel,
				myproxyCredentialHoursText);

		JPanel myproxyButtonPanel = new JPanel(new GridLayout(1, 3));
		myproxyClear = new JButton("Clear");
		myproxyClear.addActionListener(this);
		myproxyDelete = new JButton("MyProxy Delete");
		myproxyDelete.addActionListener(this);
		myproxyInit = new JButton("MyProxy Init");
		myproxyInit.addActionListener(this);

		myproxyButtonPanel.add(myproxyClear);
		myproxyButtonPanel.add(myproxyDelete);
		myproxyButtonPanel.add(myproxyInit);

		myproxyPanel.add(myproxyButtonPanel);
		gridPanel.setBorder(BorderFactory.createTitledBorder("GridProxy"));
		myproxyPanel.setBorder(BorderFactory.createTitledBorder("MyProxy"));
		return myproxyPanel;

	}

	private void addGridPanel(JLabel label, JTextField textField, JButton button) {
		JPanel panel = new JPanel();
		GridBagLayout gb = new GridBagLayout();
		panel.setLayout(gb);
		GridBagConstraints gbc = new GridBagConstraints();

		this.setGbc(gbc, 0, 1, 0.5, 0.5, 1, 1, GridBagConstraints.HORIZONTAL);
		gbc.insets = new Insets(2, 10, 2, 10);
		gb.setConstraints(textField, gbc);
		panel.add(textField); // ............2

		this.setGbc(gbc, 0, 2, 0.5, 0.5, 1, 1, GridBagConstraints.REMAINDER);
		gb.setConstraints(button, gbc);

		panel.add(button);
		panel.setBorder(BorderFactory.createTitledBorder(label.getText()));

		gridPanel.add(panel);

	}

	private void addMyproxyPanel(JLabel label, JTextField textField) {

		JPanel panel = new JPanel();
		GridBagLayout gb = new GridBagLayout();
		panel.setLayout(gb);
		GridBagConstraints gbc = new GridBagConstraints();

		this.setGbc(gbc, 0, 0, 0.5, 0.5, 1, 1, GridBagConstraints.HORIZONTAL);
		gbc.anchor = GridBagConstraints.CENTER;
		gb.setConstraints(label, gbc);
		panel.add(label);

		this.setGbc(gbc, 0, 1, 0.5, 0.5, 1, 1, GridBagConstraints.WEST);
		gbc.insets = new Insets(2, 10, 2, 10);
		gb.setConstraints(textField, gbc);
		panel.add(textField);

		this.setGbc(gbc, 0, 1, 0.5, 0.5, 1, 1, GridBagConstraints.REMAINDER);
		JLabel fillLabel = new JLabel("     ");
		gb.setConstraints(fillLabel, gbc);
		panel.add(fillLabel);

		myproxyPanel.add(panel);

	}

	private JPanel getHelpPanel() {

		JPanel helpPanel = new JPanel(new GridLayout(1, 1));

		String text = "1. GridProxyInit creates a X509 proxy certificate file in 'HOME/.taverna/' directory  \n which is valid for specified lifetime. \n \n"
				+ "2. MyProxyInit sends proxy to specified MyProxy Server, which will live there  \n for specified time. \n \n"
				+ "3. CA (Cerification Authority) root certificates, MyProxyServer CA, root certificates,  \n are required in "
				+ System.getProperty("taverna.home")
				+ "/grid-certificates directory. \n \n"
				+ "4. If CA root certificates are missing in the above directory."
				+ "\n One can add them by downloading them  from  \n   'https://dist.eugridpma.org/distribution/igtf/current/accredited/tgz/'"
				+ "\n or obtain them from certificate's CA  \n\n"
				+ "Errors: If you see 'java.security.InvalidKeyException: Illegal key size' error message, \n please donwload and add JCE Unlimited Strength"
				+ "Policy  \nfiles into java installation as specified in ce_policy-1_x.x.zip \n\n ";
		JTextArea area = new JTextArea(text);

		helpPanel.add(area);
		return helpPanel;
	}

}
