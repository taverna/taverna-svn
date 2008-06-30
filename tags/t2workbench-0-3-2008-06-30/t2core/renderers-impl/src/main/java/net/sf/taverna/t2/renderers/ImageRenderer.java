package net.sf.taverna.t2.renderers;

import java.awt.image.ImageProducer;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;

/**
 * @author Matthew Pocock
 * @author Ian Dunlop
 */
public class ImageRenderer implements Renderer

{
	private Pattern pattern;

	public ImageRenderer() {
		pattern = Pattern.compile(".*image/.*");
	}

	public boolean canHandle(String mimeType) {
		return pattern.matcher(mimeType).matches();
	}

	public JComponent getComponent(EntityIdentifier entityIdentifier,
			DataFacade dataFacade) throws RendererException {
		Object data = null;
		try {
			data = dataFacade.resolve(entityIdentifier, byte[].class);
		} catch (RetrievalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (data instanceof byte[]) {
			// JLabel or something else?
			try {
				ImageIcon theImage = new ImageIcon((byte[]) data);
				return new JLabel(theImage);
			} catch (Exception e) {
				throw new RendererException("Unable to generate image", e);
			}
		} else if (data instanceof ImageProducer) {
			JLabel label = new JLabel();
			java.awt.Image image = label.createImage((ImageProducer) data);
			ImageIcon icon = new ImageIcon(image);
			label.setIcon(icon);
			return label;
		}

		return null;
	}

	public boolean canHandle(DataFacade facade,
			EntityIdentifier entityIdentifier, String mimeType)
			throws RendererException {
		return canHandle(mimeType);
	}

	public String getType() {
		return "Image";
	}
}
