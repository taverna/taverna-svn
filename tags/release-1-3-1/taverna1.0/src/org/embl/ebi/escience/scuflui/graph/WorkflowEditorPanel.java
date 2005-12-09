/*
 * Created on May 18, 2005
 */
package org.embl.ebi.escience.scuflui.graph;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.AdvancedModelExplorer;
import org.embl.ebi.escience.scuflui.ExtensionFileFilter;
import org.embl.ebi.escience.scuflui.ScuflIcons;
import org.embl.ebi.escience.scuflui.ScuflUIComponent;
import org.embl.ebi.escience.scuflui.actions.LoadWebWorkflowAction;
import org.embl.ebi.escience.scuflui.actions.LoadWorkflowAction;
import org.embl.ebi.escience.scuflui.actions.OfflineToggleModel;
import org.embl.ebi.escience.scuflui.actions.ResetAction;
import org.embl.ebi.escience.scuflui.actions.SaveWorkflowAction;

/**
 * COMMENT
 * 
 * @author <a href="mailto:ktg@cs.nott.ac.uk">Kevin Glover</a>
 * @version $Revision: 1.7 $
 */
public class WorkflowEditorPanel extends JPanel implements ScuflUIComponent
{
	final JFileChooser fc = new JFileChooser();
	WorkflowEditor editor;

	/**
	 * 
	 */
	public WorkflowEditorPanel()
	{
		super();
		// TODO Implement WorkflowEditorPanel constructor
	}

	public void attachToModel(ScuflModel model)
	{
		setLayout(new BorderLayout());

		editor = new WorkflowEditor();
		editor.attachToModel(model);		
		
		JCheckBox offline = new JCheckBox("Offline");
		offline.setModel(new OfflineToggleModel(model));

		Action saveImage = new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				Preferences prefs = Preferences.userNodeForPackage(AdvancedModelExplorer.class);
				String curDir = prefs.get("currentDir", System.getProperty("user.home"));
				fc.setDialogTitle("Save Workflow");
				fc.resetChoosableFileFilters();
				fc.setFileFilter(new ExtensionFileFilter(new String[] { "png" }));
				fc.setCurrentDirectory(new File(curDir));
				int returnVal = fc.showSaveDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					File file = fc.getSelectedFile();

					Object[] cells = editor.getRoots();
					if (cells.length > 0)
					{
						int inset = 15;
						Rectangle2D bounds = editor.getCellBounds(cells);
						editor.toScreen(bounds);
						BufferedImage img = new BufferedImage(
								(int) bounds.getWidth() + (2 * inset), (int) bounds.getHeight()
										+ (2 * inset), BufferedImage.TYPE_INT_ARGB);
						Graphics2D graphics = img.createGraphics();
						if (editor.isOpaque())
						{
							graphics.setColor(editor.getBackground());
							graphics.fillRect(0, 0, img.getWidth(), img.getHeight());

						}
						else
						{
							graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR,
									0.0f));
							graphics.fillRect(0, 0, img.getWidth(), img.getHeight());
							graphics.setComposite(AlphaComposite.SrcOver);
						}
						graphics.translate((int) (-bounds.getX() + inset),
								(int) (-bounds.getY() + inset));
						boolean tmp = editor.isDoubleBuffered();
						editor.setDoubleBuffered(false);
						editor.paint(graphics);
						editor.setDoubleBuffered(tmp);
						try
						{
							ImageIO.write(img, "png", file);
						}
						catch (IOException e1)
						{
							JOptionPane.showMessageDialog(null, "Problem saving workflow : \n"
									+ e1.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
		};
		saveImage.putValue(Action.SHORT_DESCRIPTION, "Save image of workflow");
		saveImage.putValue(Action.SMALL_ICON, ScuflIcons.savePNGIcon);

		JCheckBox boring = new JCheckBox("Show Boring");
		boring.setModel(editor.new ShowBoringModel());

		// Create the tool bar
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setRollover(true);
		toolbar.setMaximumSize(new Dimension(2000, 30));
		toolbar.setBorderPainted(true);

		toolbar.add(new LoadWorkflowAction(model));
		toolbar.add(new LoadWebWorkflowAction(model));
		toolbar.add(new SaveWorkflowAction(model));
		toolbar.add(saveImage);
		toolbar.addSeparator();
		toolbar.add(offline);
		toolbar.add(boring);
		toolbar.add(Box.createHorizontalGlue());
		toolbar.add(new ResetAction(model));

		JScrollPane scrollPane = new JScrollPane(editor);
		scrollPane.setPreferredSize(new Dimension(0, 0));

		add(toolbar, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
	}

	public void detachFromModel()
	{
		editor.detachFromModel();
	}

	public ImageIcon getIcon()
	{
		return ScuflIcons.windowDiagram;
	}

	public String getName()
	{
		return "Workflow Editor (BETA)";
	}
}
