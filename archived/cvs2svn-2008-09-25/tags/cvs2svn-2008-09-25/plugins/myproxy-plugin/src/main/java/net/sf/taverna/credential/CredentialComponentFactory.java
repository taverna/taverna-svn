package net.sf.taverna.credential;

import javax.swing.ImageIcon;

import org.embl.ebi.escience.scuflui.spi.UIComponentFactorySPI;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

public class CredentialComponentFactory implements UIComponentFactorySPI {

	public UIComponentSPI getComponent() {
		// TODO Auto-generated method stub
		return new CredentialComponent();
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return "Credential Component";
	}

}
