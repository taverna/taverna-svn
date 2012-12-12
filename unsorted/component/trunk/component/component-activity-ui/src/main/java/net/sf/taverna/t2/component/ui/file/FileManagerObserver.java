package net.sf.taverna.t2.component.ui.file;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.Border;

import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceDesc;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.workbench.StartupSPI;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.workbench.models.graph.svg.SVGGraphController;
import net.sf.taverna.t2.workbench.views.graph.GraphViewComponent;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.batik.swing.JSVGCanvas;

public class FileManagerObserver implements StartupSPI {

	private FileManager fileManager = FileManager.getInstance();

	@Override
	public boolean startup() {
		fileManager.addObserver(new Observer<FileManagerEvent>() {
			@Override
			public void notify(Observable<FileManagerEvent> observable, FileManagerEvent event) throws Exception {
				Dataflow currentDataflow = fileManager.getCurrentDataflow();
				if (currentDataflow != null) {
					SVGGraphController graphController = GraphViewComponent.graphControllerMap.get(currentDataflow);
					if (graphController != null) {
						JSVGCanvas svgCanvas = graphController.getSVGCanvas();
						Object dataflowSource = fileManager.getDataflowSource(currentDataflow);
						if (dataflowSource instanceof ComponentServiceDesc) {
							svgCanvas.setBorder(new ComponentBorder((ComponentServiceDesc) dataflowSource));
							svgCanvas.repaint();
						} else {
							svgCanvas.setBorder(null);
							svgCanvas.repaint();
						}
					}
				}
			}
		});
		return true;
	}

	@Override
	public int positionHint() {
		return 200;
	}

	class ComponentBorder implements Border {

		private final Color COLOR = new Color(163, 66, 51);
		private final Insets insets = new Insets(25, 0, 0, 0);
		private final String text;

		public ComponentBorder(ComponentServiceDesc componentServiceDesc) {
			text = "Component : " + componentServiceDesc.getName();
		}

		@Override
		public Insets getBorderInsets(java.awt.Component c) {
			return insets;
		}

		@Override
		public boolean isBorderOpaque() {
			return true;
		}

		@Override
		public void paintBorder(java.awt.Component c, Graphics g, int x, int y, int width, int height) {
			g.setColor(COLOR);
			g.fillRect(x, y, width, 20);
			g.setFont(g.getFont().deriveFont(Font.BOLD));
			g.setColor(Color.WHITE);
			g.drawString(text, x + 5, y + 15);
		}
	}

}
