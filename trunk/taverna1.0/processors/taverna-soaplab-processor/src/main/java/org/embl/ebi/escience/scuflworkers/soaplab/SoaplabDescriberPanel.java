/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */

package org.embl.ebi.escience.scuflworkers.soaplab;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.xml.namespace.QName;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.view.WorkflowSummaryAsHTML;
import org.embl.ebi.escience.scuflui.ShadedLabel;
import org.embl.ebi.escience.scuflui.XMLTree;
import org.embl.ebi.escience.scuflui.graph.GraphColours;
import org.embl.ebi.escience.scuflui.processoractions.AbstractProcessorAction;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;

/**
 * Shows the soaplab metadata, initially in plain XML form but we'll add a
 * stylesheet transform for it later.
 * 
 * @author Tom Oinn
 */
public class SoaplabDescriberPanel extends AbstractProcessorAction {

	static Color col, col2;

	static {
		col = Color.WHITE;
		col2 = Color.WHITE;
	}

	class ColJEditorPane extends JEditorPane {
		public ColJEditorPane(String type, String text) {
			super(type, text);
			setOpaque(false);
			setEditable(false);
			setPreferredSize(new Dimension(0, 0));
			// Add a listener for hyperlinks in the metadata
			addHyperlinkListener(new HyperlinkListener() {
				public void hyperlinkUpdate(HyperlinkEvent r) {
					try {
						if (r.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
							ColJEditorPane.this.setPage(r.getURL());
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});
		}

		protected void paintComponent(Graphics g) {
			final int width = getWidth();
			final int height = getHeight();
			Graphics2D g2d = (Graphics2D) g;
			Paint oldPaint = g2d.getPaint();
			g2d.setPaint(new GradientPaint(0, 0, col, width, 0, col2));
			g2d.fillRect(0, 0, width, height);
			g2d.setPaint(oldPaint);
			super.paintComponent(g);
		}
		/**
		 * public boolean getScrollableTracksViewportWidth() { return true; }
		 * public boolean getScrollableTracksViewportHeight() { return false; }
		 */
	}

	class ColXMLTree extends XMLTree {
		public ColXMLTree(String text) throws java.io.IOException,
				org.jdom.JDOMException {
			super(text);
			setOpaque(false);
		}

		protected void paintComponent(Graphics g) {
			final int width = getWidth();
			final int height = getHeight();
			Graphics2D g2d = (Graphics2D) g;
			Paint oldPaint = g2d.getPaint();
			g2d.setPaint(new GradientPaint(0, 0, col, width, 0, col2));
			g2d.fillRect(0, 0, width, height);
			g2d.setPaint(oldPaint);
			super.paintComponent(g);
		}
	}

	public JComponent getComponent(Processor processor) {
		col = GraphColours.getColour(ProcessorHelper
				.getPreferredColour(processor), Color.WHITE);
		col2 = ShadedLabel.halfShade(col);
		if (processor.isOffline()) {
			// Can't fetch metadata when we're in offline mode
			ColJEditorPane message = new ColJEditorPane(
					"text/html",
					"<html><head>"
							+ WorkflowSummaryAsHTML.STYLE_NOBG
							+ "</head><body><font color=\"red\">Offline mode</font><p>Taverna is currently working in offline mode, metadata must be fetched from the Soaplab server and is therefore unavailable in this mode.</body></html>");
			return message;
		}
		try {
			SoaplabProcessor sp = (SoaplabProcessor) processor;
			URL soaplabEndpoint = sp.getEndpoint();
			Call call = (Call) new Service().createCall();
			call.setTimeout(new Integer(0));
			call.setTargetEndpointAddress(soaplabEndpoint);
			call.setOperationName(new QName("describe"));
			String metadata = (String) call.invoke(new Object[0]);

			// Old impl, returns a tree of the XML
			// ColXMLTree tree = new ColXMLTree(metadata);
			URL sheetURL = SoaplabDescriberPanel.class
					.getResource("analysis_metadata_2_html.xsl");
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Templates stylesheet = transformerFactory
					.newTemplates(new StreamSource(sheetURL.openStream()));
			Transformer transformer = stylesheet.newTransformer();
			StreamSource inputStream = new StreamSource(
					new ByteArrayInputStream(metadata.getBytes()));
			ByteArrayOutputStream transformedStream = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(transformedStream);
			transformer.transform(inputStream, result);
			transformedStream.flush();
			transformedStream.close();
			String summaryText = "<html><head>"
					+ WorkflowSummaryAsHTML.STYLE_NOBG + "</head>"
					+ transformedStream.toString() + "</html>";
			JEditorPane metadataPane = new ColJEditorPane("text/html",
					summaryText);
			metadataPane.setText(transformedStream.toString());
			// System.out.println(transformedStream.toString());
			JScrollPane jsp = new JScrollPane(metadataPane,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			jsp.setPreferredSize(new Dimension(0, 0));
			jsp.getVerticalScrollBar().setValue(0);
			return jsp;
			// return tree;
		} catch (Exception ex) {
			JEditorPane error = new ColJEditorPane(
					"text/html",
					"<html><head>"
							+ WorkflowSummaryAsHTML.STYLE_NOBG
							+ "</head><body><font color=\"red\">Error</font><p>An exception occured while trying to fetch Soaplab metadata from the server. The error was :<pre>"
							+ ex.getMessage() + "</pre></body></html>");
			ex.printStackTrace();
			return error;
		}
	}

	public boolean canHandle(Processor processor) {
		return (processor instanceof SoaplabProcessor);
	}

	public String getDescription() {
		return "Show soaplab metadata";
	}

	public ImageIcon getIcon() {
		return ProcessorHelper.getIconForTagName("soaplabwsdl");
	}

	public Dimension getFrameSize() {
		return new Dimension(450, 450);
	}

}
