package uk.ac.soton.mib104.t2.activities.oauth.util;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * IconManager is a factory for icons.
 * 
 * @author Mark Borkum
 */
public final class IconManager {
	
	private static Icon ICON_LARGE;
	
	private static Icon ICON_SMALL;
	
	private static Icon ICON_SMALL_REST;
	
	/**
	 * Returns the large OAuth icon (53x53 pixels).
	 * 
	 * @return  the large OAuth icon.
	 */
	public static final Icon getLargeIcon() {
		if (ICON_LARGE == null) {
			ICON_LARGE = new ImageIcon(IconManager.class.getResource("/oauthIcon_53x53.png"));
		}
		
		return ICON_LARGE;
	}

	/**
	 * Returns the small OAuth icon (16x16 pixels).
	 * 
	 * @return  the small OAuth icon.
	 */
	public static final Icon getSmallIcon() {
		if (ICON_SMALL == null) {
			ICON_SMALL = new ImageIcon(IconManager.class.getResource("/oauthIcon_16x16.png"));
		}
		
		return ICON_SMALL;
	}
	
	/**
	 * Returns the "small" OAuth+REST icon (18x18 pixels).
	 * 
	 * @return  the small OAuth+REST icon.
	 */
	public static final Icon getSmallRESTIcon() {
		if (ICON_SMALL_REST == null) {
			ICON_SMALL_REST = new ImageIcon(IconManager.class.getResource("/service_type_rest_oauth.png"));
		}
		
		return ICON_SMALL_REST;
	}
	
	/**
	 * Sole constructor.
	 */
	private IconManager() {
		super();
	}

}
