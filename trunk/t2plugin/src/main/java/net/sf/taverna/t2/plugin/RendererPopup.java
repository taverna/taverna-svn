package net.sf.taverna.t2.plugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.renderers.Renderer;
import net.sf.taverna.t2.renderers.RendererRegistry;

/**
 * Displays a {@link JPopupMenu} containing the MIME types appropriate to the
 * workflow result. On selecting one it gets the {@link JPanel} from the
 * corresponding {@link Renderer} and renders the results
 * 
 * Inspired by the ScavengerTreePopupHandler in T1
 * 
 * @author Ian Dunlop
 * 
 */
public class RendererPopup extends MouseAdapter {

	private JTree tree;
	private final DataFacade dataFacade;

	public RendererPopup(JTree tree, DataFacade dataFacade) {
		this.tree = tree;
		this.dataFacade = dataFacade;
	}

	public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) {
			doEvent(e);
		}
	}

	/**
	 * Similarly handle the mouse released event
	 */
	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {
			doEvent(e);
		}
	}

	void doEvent(MouseEvent e) {
		Object lastSelectedPathComponent = tree.getLastSelectedPathComponent();
		if (lastSelectedPathComponent != null) {
			JPopupMenu menu = new JPopupMenu();
			menu.setLabel("Available Renderers:");
			menu.addSeparator();
			if (lastSelectedPathComponent instanceof ResultTreeNode) {
				List<String> types = ((ResultTreeNode) lastSelectedPathComponent)
						.getMimeTypes();
				RendererRegistry rendererRegistry = new RendererRegistry();
				EntityIdentifier token = ((ResultTreeNode) lastSelectedPathComponent)
						.getToken();
				List<Renderer> allRenderers = new ArrayList<Renderer>();
				//if there are no renderers then display these MIME types in a dialogue box
				String allMimeTypes = "";
				for (String type : types) {
					allMimeTypes = allMimeTypes + type + "\n";
					List<Renderer> renderersForMimeType = rendererRegistry
							.getRenderersForMimeType(dataFacade, token, type);
					for (Renderer renderer : renderersForMimeType) {
						if (!allRenderers.contains(renderer)) {
							allRenderers.add(renderer);
						}
					}
				}
				if (allRenderers.isEmpty()) {
					JOptionPane.showMessageDialog(tree,
							"Unable to display for mime types " + allMimeTypes,
							"Unable to render", JOptionPane.WARNING_MESSAGE);
				} else {
					for (Renderer renderer : allRenderers) {
						menu
								.add(getMenuForRenderer(renderer, dataFacade,
										token));
						menu.addSeparator();
					}
				}
			}
			menu.show(tree, e.getX(), e.getY());
		}

	}

	/**
	 * When the {@link JMenuItem} is selected in the {@link RendererPopup} the
	 * appropriate {@link Renderer} will be displayed
	 * 
	 * @param renderer
	 * @param dataFacade
	 * @param identifier
	 * @return
	 */
	private JMenuItem getMenuForRenderer(Renderer renderer,
			DataFacade dataFacade, EntityIdentifier identifier) {
		final Renderer guiRenderer = renderer;
		final DataFacade guiDataFacade = dataFacade;
		final EntityIdentifier guiEntityIdentifier = identifier;
		String type = renderer.getType();
		JMenuItem menuItem = new JMenuItem("Render as " + type);
		menuItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				JDialog resultDialog = new JDialog();
				resultDialog.add(new JScrollPane(guiRenderer.getComponent(
						guiEntityIdentifier, guiDataFacade)));
				resultDialog.setSize(500, 500);
				resultDialog.setVisible(true);
			}

		});
		return menuItem;
	}
}
