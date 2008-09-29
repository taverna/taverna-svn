package net.sf.taverna.t2.plugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.JTree;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.renderers.Renderer;
import net.sf.taverna.t2.renderers.RendererException;
import net.sf.taverna.t2.renderers.RendererRegistry;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

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

	private static Logger logger = Logger.getLogger(RendererPopup.class);
	private JTree tree;
	private final DataFacade dataFacade;
	private JPanel renderedResultPanel;
	private RenderedResultComponent renderedResultComponent;

	public RendererPopup(JTree tree, DataFacade dataFacade, RenderedResultComponent renderedResultComponent) {
		this.tree = tree;
		this.dataFacade = dataFacade;
		this.renderedResultComponent = renderedResultComponent;
		renderedResultPanel = new JPanel();
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
		EntityIdentifier token  = null;
		if (lastSelectedPathComponent != null) {
			JPopupMenu menu = new JPopupMenu();
			menu.setLabel("Available Renderers:");
			menu.addSeparator();
			if (lastSelectedPathComponent instanceof ResultTreeChildNode) {
				List<String> types = ((ResultTreeChildNode) lastSelectedPathComponent)
						.getMimeTypes();
				RendererRegistry rendererRegistry = new RendererRegistry();
				token = ((ResultTreeChildNode) lastSelectedPathComponent).getEntityIdentifier();
					
				List<Renderer> allRenderers = new ArrayList<Renderer>();
				// if there are no renderers then display these MIME types in a
				// dialogue box
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
				menu.add(new SaveAction(dataFacade, token));
				menu.show(tree, e.getX(), e.getY());
			}
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
				JComponent component = null;
				try {
					component = guiRenderer.getComponent(guiEntityIdentifier,
							guiDataFacade);
				} catch (RendererException e1) {//maybe this should be Exception
					// show the user that something unexpected has happened but
					// continue
					component = new JTextArea(
							"Could not render using renderer type "
									+ guiRenderer.getClass().getName()
									+ "\n"
									+ "Please try with a different renderer if available and consult log for details of problem");
					logger.warn("Couln not render using "
							+ guiRenderer.getClass().getName(), e1);
				}
//				RenderedResultComponent rendererComponent = RendererResultComponentFactory.getInstance().getRendererComponent();
//				rendererComponent.setResultComponent(component);
				renderedResultComponent.setResultComponent(component);
			}

		});
		return menuItem;
	}
	
	private class SaveAction extends AbstractAction {

		private final DataFacade dataFacade2;
		private final EntityIdentifier entityIdentifier;

		private SaveAction(DataFacade dataFacade, EntityIdentifier entityIdentifier) {
			super("Save to file"); //icon?
			dataFacade2 = dataFacade;
			this.entityIdentifier = entityIdentifier;
			
		}

		public void actionPerformed(ActionEvent ae) {
			Object resolve = null;
			try {
				resolve = dataFacade2.resolve(entityIdentifier);
			} catch (RetrievalException e) {
				JOptionPane.showMessageDialog(tree,
						"Problem saving data : \n" + e.getMessage(),
						"Exception!", JOptionPane.ERROR_MESSAGE);
			} catch (NotFoundException e) {
				JOptionPane.showMessageDialog(tree,
						"Problem saving data : \n" + e.getMessage(),
						"Exception!", JOptionPane.ERROR_MESSAGE);
			}
			JFileChooser fc = new JFileChooser();
			String curDir = System.getProperty("user.home");
			fc.setCurrentDirectory(new File(curDir));
			// Popup a save dialog and allow the user to store
			// the data to disc
			int returnVal = fc.showSaveDialog(tree);
			if (returnVal != JFileChooser.APPROVE_OPTION) {
				return;
			}
			File file = fc.getSelectedFile();
			try {
				FileOutputStream fos = new FileOutputStream(file);
				if (resolve instanceof byte[]) {
					logger.info("saving resolved entity as byte stream");
					fos.write((byte[]) resolve);
					fos.flush();
					fos.close();
				} else if (resolve instanceof InputStream ){ 
					logger.info("saving resolved entity as input stream");
					IOUtils.copy((InputStream) resolve, fos);
			        fos.flush();
			        fos.close();
				} else {
					logger.info("saving resolved entity as a string");
					Writer out =
						new BufferedWriter(new OutputStreamWriter(fos));
					out.write((String) resolve);
					fos.flush();
					out.flush();
					fos.close();
					out.close();
				}
			} catch (IOException ioe) {
				JOptionPane.showMessageDialog(tree,
					"Problem saving data : \n" + ioe.getMessage(),
					"Exception!", JOptionPane.ERROR_MESSAGE);
			}
		}

	}
}
