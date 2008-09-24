package net.sf.taverna.ocula.frame;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import net.sf.taverna.ocula.Ocula;
import net.sf.taverna.ocula.ui.ColourSet;
import net.sf.taverna.ocula.ui.Icons;
import net.sf.taverna.ocula.ui.ResultSetPanel;

import org.jdom.Element;

/**
 * Handles the &lt;compound&gt; element and builds frames that contain other
 * frames. It does this by grouping a number of frames into one parent
 * frame.
 * 
 * @author Ismael Juma (ismael@juma.me.uk)
 *
 */
public class CompoundFrameBuilder implements FrameSPI {

    public String getElementName() {
	return "compound";
    }

    public OculaFrame makeFrame(Ocula ocula, Element element) {
	String name = element.getAttributeValue("name", "");
	String iconName = element.getAttributeValue("icon", "NoIcon");
	Icon icon = Icons.getIcon(iconName);
	final CompoundFrame cf = new CompoundFrame(name, icon);
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		cf.getProgressBar().setIndeterminate(true);
	    }
	});
	for (Iterator i = element.getChildren().iterator(); i.hasNext();) {
	    OculaFrame frame = ocula.getFrameHandler().getFrame(
		    (Element) i.next());
	    if (frame != null) {
		final boolean hasNext = i.hasNext();
		final Component c = (Component) frame;
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			cf.getContents().add(c);
			if (hasNext) {
			    Component box = Box.createRigidArea(new Dimension(5, 5));
			    cf.getContents().add(box);
			    cf.getContents().revalidate();
			}
		    }
		});
	    }
	}
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		cf.getProgressBar().setIndeterminate(false);
		cf.getProgressBar().setValue(100);
	    }
	});
	return cf;
    }
    
    /**
     * Simple subclass of ResultSetPanel that changes the layout manager to
     * a vertically oriented BoxLayout and changes the internal empty border
     * to a thicker version.
     */
    class CompoundFrame extends ResultSetPanel implements OculaFrame	{
	public CompoundFrame(String name, Icon icon) {
	    super(name, icon);
	    contentsPanel.setLayout(new BoxLayout(contentsPanel,
		    BoxLayout.Y_AXIS));
	    Border border = BorderFactory.createCompoundBorder(BorderFactory
		    .createLineBorder(ColourSet.getColour("ocula.panelborder"),
			    2), BorderFactory.createEmptyBorder(8, 8, 8, 8));
	    contentsPanel.setBorder(border);
	}
	
    }

}
