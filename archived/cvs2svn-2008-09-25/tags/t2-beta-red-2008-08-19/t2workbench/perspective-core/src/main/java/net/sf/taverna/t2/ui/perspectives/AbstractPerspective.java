package net.sf.taverna.t2.ui.perspectives;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.swing.ImageIcon;

import net.sf.taverna.t2.workbench.ui.zaria.PerspectiveSPI;

import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * An abstract implementation of a perspective that handles the storing of the
 * layout XML if modified via the 'update' method, which once set causes this
 * XML to be used rather than the bundled resource. Concrete subclass should
 * provide getText, getButtonIcon, and getLayoutResourceStream.
 * 
 * @author Stuart Owen
 * 
 */
public abstract class AbstractPerspective implements PerspectiveSPI {

	private Element layoutElement = null;
	private boolean visible = true;

	public InputStream getLayoutInputStream() {
		if (layoutElement == null) {
			return getLayoutResourceStream();
		} else {
			XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
			String xml = outputter.outputString(layoutElement);
			return new ByteArrayInputStream(xml.getBytes());
		}
	}

	public void update(Element layoutElement) {
		this.layoutElement = layoutElement;
	}

	/**
	 * The name of the perspective
	 */
	public abstract String getText();

	/**
	 * The icon for the perspective
	 */
	public abstract ImageIcon getButtonIcon();

	/**
	 * 
	 * @return the resource stream for the perspective layout XML
	 */
	protected abstract InputStream getLayoutResourceStream();

	/**
	 * Position hint, default to 101, meaning that perspective that don't
	 * provide a hint will always appear towards the end (Built-in perspectives
	 * coming first in a controlled order).
	 */
	public int positionHint() {
		return 101;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

}
