package net.sf.taverna.ocula.frame;

import javax.swing.JComponent;

import org.jdom.Element;

/**
 * Value object that contains an {@link OculaFrame} and the {@link Element} from
 * which it was created.
 * 
 * @author Ismael Juma (ismael@juma.me.uk)
 */
public final class FrameAndElement {
    private final OculaFrame frame;
    private final Element element;
    
    /**
     * Gets the element from which the frame was created.
     */
    public final Element getElement() {
	return element;
    }

    /**
     * Gets the frame within this object. The OculaFrame is guaranteed to be
     * a JComponent.
     */
    public final OculaFrame getFrame() {
        return frame;
    }

    /**
     * Creates a new FrameAndElement containing the objects received as the
     * parameters.
     * 
     * @param frame An OculaFrame that is also a JComponent.
     * @param element The Element from which the OculaFrame was created.
     */
    public FrameAndElement(OculaFrame frame, Element element) {
	if (frame == null || element == null) {
	    throw new IllegalArgumentException("frame and element must not be " +
	    		"null.");
	}
	if (!(frame instanceof JComponent)) {
	    throw new IllegalArgumentException("frame must be a JComponent or" +
	    		" subclass");
	}
	this.frame = frame;
	this.element = element;
    }
}