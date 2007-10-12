package uk.org.mygrid.dataplaygroundui;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.embl.ebi.escience.scuflui.ResultItemPanel;
import org.embl.ebi.escience.scuflui.shared.ShadedLabel;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

import uk.org.mygrid.dataplayground.PlaygroundDataThing;

public class PlaygroundRendererPanel extends JPanel implements UIComponentSPI {

	private static PlaygroundRendererPanel instance;
	private HashMap<PlaygroundDataThing,ResultItemPanel> playgroundDataResultPanels;
	private JTabbedPane tabbedPane;

	public PlaygroundRendererPanel() {
		super();
		playgroundDataResultPanels = new HashMap<PlaygroundDataThing,ResultItemPanel>();
		tabbedPane = new JTabbedPane();
		ShadedLabel header = new ShadedLabel("Data Viewer", ShadedLabel.TAVERNA_BLUE);
		this.setLayout(new BorderLayout());
		this.add(header,BorderLayout.PAGE_START);
		this.add(tabbedPane,BorderLayout.CENTER);
	}

	public void add(PlaygroundDataThing d){
		
		ResultItemPanel r = new ResultItemPanel(d.getDataThing());
		if(!playgroundDataResultPanels.containsKey(d)){
		playgroundDataResultPanels.put(d,r);
		tabbedPane.add(d.getName(),r);
		
		}
	}
	
	public void remove(PlaygroundDataThing d){
		
		ResultItemPanel r = playgroundDataResultPanels.get(d);
		
		if(r != null){		
			tabbedPane.remove(r);
		playgroundDataResultPanels.remove(d);
		}
	}
	
	public void replace(PlaygroundDataThing d){
				
		if(playgroundDataResultPanels.containsKey(d)){
			
			int index = tabbedPane.indexOfComponent(playgroundDataResultPanels.get(d));
			remove(d);
			
			ResultItemPanel r = new ResultItemPanel(d.getDataThing());
			
			if(!playgroundDataResultPanels.containsKey(d)){
			playgroundDataResultPanels.put(d,r);
			tabbedPane.add(r,index);
		     }
		}
		
	}
	
	public void select(PlaygroundDataThing d){
		if(playgroundDataResultPanels.containsKey(d)){
			tabbedPane.setSelectedComponent(playgroundDataResultPanels.get(d));
		}
	}
	
	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public void onDisplay() {
		// TODO Auto-generated method stub
		
	}

	public void onDispose() {
		// TODO Auto-generated method stub
		
	}
	
	
	
	public static UIComponentSPI getInstance() {
	if (instance == null) {
           instance = new PlaygroundRendererPanel();
		}
	return instance;
    }


}
