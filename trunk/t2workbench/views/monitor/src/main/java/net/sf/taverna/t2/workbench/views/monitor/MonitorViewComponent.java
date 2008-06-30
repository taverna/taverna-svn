package net.sf.taverna.t2.workbench.views.monitor;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.monitor.MonitorManager.MonitorMessage;
import net.sf.taverna.t2.workbench.models.graph.GraphElement;
import net.sf.taverna.t2.workbench.models.graph.GraphEventManager;
import net.sf.taverna.t2.workbench.models.graph.svg.SVGGraphController;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;

public class MonitorViewComponent extends JPanel implements UIComponentSPI {

	private static final long serialVersionUID = 1L;

	private SVGGraphController graphController;
		
	private JSVGCanvas svgCanvas;
	
	public MonitorViewComponent() {
		super(new BorderLayout());
		
		svgCanvas = new JSVGCanvas();
		svgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);

		svgCanvas.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {
			public void gvtRenderingCompleted(GVTTreeRendererEvent arg0) {
				graphController.setUpdateManager(svgCanvas.getUpdateManager());
			}
		});
		add(svgCanvas, BorderLayout.CENTER);
	}
	
	public Observer<MonitorMessage> setDataflow(Dataflow dataflow) {
		graphController = new SVGGraphController(dataflow, new MonitorGraphEventManager(), this);	
		svgCanvas.setDocument(graphController.generateSVGDocument());
//		revalidate();
		return new GraphMonitor(graphController);
	}
		
	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return "Monitor View Component";
	}

	public void onDisplay() {
		// TODO Auto-generated method stub

	}

	public void onDispose() {
		// TODO Auto-generated method stub

	}

}

class MonitorGraphEventManager implements GraphEventManager {

	public void mouseClicked(GraphElement graphElement, short button,
			boolean altKey, boolean ctrlKey, boolean metaKey, int x, int y,
			int screenX, int screenY) {
		// TODO Auto-generated method stub
		
	}

	public void mouseDown(GraphElement graphElement, short button,
			boolean altKey, boolean ctrlKey, boolean metaKey, int x, int y,
			int screenX, int screenY) {
		// TODO Auto-generated method stub
		
	}

	public void mouseMoved(GraphElement graphElement, short button,
			boolean altKey, boolean ctrlKey, boolean metaKey, int x, int y,
			int screenX, int screenY) {
		// TODO Auto-generated method stub
		
	}

	public void mouseUp(GraphElement graphElement, short button,
			boolean altKey, boolean ctrlKey, boolean metaKey, int x, int y,
			int screenX, int screenY) {
		// TODO Auto-generated method stub
		
	}
	
}
