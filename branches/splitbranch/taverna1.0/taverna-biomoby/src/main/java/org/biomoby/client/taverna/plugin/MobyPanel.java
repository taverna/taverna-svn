/*
 * Created on Sep 9, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.biomoby.client.taverna.plugin;

import java.awt.BorderLayout;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.ScuflUIComponent;
/**
 * @author Eddie
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MobyPanel extends JPanel implements ScuflUIComponent{

	private JTextArea textArea = null;
	private String text = "";
	private String name = "";
	private JLabel jLabel = new JLabel();
	/**
	 * This is the default constructor
	 */
	public MobyPanel(String label, String name, String text) {
		super(new BorderLayout());
		jLabel.setText(label);
		this.text = text;
		this.name = name;
		initialize();
	}
	
	public MobyPanel(String label) {
		super(new BorderLayout());
		jLabel.setText(label);
		initialize();
	}
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(450, 450);
		jLabel.setHorizontalAlignment(JLabel.CENTER);
		add(jLabel, BorderLayout.NORTH);
		add(getTextArea(), BorderLayout.CENTER);
	}

	/**
	 * This method initializes jTextArea	
	 * 	
	 * @return javax.swing.JTextArea	
	 */    
	private JTextArea getTextArea() {
		if (textArea == null) {
			textArea = new JTextArea();
		}
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setText(this.text);
		textArea.setEditable(false);
		textArea.setEnabled(true);
		textArea.setAutoscrolls(true);
		textArea.setCaretPosition(0);
		return textArea;
	}
	
	public void setText(String text) {
	    this.text = text;
	    if (textArea == null) {
			textArea = new JTextArea(this.text);
		}
	    textArea.setText(text);
	}

    /* (non-Javadoc)
     * @see org.embl.ebi.escience.scuflui.ScuflUIComponent#attachToModel(org.embl.ebi.escience.scufl.ScuflModel)
     */
    public void attachToModel(ScuflModel model) {
    }

    /* (non-Javadoc)
     * @see org.embl.ebi.escience.scuflui.ScuflUIComponent#detachFromModel()
     */
    public void detachFromModel() {
    }

    /* (non-Javadoc)
     * @see org.embl.ebi.escience.scuflui.ScuflUIComponent#getIcon()
     */
    public ImageIcon getIcon() {
        Class cls = this.getClass();
        URL url = cls.getClassLoader().getResource(
                "org/biomoby/client/taverna/plugin/moby_small.gif");
        return new ImageIcon(url);
    }
    
    public String getName(){
    	if (name==null) return "";
    	else return name;
    }
  }
