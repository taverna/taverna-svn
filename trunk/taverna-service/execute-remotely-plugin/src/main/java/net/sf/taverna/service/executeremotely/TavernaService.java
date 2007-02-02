package net.sf.taverna.service.executeremotely;

import java.net.URL;

import javax.xml.rpc.ServiceException;

import net.sf.taverna.service.wsdl.client.Taverna;
import net.sf.taverna.service.wsdl.client.TavernaServiceLocator;

public class TavernaService {
	
	private static TavernaServiceLocator locator = new TavernaServiceLocator();
	
	public static Taverna connect(URL url) throws ServiceException {
		return locator.getTaverna(url);
	}
}
