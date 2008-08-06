package net.sf.taverna.t2.workbench.iterationstrategy.editor;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

public class IterationStrategyIcons {

	private static Logger logger = Logger
			.getLogger(IterationStrategyIcons.class);

	public static ImageIcon joinIteratorIcon, lockStepIteratorIcon,
			leafnodeicon;

	static {
		try {
			Class<?> c = IterationStrategyIcons.class;
			joinIteratorIcon = new ImageIcon(c
					.getResource("../icons/crossproducticon.png"));
			lockStepIteratorIcon = new ImageIcon(c
					.getResource("../icons/dotproducticon.png"));
			leafnodeicon = new ImageIcon(c
					.getResource("../icons/leafnodeicon.png"));
		} catch (Exception ex) {
			logger.warn("Could not find icon", ex);
		}
	}
}
