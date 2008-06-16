package net.sf.taverna.t2.workbench.ui.activitypalette;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class ActivityPaletteTester {
	
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		ActivityPaletteComponent palette = new ActivityPaletteComponent();
		frame.add(palette);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(300, 600);
		frame.setVisible(true);
	}

}
