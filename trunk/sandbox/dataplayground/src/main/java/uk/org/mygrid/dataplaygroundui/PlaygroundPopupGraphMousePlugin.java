package uk.org.mygrid.dataplaygroundui;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;

import uk.org.mygrid.dataplayground.PlaygroundDataObject;
import uk.org.mygrid.dataplayground.PlaygroundDataThing;
import uk.org.mygrid.dataplayground.PlaygroundObject;
import uk.org.mygrid.dataplayground.PlaygroundObjectModel;
import uk.org.mygrid.dataplayground.PlaygroundPortObject;
import uk.org.mygrid.dataplayground.PlaygroundProcessorObject;

import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.impl.SparseVertex;
import edu.uci.ics.jung.visualization.Layout;
import edu.uci.ics.jung.visualization.PickSupport;
import edu.uci.ics.jung.visualization.PickedState;
import edu.uci.ics.jung.visualization.PluggableRenderer;
import edu.uci.ics.jung.visualization.SettableVertexLocationFunction;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;

public class PlaygroundPopupGraphMousePlugin extends
		AbstractPopupGraphMousePlugin {

	SettableVertexLocationFunction vl;
	PlaygroundRendererPanel prp;
	PlaygroundPanel pp;
	private PlaygroundMobyPanel playgroundMobyPanel;

	public PlaygroundPopupGraphMousePlugin(SettableVertexLocationFunction vl,PlaygroundPanel pp) {
		this.vl = vl;
		prp = (PlaygroundRendererPanel) PlaygroundRendererPanel.getInstance();
		playgroundMobyPanel = (PlaygroundMobyPanel) PlaygroundMobyPanel.getInstance();
		this.pp = pp;
	}

	protected void handlePopup(MouseEvent e) {

		final VisualizationViewer vv = (VisualizationViewer) e.getSource();
		
		final Point2D point = e.getPoint();
		final Point2D inversePoint = vv.inverseViewTransform(e.getPoint());
		
		Layout layout = vv.getGraphLayout();
		
		final Graph graph = layout.getGraph();
		PickSupport ps = vv.getPickSupport();

		if (ps != null) {

			final Vertex vertex = ps.getVertex(inversePoint.getX(), inversePoint.getY());
			final Edge edge = ps.getEdge(inversePoint.getX(), inversePoint.getY());
			final PickedState pickedState = vv.getPickedState();
			JPopupMenu popupMenu = new JPopupMenu();
			final Set pickedVerticies = pickedState.getPickedVertices();
			
			
			if (vertex != null) {
				
				if(vertex instanceof PlaygroundProcessorObject){
					
					popupMenu.add(new AbstractAction("Run") {
	                    public void actionPerformed(ActionEvent e) {
	                          
	                    		Thread t = new Thread(){

									
									public void run() {
										((PlaygroundProcessorObject)vertex).setRunning(true);
										ArrayList<PlaygroundObject> result =	PlaygroundObjectModel.run((PlaygroundProcessorObject)vertex,pp.isRecording());
				                        pp.addResults(result,point);
				                        ((PlaygroundProcessorObject)vertex).setRunning(false);	
									}
	                    	  
	                    		};
	                    		
	                    		t.start();
	                    	
	                    }});
					
					popupMenu.add(new AbstractAction("Get Processor Details") {
	                    
						public void actionPerformed(ActionEvent e) {
	                    
							pickedState.pick(vertex, false);
	                    	
	                    	 Thread t = new Thread(){

									
									public void run() {
	                    	 playgroundMobyPanel.Set(((PlaygroundProcessorObject)vertex).getProcessor());
									}
	                    	 };
	                    	 
	                    	 t.start();
	                    }});
					
				}
				if(vertex instanceof PlaygroundDataObject){
				popupMenu.add(new AbstractAction("Collapse") {
                    public void actionPerformed(ActionEvent e) {
                        pickedState.pick(vertex, false);
                        ((PlaygroundDataObject)vertex).collapse();
                    }});
				popupMenu.add(new AbstractAction("Get Data Object Details") {
                    public void actionPerformed(ActionEvent e) {
                    	
                    	
                    	pickedState.pick(vertex, false);
                    
                    	Thread t = new Thread(){

							
							public void run() {                    	
                    	playgroundMobyPanel.Set(((PlaygroundDataObject)vertex).getProcessor());
							}
                    	};
                    	
                    	t.run();
                    }});
				popupMenu.add(new AbstractAction("Expose id & namespace") {
                    public void actionPerformed(ActionEvent e) {
                    	 pickedState.pick(vertex, false);
                    	 Layout layout = vv.getGraphLayout();
                    	 for (Iterator i = graph.getVertices().iterator(); i
							.hasNext();) {
						layout.lockVertex((Vertex) i.next());
					}
                    	 
                    	ArrayList ports = new ArrayList(((PlaygroundDataObject)vertex).getInputPortObjects().values());
                    	Iterator iterator = ports.iterator();
                   	
                    	while(iterator.hasNext() ) {
                   		
                   		PlaygroundPortObject ppo = (PlaygroundPortObject)iterator.next();
                   		if(ppo.isInvisible() && (ppo.getName().equalsIgnoreCase("id") || ppo.getName().equalsIgnoreCase("namespace") )){
                   		pp.getPlaygroundModel().addPort((PlaygroundDataObject)vertex,ppo);
                   		System.out.println("Component  " + ppo);
                   		Point2D point = vl.getLocation((PlaygroundDataObject)vertex);
                   		vl.setLocation(ppo,new Point2D.Double(point.getX() + ((int)( Math.random() * 40) - 20)   ,point.getY() + ((int)( Math.random() * 40) - 20)));
                   		
                   		}	
                   		}
                    	vv.getModel().restart();
                   		
                   		for (Iterator i = graph.getVertices().iterator(); i
						.hasNext();) {
					layout.unlockVertex((Vertex) i.next());
				}
				vv.repaint();
                    	
                    	
                    }});
				
				
				
				
				
				}
				popupMenu.add(new AbstractAction("Delete") {
	                    public void actionPerformed(ActionEvent e) {
	                    
	                    for(Iterator i = pickedVerticies.iterator(); i.hasNext();){	
	                    	Vertex v = (Vertex)i.next();
	                    	pickedState.pick(v, false);
	                        graph.removeVertex(v);
	                        vv.repaint();
	                        if(v instanceof PlaygroundDataThing){
	                        	
	                        	prp.remove((PlaygroundDataThing)v);
	                        	
	                        }
	                    }
	                    }});
				
				 
	            } else if(edge != null) {
	                popupMenu.add(new AbstractAction("Delete") {
	                    public void actionPerformed(ActionEvent e) {
	                       
	                    	pickedState.pick(edge, false);
	                        graph.removeEdge(edge);
	                        vv.repaint();
	                    
	                    
	                    }});
				
			
	            
	            
	            }//else there isn't a vertex or edge selected.
			else {
				popupMenu.add(new AbstractAction("Create Data Item") {
					public void actionPerformed(ActionEvent e) {
						Vertex d = new PlaygroundDataThing();
						vl.setLocation(d, vv.inverseViewTransform(inversePoint));
						Layout layout = vv.getGraphLayout();
						for (Iterator i = graph.getVertices().iterator(); i
								.hasNext();) {
							layout.lockVertex((Vertex) i.next());
						}
						
						graph.addVertex(d);
						prp.add((PlaygroundDataThing)d);
						vv.getModel().restart();
						
						for (Iterator i = graph.getVertices().iterator(); i
								.hasNext();) {
							layout.unlockVertex((Vertex) i.next());
						}
						vv.repaint();
					}
				});

			}//end else

			
			if(popupMenu.getComponentCount() > 0) {
                popupMenu.show(vv, e.getX(), e.getY());
            }
			
		}
	}

}
