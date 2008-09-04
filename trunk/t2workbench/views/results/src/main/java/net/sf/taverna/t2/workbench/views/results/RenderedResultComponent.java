/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.workbench.views.results;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;
import net.sf.taverna.t2.annotation.annotationbeans.MimeType;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ErrorDocument;
import net.sf.taverna.t2.reference.ErrorDocumentService;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.Identified;
import net.sf.taverna.t2.reference.IdentifiedList;
import net.sf.taverna.t2.reference.ListService;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.StackTraceElementBean;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.T2ReferenceType;
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
				if (e.getStateChange() == ItemEvent.SELECTED) {
					int selectedIndex = renderers.getSelectedIndex();
					if (renderersForMimeType != null && renderersForMimeType.size() > selectedIndex) {
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
			}
		});
		
		JPanel renderersPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		renderersPanel.add(new JLabel("Result Type"));
		renderersPanel.add(renderers);
		
		add(renderersPanel, BorderLayout.NORTH);
		
		
		resultPanel = new JPanel(new BorderLayout());
//		resultPanel.add(new JLabel("Select a result from the \"Results Panel\""), BorderLayout.CENTER);
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
		} else if (identified instanceof ErrorDocument) {
			ErrorDocument errorDocument = (ErrorDocument) identified;
			renderersForMimeType = null;
			
			DefaultMutableTreeNode root = new DefaultMutableTreeNode("Error Trace");
			buildErrorDocumentTree(root, errorDocument);
			
			JTree errorTree = new JTree(root);
			errorTree.setCellRenderer(new DefaultTreeCellRenderer() {

				public Component getTreeCellRendererComponent(JTree tree,
						Object value, boolean selected, boolean expanded,
						boolean leaf, int row, boolean hasFocus) {
					Component renderer = null;
					if (value instanceof DefaultMutableTreeNode) {
						DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
						Object userObject = treeNode.getUserObject();
						if (userObject instanceof ErrorDocument) {
							ErrorDocument errorDocument = (ErrorDocument) userObject;
							renderer = super.getTreeCellRendererComponent(tree,
									errorDocument.getMessage(),
									selected, expanded, leaf, row, hasFocus);
						}
					}
					if (renderer == null) {
						renderer = super.getTreeCellRendererComponent(tree, value,
							selected, expanded, leaf, row, hasFocus);
					}
					if (renderer instanceof JLabel) {
						JLabel label = (JLabel) renderer;
						label.setIcon(null);
					}
					return renderer;
				}
				
			});
	
			renderers.setModel(new DefaultComboBoxModel(new String[] {"Error Document"}));
			resultPanel.removeAll();
			resultPanel.add(errorTree, BorderLayout.CENTER);
		}
		
	}
	
	private void buildErrorDocumentTree(DefaultMutableTreeNode node, ErrorDocument errorDocument) {
		DefaultMutableTreeNode child = new DefaultMutableTreeNode(errorDocument);
		String exceptionMessage = errorDocument.getExceptionMessage();
		if (exceptionMessage != null && !exceptionMessage.equals("")) {
			DefaultMutableTreeNode exceptionMessageNode = new DefaultMutableTreeNode(exceptionMessage);
			child.add(exceptionMessageNode);
			List<StackTraceElementBean> stackTrace = errorDocument.getStackTraceStrings();
			if (stackTrace.size() > 0) {
				for (StackTraceElementBean stackTraceElement : stackTrace) {
					exceptionMessageNode.add(new DefaultMutableTreeNode(getStackTraceElementString(stackTraceElement)));
				}
			}

		}
		node.add(child);

		Set<T2Reference> errorReferences = errorDocument.getErrorReferences();
		for (T2Reference reference : errorReferences) {
			if (reference.getReferenceType().equals(T2ReferenceType.ErrorDocument)) {
				ErrorDocumentService errorDocumentService = context.getReferenceService().getErrorDocumentService();
				ErrorDocument causeErrorDocument = errorDocumentService.getError(reference);
				if (errorReferences.size() == 1) {
					buildErrorDocumentTree(node, causeErrorDocument);
				} else {
					buildErrorDocumentTree(child, causeErrorDocument);
				}
			} else if (reference.getReferenceType().equals(T2ReferenceType.IdentifiedList)) {
				List<ErrorDocument> errorDocuments = getErrorDocuments(reference);
				if (errorDocuments.size() == 1) {
					buildErrorDocumentTree(node, errorDocuments.get(0));
				} else {
					for (ErrorDocument errorDocument2 : errorDocuments) {
						buildErrorDocumentTree(child, errorDocument2);
					}
				}
			}
			
		}

	}
	
	private String getStackTraceElementString(StackTraceElementBean stackTraceElement) {
		StringBuilder sb = new StringBuilder();
		sb.append(stackTraceElement.getClassName());
		sb.append('.');
		sb.append(stackTraceElement.getMethodName());
		if (stackTraceElement.getFileName() == null) {
			sb.append("(unknown file)");
		} else {
			sb.append('(');
			sb.append(stackTraceElement.getFileName());
			sb.append(':');
			sb.append(stackTraceElement.getLineNumber());
			sb.append(')');
		}
		return sb.toString();
	}
	
	private List<ErrorDocument> getErrorDocuments(T2Reference reference) {
		List<ErrorDocument> errorDocuments = new ArrayList<ErrorDocument>();
		if (reference.getReferenceType().equals(T2ReferenceType.ErrorDocument)) {
			ErrorDocumentService errorDocumentService = context.getReferenceService().getErrorDocumentService();
			errorDocuments.add(errorDocumentService.getError(reference));			
		} else if (reference.getReferenceType().equals(T2ReferenceType.IdentifiedList)) {
			ListService listService = context.getReferenceService().getListService();
			IdentifiedList<T2Reference> list = listService.getList(reference);
			for (T2Reference listReference : list) {
				errorDocuments.addAll(getErrorDocuments(listReference));
			}
		}
		return errorDocuments;
	}

	private String getMimeType(ExternalReferenceSPI externalReference, InvocationContext context) {
		if (!mimeTypes.containsKey(t2Reference)) {
			InputStream inputStream = externalReference.openStream(context);
			try {
				byte[] bytes = new byte[64];
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
