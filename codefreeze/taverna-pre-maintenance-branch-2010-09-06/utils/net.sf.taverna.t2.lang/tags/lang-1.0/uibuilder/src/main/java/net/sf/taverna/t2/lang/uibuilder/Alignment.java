package net.sf.taverna.t2.lang.uibuilder;

import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

/**
 * Static utility method to align alignable components within a container
 * 
 * @author Tom Oinn
 * 
 */
public abstract class Alignment {

	private static Logger logger = Logger
	.getLogger(Alignment.class);

	/**
	 * Find all instances of BeanFieldTextArea in the specified container and
	 * set all label widths to the same as the widest of them, aligning the
	 * content areas as a result
	 * 
	 * @param container
	 */
	public static void alignInContainer(Container container) {
		int widestLabel = 0;
		final List<AlignableComponent> fields = new ArrayList<AlignableComponent>();
		for (Component comp : container.getComponents()) {
			if (comp instanceof AlignableComponent) {
				AlignableComponent field = (AlignableComponent) comp;
				int fieldWidth = field.getLabelWidth();
				if (fieldWidth > widestLabel) {
					widestLabel = fieldWidth;
				}
				fields.add(field);
			}
		}
		final int widestLabelVal = widestLabel;
		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						for (AlignableComponent field : fields) {
							field.setLabelWidth(widestLabelVal);
						}
					}
				});
			} catch (InterruptedException e) {
				logger.error("", e);
			} catch (InvocationTargetException e) {
				logger.error("", e);
			}
		} else {
			for (AlignableComponent field : fields) {
				field.setLabelWidth(widestLabelVal);
			}
		}
	}

}
