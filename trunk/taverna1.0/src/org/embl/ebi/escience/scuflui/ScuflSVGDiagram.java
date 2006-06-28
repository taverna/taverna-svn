/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.JSVGScrollPane;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.batik.swing.svg.GVTTreeBuilderAdapter;
import org.apache.batik.swing.svg.GVTTreeBuilderEvent;
import org.apache.batik.swing.svg.SVGDocumentLoaderAdapter;
import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.scufl.ScuflModelEventListener;
import org.embl.ebi.escience.scufl.view.DotView;
import org.w3c.dom.svg.SVGDocument;

/**
 * An SVG based version of the ScuflDiagram
 * 
 * @author Tom Oinn
 */
public class ScuflSVGDiagram extends JComponent implements
		ScuflModelEventListener, ScuflUIComponent {

	private static Logger logger = Logger.getLogger(ScuflSVGDiagram.class);

	private ScuflModel model;

	private DotView dot;

	boolean graphicValid = false;

	JSVGCanvas svgCanvas;

	JSVGScrollPane pane;

	Timer updateTimer = null;

	public String getDot() {
		return this.dot.getDot();
	}

	public DotView getDotView() {
		return this.dot;
	}

	public ImageIcon getIcon() {
		return TavernaIcons.windowDiagram;
	}

	public ScuflSVGDiagram() {
		super();
		setBackground(Color.white);
		setOpaque(false);
		setLayout(new BorderLayout());
		svgCanvas = new JSVGCanvas();
		pane = new JSVGScrollPane(svgCanvas);
		pane.setPreferredSize(new Dimension(0, 0));
		add(pane, BorderLayout.CENTER);
		JPanel statusPanel = new JPanel();
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.LINE_AXIS));
		final JLabel statusLabel = new JLabel("No document");
		statusPanel.add(Box.createRigidArea(new Dimension(5, 5)));
		statusPanel.add(statusLabel);
		statusPanel.add(Box.createHorizontalGlue());
		statusPanel.setMaximumSize(new Dimension(6000, 30));
		add(statusPanel, BorderLayout.SOUTH);
		svgCanvas.addSVGDocumentLoaderListener(new SVGDocumentLoaderAdapter() {
			public void documentLoadingStarted(SVGDocumentLoaderEvent e) {
				statusLabel.setText("Loading document...");
			}

			public void documentLoadingCompleted(SVGDocumentLoaderEvent e) {
				statusLabel.setText("Document loaded.");
			}
		});
		svgCanvas.addGVTTreeBuilderListener(new GVTTreeBuilderAdapter() {
			public void gvtBuildStarted(GVTTreeBuilderEvent e) {
				statusLabel.setText("Build started...");
			}

			public void gvtBuildCompleted(GVTTreeBuilderEvent e) {
				statusLabel.setText("Build done.");
			}
		});
		svgCanvas.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {
			public void gvtRenderingPrepare(GVTTreeRendererEvent e) {
				statusLabel.setText("Rendering started...");
			}

			public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
				statusLabel.setText("Rendering done.");
			}
		});
	}

	public void attachToModel(ScuflModel model) {
		if (this.model == null) {
			this.dot = new DotView(model);
			this.dot.setPortDisplay(DotView.BOUND);
			model.addListener(this);
			graphicValid = false;
			updateTimer = new Timer();
			updateTimer.schedule(new UpdateTimer(), (long) 0, (long) 2000);
		}
	}

	public void detachFromModel() {
		if (this.model != null) {
			model.removeListener(this);
			model.removeListener(dot);
			this.dot = null;
			this.model = null;
			repaint();
			updateTimer.cancel();
		}
	}

	class UpdateTimer extends TimerTask {
		public UpdateTimer() {
			super();
		}

		public void run() {
			if (!graphicValid) {
				graphicValid = true;
				updateGraphic();
			}
		}
	}

	public String getName() {
		return "Scufl SVG Diagram";
	}

	public void receiveModelEvent(ScuflModelEvent event) {
		graphicValid = false;
	}

	synchronized void updateGraphic() {
		updateTimer.cancel();
		try {
			svgCanvas.setSVGDocument(getSVG(getDot()));
			pane.revalidate();
		} catch (IOException ioe) {
			JOptionPane.showMessageDialog(ScuflSVGDiagram.this, ioe
					.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
		} catch (Exception other) {
			logger.error("Could not update graphics", other);
		}
		updateTimer = new Timer();
		updateTimer.schedule(new UpdateTimer(), 0, 2000);
	}

	static SAXSVGDocumentFactory docFactory = null;

	static {
		String parser = XMLResourceDescriptor.getXMLParserClassName();
		docFactory = new SAXSVGDocumentFactory(parser);
	}

	public static SVGDocument getSVG(String dotText) throws IOException {
		String dotLocation = System.getProperty("taverna.dotlocation");
		if (dotLocation == null) {
			dotLocation = "dot";
		}
		logger.debug("Invoking dot...");
		Process dotProcess = Runtime.getRuntime().exec(
				new String[] { dotLocation, "-Tsvg" });
		StreamDevourer devourer = new StreamDevourer(dotProcess
				.getInputStream());
		devourer.start();
		// Must create an error devourer otherwise stderr fills up and the
		// process stalls!
		StreamDevourer errorDevourer = new StreamDevourer(dotProcess
				.getErrorStream());
		errorDevourer.start();
		PrintWriter out = new PrintWriter(dotProcess.getOutputStream(), true);
		out.print(dotText);
		out.flush();
		out.close();
		return docFactory.createSVGDocument(
				"http://taverna.sf.net/diagram/generated.svg",
				new StringReader(devourer.blockOnOutput()));
	}

}
