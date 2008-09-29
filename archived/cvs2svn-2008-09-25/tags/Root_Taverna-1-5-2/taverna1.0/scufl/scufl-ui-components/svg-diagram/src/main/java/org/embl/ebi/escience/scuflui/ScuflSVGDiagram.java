/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Timer;
import java.util.TimerTask;
import java.util.WeakHashMap;

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
import org.embl.ebi.escience.scufl.view.DotViewSettings;
import org.embl.ebi.escience.scuflui.shared.StreamDevourer;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;
import org.w3c.dom.svg.SVGDocument;

/**
 * An SVG based version of the ScuflDiagram
 * 
 * @author Tom Oinn
 */
@SuppressWarnings("serial")
public class ScuflSVGDiagram extends JComponent implements
		ScuflModelEventListener, WorkflowModelViewSPI {
	
	private WeakHashMap<ScuflModel, DotViewSettings> dotViewSettings = new WeakHashMap<ScuflModel, DotViewSettings>();
	
	private static Logger logger = Logger.getLogger(ScuflSVGDiagram.class);

	static SAXSVGDocumentFactory docFactory = null;
	
	static {
		String parser = XMLResourceDescriptor.getXMLParserClassName();
		logger.info("Using XML parser " + parser);
		docFactory = new SAXSVGDocumentFactory(parser);
	}

	private boolean fitToWindow = true;
	
	private ScuflModel model;

	private DotView dot;

	boolean graphicValid = false;

	JSVGCanvas svgCanvas;

	JSVGScrollPane pane;

	Timer updateTimer = null;

	ComponentListener resizeListener = new ComponentListener() {
		public void componentHidden(ComponentEvent e) {}
		public void componentMoved(ComponentEvent e) {}
		public void componentShown(ComponentEvent e) {}
		public void componentResized(ComponentEvent e) {
			if (isFitToWindow()) {
				graphicValid = false;
			}
		}
	};

	public String getDot() {
		return dot.getDot();
	}

	public DotView getDotView() {
		return dot;
	}

	public ImageIcon getIcon() {
		return TavernaIcons.windowDiagram;
	}

	public ScuflSVGDiagram() {
		this(true, true);
	}
	
	public ScuflSVGDiagram(boolean withStatusBar, boolean withScrollbars) {
		super();
		setBackground(Color.white);
		setOpaque(false);
		setLayout(new BorderLayout());
		addComponentListener(resizeListener);
		svgCanvas = new JSVGCanvas();
		svgCanvas.setOpaque(false);
		pane = new JSVGScrollPane(svgCanvas);
		if (withScrollbars) {
			pane.setPreferredSize(new Dimension(0, 0));
			add(pane, BorderLayout.CENTER);
		} else {
			add(svgCanvas, BorderLayout.CENTER);
		}
		final JLabel statusLabel = new JLabel("No document");
		if (withStatusBar) {
			JPanel statusPanel = new JPanel();
			statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.LINE_AXIS));
			statusPanel.add(Box.createRigidArea(new Dimension(5, 5)));
			statusPanel.add(statusLabel);
			statusPanel.add(Box.createHorizontalGlue());
			statusPanel.setMaximumSize(new Dimension(6000, 30));
			add(statusPanel, BorderLayout.SOUTH);
		}
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

	public synchronized void attachToModel(ScuflModel model) {
		if (this.model != null) {
			logger.warn("Did not detachFromModel() before attachToModel()");
			detachFromModel();
		}
		this.model = model;
		dot = new DotView(model);
		DotViewSettings dotViewSetting = dotViewSettings.get(model);
		if (dotViewSetting != null) { // previous settings
			dot.setViewSettings(dotViewSetting);
		} else { // defaults
			dot.setPortDisplay(DotView.NONE);
			dot.setTypeLabelDisplay(false);
		}
		model.addListener(this);
		graphicValid = false;
		updateTimer = new Timer();
		updateTimer.schedule(new UpdateTimer(), 0, 2000);
	}

	public synchronized void detachFromModel() {
		if (model == null) {
			return;
		}
		// Remember the settings (Boring, expand nested, etc) for each
		// workflow model. As we use weak references, this should be OK
		dotViewSettings.put(model, dot.getViewSettings());
		
		model.removeListener(this);
		model.removeListener(dot);
		dot = null;
		model = null;
		repaint();
		updateTimer.cancel();
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

	public synchronized void updateGraphic() {
		updateTimer.cancel();
		try {
			svgCanvas.setSVGDocument(getSVG(getDot()));
			pane.revalidate();
		} catch (IOException ioe) {
			JOptionPane.showMessageDialog(ScuflSVGDiagram.this, ioe
					.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
		} catch (Exception other) {
			logger.warn("Could not update graphics");
		}
		updateTimer = new Timer();
		updateTimer.schedule(new UpdateTimer(), 0, 1000);
	}

	public static SVGDocument getSVG(String dotText) throws IOException {
		// FIXME: Should use MyGridConfiguration.getProperty(), 
		// but that would not include the system property
		// specified at command line on Windows (runme.bat) 
		// and OS X (Taverna.app)
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
		

		String svgText = devourer.blockOnOutput();
		// Avoid TAV-424, replace buggy SVG outputted by "modern" GraphViz versions.
		// http://www.graphviz.org/bugs/b1075.html
		// Contributed by Marko Ullgren
		svgText = svgText.replaceAll("font-weight:regular","font-weight:normal");

		// Fake URI, just used for internal references like #fish
		return docFactory.createSVGDocument("http://taverna.sf.net/diagram/generated.svg", 
			new StringReader(svgText));
	}

	public void onDisplay() {
	}

	public void onDispose() {
		detachFromModel();		
	}

	/**
	 * If diagram should be rescaled when resizing panel
	 * 
	 * @return true if the diagram should be rescaled
	 */
	public boolean isFitToWindow() {
		return fitToWindow;
	}
	
	/**
	 * Enable automatic rescale on resize of panel.
	 * 
	 * @param fitToWindow true if the diagram should be rescaled
	 */
	public void setFitToWindow(boolean fitToWindow) {
		this.fitToWindow = fitToWindow;
		if (fitToWindow) {
			// Do a redraw
			graphicValid = false;
		}
	}
	
}
