package net.sf.taverna.t2.component.ui.file;

import static javax.swing.SwingUtilities.invokeLater;
import static javax.swing.SwingUtilities.isEventDispatchThread;
import static net.sf.taverna.t2.component.ui.util.Utils.currentDataflowIsComponent;
import static net.sf.taverna.t2.workbench.views.graph.GraphViewComponent.graphControllerMap;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.Border;

import net.sf.taverna.t2.component.registry.ComponentVersionIdentification;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.workbench.StartupSPI;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.workbench.models.graph.svg.SVGGraphController;
import net.sf.taverna.t2.workbench.ui.impl.configuration.colour.ColourManager;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.batik.swing.JSVGCanvas;

public class FileManagerObserver implements StartupSPI {

	private static final Color COLOR = new Color(230, 147, 210);

	private static FileManager fileManager = FileManager.getInstance();

	@Override
	public boolean startup() {
		ColourManager.getInstance().setPreferredColour(
				"net.sf.taverna.t2.component.registry.Component", COLOR);
		ColourManager.getInstance().setPreferredColour(
				"net.sf.taverna.t2.component.ComponentActivity", COLOR);
		fileManager.addObserver(new Observer<FileManagerEvent>() {
			@Override
			public void notify(Observable<FileManagerEvent> observable,
					FileManagerEvent event) throws Exception {
				FileManagerObserverRunnable runnable = new FileManagerObserverRunnable(
						event);
				if (isEventDispatchThread()) {
					runnable.run();
				} else {
					invokeLater(runnable);
				}
			}
		});
		return true;
	}

	@Override
	public int positionHint() {
		return 200;
	}

	public class FileManagerObserverRunnable implements Runnable {
		public FileManagerObserverRunnable(FileManagerEvent message) {
		}

		public void run() {
			Dataflow currentDataflow = fileManager.getCurrentDataflow();
			if (currentDataflow == null)
				return;
			SVGGraphController graphController = graphControllerMap
					.get(currentDataflow);
			if (graphController == null)
				return;
			JSVGCanvas svgCanvas = graphController.getSVGCanvas();
			Object dataflowSource = fileManager
					.getDataflowSource(currentDataflow);
			if (currentDataflowIsComponent()) {
				svgCanvas.setBorder(new ComponentBorder(
						(ComponentVersionIdentification) dataflowSource));
				svgCanvas.repaint();
			} else {
				svgCanvas.setBorder(null);
				svgCanvas.repaint();
			}
		}
	}

	class ComponentBorder implements Border {

		private final Insets insets = new Insets(25, 0, 0, 0);
		private final String text;

		public ComponentBorder(ComponentVersionIdentification identification) {
			text = "Component : " + identification.getComponentName();
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
		public void paintBorder(java.awt.Component c, Graphics g, int x, int y,
				int width, int height) {
			g.setColor(COLOR);
			g.fillRect(x, y, width, 20);
			g.setFont(g.getFont().deriveFont(Font.BOLD));
			g.setColor(Color.WHITE);
			g.drawString(text, x + 5, y + 15);
		}
	}

}
