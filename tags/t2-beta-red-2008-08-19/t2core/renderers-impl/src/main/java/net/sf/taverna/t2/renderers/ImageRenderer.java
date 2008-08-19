package net.sf.taverna.t2.renderers;

import java.awt.image.ImageProducer;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;

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

	public String getType() {
		return "Image";
	}

	public boolean canHandle(ReferenceService referenceService,
			T2Reference reference, String mimeType) throws RendererException {
		return canHandle(mimeType);
	}

	public JComponent getComponent(ReferenceService referenceService,
			T2Reference reference) throws RendererException {
		Object data = null;
		try {
			data = referenceService.renderIdentifier(reference, byte[].class,
					null);
		} catch (Exception e) {
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
}
