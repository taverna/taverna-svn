package net.sf.taverna.t2.lang.ui.icons;

import javax.swing.ImageIcon;

public class Icons {

	public static ImageIcon okIcon;
	public static ImageIcon severeIcon;
	public static ImageIcon warningIcon;

	static {
		try {
			Class c = Icons.class;
			okIcon = new ImageIcon(c.getResource("ok.png"));
			severeIcon = new ImageIcon(c.getResource("severe.png"));
			warningIcon = new ImageIcon(c.getResource("warning.png"));

		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(ex.toString());
		}
	}
}
