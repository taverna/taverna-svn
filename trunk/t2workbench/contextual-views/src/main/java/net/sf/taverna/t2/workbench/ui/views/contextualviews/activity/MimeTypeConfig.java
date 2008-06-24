package net.sf.taverna.t2.workbench.ui.views.contextualviews.activity;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;


public class MimeTypeConfig extends JPanel{
	
	private JTextArea mimeTypes;
	private JTextArea newMimeType;
	private List<String> mimeTypeList;
	private ActionListener listener;

	public MimeTypeConfig() {
		init();
	}

	private void init() {
		mimeTypeList = new ArrayList<String>();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		mimeTypes = new JTextArea();
		JLabel mimeLabel = new JLabel("Add new mime type");
		newMimeType = new JTextArea();
		JButton addMimeButton = new JButton("Add mime type");
		addMimeButton.addActionListener(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				String text = mimeTypes.getText();
				text  = text + "\n" + newMimeType.getText();
				mimeTypes.setText(text);
				mimeTypes.revalidate();
				mimeTypeList.add(newMimeType.getText());
			}
			
		});
		JButton OKbutton = new JButton("OK");
		OKbutton.addActionListener(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				listener.actionPerformed(e);
			}
			
		});
		add(mimeLabel);
		add(newMimeType);
		add(addMimeButton);
		add(mimeTypes);
		add(OKbutton);
	}

	public List<String> getMimeTypeList() {
		return mimeTypeList;
	}
	
	public void addNewMimeListener(ActionListener listener) {
		this.listener = listener;
	}

}
