package net.sf.taverna.t2.workbench.views.results;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;
import net.sf.taverna.t2.annotation.annotationbeans.MimeType;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ErrorDocument;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.Identified;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.renderers.Renderer;
import net.sf.taverna.t2.renderers.RendererException;
import net.sf.taverna.t2.renderers.RendererRegistry;

/**
 * Displays the {@link Component} returned from a workflow result
 * {@link Renderer} within a {@link JPanel}.
 * 
 * @author Ian Dunlop
 * 
 */
public class RenderedResultComponent extends JPanel {

	private static Logger logger = Logger.getLogger(RenderedResultComponent.class);
	
	private Component resultComponent;

	private JPanel resultPanel;
	
	private JComboBox renderers;
	
	private List<Renderer> renderersForMimeType;

	private RendererRegistry rendererRegistry = new RendererRegistry();
	
	private Map<ExternalReferenceSPI, String> mimeTypes = new HashMap<ExternalReferenceSPI, String>();
	
	private T2Reference t2Reference;
	
	private InvocationContext context;

	/**
	 * Create the {@link JPanel} which displays the results and place it in the
	 * centre in a {@link JScrollPane}
	 */
	protected RenderedResultComponent() {
		setLayout(new BorderLayout());
		
		renderers = new JComboBox();
		renderers.setEditable(false);
		renderers.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				System.out.println("itemStateChanged");
				if (e.getStateChange() == ItemEvent.SELECTED) {
					int selectedIndex = renderers.getSelectedIndex();
					Renderer renderer = renderersForMimeType.get(selectedIndex);
					JComponent component = null;
					try {
						component = renderer.getComponent(context
								.getReferenceService(), t2Reference);
					} catch (RendererException e1) {// maybe this should be
						// Exception
						// show the user that something unexpected has happened but
						// continue
						component = new JTextArea(
								"Could not render using renderer type "
								+ renderer.getClass().getName()
								+ "\n"
								+ "Please try with a different renderer if available and consult log for details of problem");
						logger.warn("Couln not render using "
								+ renderer.getClass().getName(), e1);
					}
					resultPanel.removeAll();
					resultPanel.add(component, BorderLayout.CENTER);
					revalidate();
				}
			}
		});
		add(renderers, BorderLayout.NORTH);
		
		
		resultPanel = new JPanel(new BorderLayout());
		resultPanel.add(new JLabel("To display: select a result from the \"Results Panel\""), BorderLayout.CENTER);
		resultPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(new JScrollPane(resultPanel), BorderLayout.CENTER);
	}

	/**
	 * Change the {@link Renderer} {@link Component} which will be displayed and
	 * redraw it.
	 * 
	 * @param newResultComponent
	 */
	public void setResult(ResultTreeNode result) {
		t2Reference = result.getReference();
		context = result.getContext();
		Identified identified = context.getReferenceService().resolveIdentifier(t2Reference, null, context);
		String mimeType = null;
		if (identified instanceof ReferenceSet) {
			ReferenceSet referenceSet = (ReferenceSet) identified;
			List<ExternalReferenceSPI> externalReferences = new ArrayList<ExternalReferenceSPI>(referenceSet.getExternalReferences());
			Collections.sort(externalReferences, new Comparator<ExternalReferenceSPI>() {
				public int compare(ExternalReferenceSPI o1, ExternalReferenceSPI o2) {
					return (int) (o1.getResolutionCost() - o2.getResolutionCost());
				}
			});
			for (ExternalReferenceSPI externalReference : externalReferences) {
				mimeType = getMimeType(externalReference, context);
				if (mimeType != null) {
					break;
				}
			}
		} else if (identified instanceof ErrorDocument) {
			ErrorDocument errorDocument = (ErrorDocument) identified;
			
		}
		if (mimeType == null) {
			mimeType = "text/plain";
		}

		renderersForMimeType = rendererRegistry.getRenderersForMimeType(context, t2Reference, mimeType);
		Object[] rendererList = new Object[renderersForMimeType.size()];
		for (int i = 0; i < rendererList.length; i++) {
			rendererList[i] = renderersForMimeType.get(i).getType();
		}
		renderers.setModel(new DefaultComboBoxModel(rendererList));
		if (renderersForMimeType.size() > 0) {
			renderers.setSelectedIndex(-1);
			renderers.setSelectedIndex(0);
		}
		
	}

	private String getMimeType(ExternalReferenceSPI externalReference, InvocationContext context) {
		if (!mimeTypes.containsKey(t2Reference)) {
			InputStream inputStream = externalReference.openStream(context);
			byte[] bytes = new byte[64];
			try {
				inputStream.read(bytes);
				mimeTypes.put(externalReference, Magic.getMagicMatch(bytes, true).getMimeType());
			} catch (IOException e) {
				e.printStackTrace();
				logger.debug("Failed to read from stream to determine mimetype", e);
			} catch (MagicParseException e) {
				e.printStackTrace();
				logger.debug("Error calling mime magic", e);
			} catch (MagicMatchNotFoundException e) {
				e.printStackTrace();
				logger.debug("Error calling mime magic", e);
			} catch (MagicException e) {
				e.printStackTrace();
				logger.debug("Error calling mime magic", e);
			} finally {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
					logger.debug("Failed to close stream after determining mimetype", e);
				}
			}
		}
		return mimeTypes.get(externalReference);
	}
	
	
}
