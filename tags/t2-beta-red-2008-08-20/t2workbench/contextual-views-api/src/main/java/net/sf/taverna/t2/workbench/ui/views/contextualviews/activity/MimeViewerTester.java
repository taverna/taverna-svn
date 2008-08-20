package net.sf.taverna.t2.workbench.ui.views.contextualviews.activity;

import javax.swing.JFrame;

public class MimeViewerTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JFrame mimeFrame = new JFrame();
		MimeTypeConfig mimeConf = new MimeTypeConfig();
		mimeFrame.add(mimeConf);
		mimeFrame.setTitle("mime");
		mimeFrame.setVisible(true);
	}

}
