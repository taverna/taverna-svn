package uk.ac.mrc.hgmp.taverna.retsina;

import com.jgraph.graph.Port;




public class ScuflInputPort extends ScuflPort {

//  private static int fontSize = 12;
//  private static Font font = new Font("Monospaced",
//                    Font.PLAIN, fontSize);
    private org.embl.ebi.escience.scufl.Port scuflPort;

    public ScuflInputPort() {
	super();
    }

    public ScuflInputPort(Object userObject) {
        super(userObject);
    }
    
    public ScuflInputPort(Object userObject, Port anchor) {
        super(userObject, anchor);
    }

    public ScuflInputPort(Object userObject, org.embl.ebi.escience.scufl.Port scuflPort){
        super(userObject,scuflPort);
        this.scuflPort = scuflPort;
    }

    public org.embl.ebi.escience.scufl.Port getScuflPort()
    {
        return scuflPort;
    }

//  public int getWidth()
//  {
//    String name = (String)getUserObject();
//    JLabel c = new JLabel();
//    FontMetrics fm = c.getFontMetrics(font);
//    int width = fm.stringWidth(name);
//    return width+ScuflInputPortView.inputPortIcon.getIconWidth();
//  }

}
