/*
 * FetaResources.java
 *
 * Created on March 4, 2005, 3:30 PM
 */

package uk.ac.man.cs.img.fetaClient.resource;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.net.URL;
import java.util.HashMap;

import javax.swing.ImageIcon;

/**
 * 
 * @author alperp
 */
public class FetaResources {

	private static HashMap icons = new HashMap();

	private static Cursor busyCursor = null;

	/** Creates a new instance of FetaResources */
	public FetaResources() {
	}

	public static ImageIcon getFetaIcon() {
		// return new ImageIcon
		// (ClassLoader.getSystemResource("uk/ac/man/cs/img/fetaClient/resource/Feta.gif"));

		URL iconURL = FetaResources.class.getResource("Feta.gif");
		if (iconURL == null) {
			return null;
		} else {
			return new ImageIcon(iconURL);
		}

	}

	public static ImageIcon getPedroIcon() {

		URL iconURL = FetaResources.class.getResource("pedro.jpg");
		if (iconURL == null) {
			return null;
		} else {
			return new ImageIcon(iconURL);
		}

	}

	public static ImageIcon getIcon(String iconFileName) {
		ImageIcon icon = (ImageIcon) icons.get(iconFileName);
		if (icon == null) {
			URL iconURL = FetaResources.class.getResource(iconFileName);
			if (iconURL == null) {
				return getFetaIcon();
			}
			icon = new ImageIcon(iconURL);
			icons.put(iconFileName, icon);
		}
		return icon;
	}

	public static Cursor getBusyCursor() {

		if (busyCursor == null) {

			try {

				ImageIcon currentIcon = getIcon("working.gif");
				ImageObserver imageObserver = currentIcon.getImageObserver();

				Image image = currentIcon.getImage();
				int width = image.getWidth(imageObserver);
				int height = image.getHeight(imageObserver);

				Point hotSpot = new Point(0, 0);
				Toolkit toolKit = Toolkit.getDefaultToolkit();

				BufferedImage bufferedImage = new BufferedImage(width, height,
						BufferedImage.TYPE_4BYTE_ABGR);

				Graphics graphics = bufferedImage.getGraphics();

				graphics.drawImage(image, 0, 0, width, height, null);

				busyCursor = toolKit.createCustomCursor(bufferedImage, hotSpot,
						"working");

			} catch (Exception err) {
				err.printStackTrace(System.out);
			}
		}
		return busyCursor;
	}

}
