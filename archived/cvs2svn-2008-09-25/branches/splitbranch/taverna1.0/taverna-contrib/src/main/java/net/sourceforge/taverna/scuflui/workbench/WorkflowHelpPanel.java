/**
 * 
 */
package net.sourceforge.taverna.scuflui.workbench;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.text.html.HTMLEditorKit;

import net.sourceforge.taverna.util.XmlUtil;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.scufl.ScuflModelEventListener;
import org.embl.ebi.escience.scufl.view.XScuflView;
import org.embl.ebi.escience.scuflui.ScuflUIComponent;

/**
 * @author Mark
 * 
 */
public class WorkflowHelpPanel extends JPanel implements ScuflUIComponent {
	
	private static Logger logger = Logger.getLogger(WorkflowHelpPanel.class);
	JEditorPane htmlPane = new JEditorPane();

	ScuflModel model = null;

	ScuflModelEventListener listener = null;

	ImageIcon imageIcon = null;

	public WorkflowHelpPanel() {
		this.setLayout(new BorderLayout());
		this.add(htmlPane, BorderLayout.CENTER);
		imageIcon = new ImageIcon(WorkflowHelpPanel.class.getResource("/etc/icons/gnome-mime-manpage.png"));

		htmlPane.setContentType("text/html");
		htmlPane.setEditorKit(new HTMLEditorKit());
		this.setSize(100, 200);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflui.ScuflUIComponent#attachToModel(org.embl.ebi.escience.scufl.ScuflModel)
	 */
	public void attachToModel(ScuflModel model) {
		this.model = model;
		final XScuflView xsv = new XScuflView(model);
		this.listener = new ScuflModelEventListener() {
			public void receiveModelEvent(ScuflModelEvent event) {
				String html = XmlUtil.transform("/etc/wkflow2html.xslt", xsv.getXMLText());
				htmlPane.setText(html);
				logger.info("model: " + html);
			}
		};
		this.model.addListener(listener);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflui.ScuflUIComponent#detachFromModel()
	 */
	public void detachFromModel() {
		this.model.removeListener(this.listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflui.ScuflUIComponent#getIcon()
	 */
	public ImageIcon getIcon() {
		return imageIcon;
	}

	public void convert2Html(ScuflModel model) {
		XScuflView xsv = new XScuflView(model);
		String html = XmlUtil.transform("/etc/xslt/wkflow2html.xslt", xsv.getXMLText());
		htmlPane.setEditorKit(new HTMLEditorKit());
		htmlPane.setText(html);

		logger.info("model: " + html);
	}

}
