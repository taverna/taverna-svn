package uk.org.mygrid.dataplaygroundui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.biomoby.client.taverna.plugin.BiomobyObjectProcessor;
import org.biomoby.client.taverna.plugin.BiomobyProcessor;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

import uk.org.mygrid.dataplayground.biomoby.PlaygroundBiomobyAction;
import uk.org.mygrid.dataplayground.biomoby.PlaygroundBiomobyObjectAction;
import uk.org.mygrid.dataplayground.biomoby.PlaygroundPopupThread;

public class PlaygroundMobyPanel extends JPanel implements UIComponentSPI {

	private static PlaygroundMobyPanel instance;

	public static UIComponentSPI getInstance() {
		if (instance == null) {
			instance = new PlaygroundMobyPanel();
		}
		return instance;
	}

	public PlaygroundMobyPanel() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return "Moby Panel";
	}

	public void onDisplay() {
		// TODO Auto-generated method stub

	}

	public void onDispose() {
		// TODO Auto-generated method stub

	}

	public void Set(Processor p) {

		if (p instanceof BiomobyObjectProcessor) {
			PlaygroundBiomobyObjectAction action = new PlaygroundBiomobyObjectAction(
					false);
			PlaygroundPopupThread popupthread = new PlaygroundPopupThread(
					(BiomobyObjectProcessor) p, action);
			popupthread.start();
			System.out.println("Thread started");
			while (popupthread.isAlive()) {
				try {
					Thread.sleep(4000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			Component c = popupthread.getComponent();

			this.removeAll();
			this.setSize(new Dimension(450, 450));
			c.setSize(new Dimension(450, 450));
			this.setLayout(new BorderLayout());
			this.add(c, BorderLayout.CENTER);

		} else if (p instanceof BiomobyProcessor) {
			PlaygroundBiomobyAction bma = new PlaygroundBiomobyAction();
			Component c = bma.getComponent(p);
			this.removeAll();
			this.setSize(new Dimension(450, 450));
			c.setSize(new Dimension(450, 450));
			this.setLayout(new BorderLayout());
			this.add(c, BorderLayout.CENTER);
		}

	}

}
