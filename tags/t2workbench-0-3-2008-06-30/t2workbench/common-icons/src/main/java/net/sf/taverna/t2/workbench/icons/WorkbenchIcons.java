package net.sf.taverna.t2.workbench.icons;

import javax.swing.ImageIcon;

/**
 * A container for common icons used by the workbench
 * 
 * @author David Withers
 * @author Stian Soiland-Reyes
 */
@SuppressWarnings("unchecked")
public class WorkbenchIcons {


	public static ImageIcon deleteIcon;
	public static ImageIcon zoomIcon;
	public static ImageIcon webIcon;
	public static ImageIcon openIcon;
	public static ImageIcon runIcon;
	public static ImageIcon refreshIcon;
	public static ImageIcon editIcon;
	public static ImageIcon findIcon;
	public static ImageIcon folderOpenIcon;
	public static ImageIcon folderClosedIcon;
	public static ImageIcon newInputIcon;
	public static ImageIcon newIcon;
	public static ImageIcon newListIcon;
	public static ImageIcon inputValueIcon;
	public static ImageIcon xmlNodeIcon;
	public static ImageIcon saveIcon;
	public static ImageIcon leafIcon;
	public static ImageIcon saveMenuIcon;
	public static ImageIcon savePNGIcon;
	public static ImageIcon importIcon;
	public static ImageIcon importFileIcon;
	public static ImageIcon importUrlIcon;
	public static ImageIcon openurlIcon;
	public static ImageIcon openMenuIcon;
	public static ImageIcon pauseIcon;
	public static ImageIcon playIcon;
	public static ImageIcon stopIcon;
	public static ImageIcon breakIcon;
	public static ImageIcon rbreakIcon;
	public static ImageIcon tickIcon;
	public static ImageIcon renameIcon;
	public static ImageIcon databaseIcon;
	public static ImageIcon nullIcon;
	public static ImageIcon uninstallIcon;
	public static ImageIcon updateRecommendedIcon;
	public static ImageIcon updateIcon;
	public static ImageIcon searchIcon;
	public static ImageIcon pasteIcon;
	public static ImageIcon copyIcon;

	static {
		// Load the image files found in this package into the class.
		try {
			Class c = WorkbenchIcons.class;
			deleteIcon = new ImageIcon(c
					.getResource("generic/delete.gif"));
			zoomIcon = new ImageIcon(c.getResource("generic/zoom.gif"));
			webIcon = new ImageIcon(c.getResource("generic/web.gif"));
			openIcon = new ImageIcon(c.getResource("generic/open.gif"));
			runIcon = new ImageIcon(c.getResource("generic/run.gif"));
			refreshIcon = new ImageIcon(c
					.getResource("generic/refresh.gif"));
			editIcon = new ImageIcon(c.getResource("generic/edit.gif"));
			findIcon = new ImageIcon(c.getResource("generic/find.gif"));
			folderOpenIcon = new ImageIcon(c
					.getResource("generic/folder-open.png"));
			folderClosedIcon = new ImageIcon(c
					.getResource("generic/folder-closed.png"));
			newInputIcon = new ImageIcon(c
					.getResource("generic/newinput.gif"));
			newIcon = new ImageIcon(c.getResource("generic/newinput.gif"));
			newListIcon = new ImageIcon(c
					.getResource("generic/newlist.gif"));
			inputValueIcon = new ImageIcon(c
					.getResource("generic/inputValue.gif"));

			xmlNodeIcon = new ImageIcon(c
					.getResource("generic/xml_node.gif"));
			leafIcon = new ImageIcon(c.getResource("generic/leaf.gif"));
			saveIcon = new ImageIcon(c.getResource("generic/save.gif"));
			saveMenuIcon = new ImageIcon(c
					.getResource("generic/savemenu.gif"));
			savePNGIcon = new ImageIcon(c
					.getResource("generic/savepng.gif"));
			importIcon = new ImageIcon(c
					.getResource("generic/import.gif"));
			importFileIcon = new ImageIcon(c
					.getResource("generic/fileimport.png"));
			importUrlIcon = new ImageIcon(c
					.getResource("generic/urlimport.png"));
			openurlIcon = new ImageIcon(c
					.getResource("generic/openurl.gif"));
			openIcon = new ImageIcon(c.getResource("generic/open.gif"));
			openMenuIcon = new ImageIcon(c
					.getResource("generic/openmenu.gif"));
			pauseIcon = new ImageIcon(c.getResource("generic/pause.gif"));
			playIcon = new ImageIcon(c.getResource("generic/play.gif"));
			stopIcon = new ImageIcon(c.getResource("generic/stop.gif"));
			breakIcon = new ImageIcon(c.getResource("generic/break.gif"));
			rbreakIcon = new ImageIcon(c
					.getResource("generic/rbreak.gif"));
			tickIcon = new ImageIcon(c.getResource("generic/tick.gif"));
			renameIcon = new ImageIcon(c
					.getResource("generic/rename.png"));
			databaseIcon = new ImageIcon(c
					.getResource("generic/database.gif"));
			nullIcon = new ImageIcon(new java.awt.image.BufferedImage(1, 1,
					java.awt.image.BufferedImage.TYPE_INT_RGB));
			copyIcon = new ImageIcon(c.getResource("generic/copy.png"));
			pasteIcon = new ImageIcon(c.getResource("generic/paste.png"));
			searchIcon = new ImageIcon(c
					.getResource("generic/search.png"));
			updateIcon = new ImageIcon(c
					.getResource("generic/update.png"));
			updateRecommendedIcon = new ImageIcon(c
					.getResource("generic/updateRecommended.png"));
			uninstallIcon = new ImageIcon(c
					.getResource("generic/uninstall.png"));
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(ex.toString());
		}
	}
}
