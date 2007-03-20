package uk.org.mygrid.dataproxy.web.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dom4j.DocumentException;
import org.jaxen.JaxenException;

import uk.org.mygrid.dataproxy.configuration.ProxyConfig;
import uk.org.mygrid.dataproxy.configuration.ProxyConfigFactory;
import uk.org.mygrid.dataproxy.configuration.WSDLConfig;
import uk.org.mygrid.dataproxy.wsdl.WSDLProxy;
import uk.org.mygrid.dataproxy.wsdl.impl.WSDLProxyImpl;

/**
 * Servlet implementation class for Servlet: ViewWSDLServlet
 *
 */
 @SuppressWarnings("serial")
public class ViewWSDLServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
    private static Logger logger = Logger.getLogger(ViewWSDLServlet.class);
    
	public ViewWSDLServlet() {
		super();
	}   	
		
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request,response);
	}  	
		
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String wsdlID=request.getParameter("id");
		if (wsdlID!=null) {
			ProxyConfig config = ProxyConfigFactory.getInstance();
			WSDLConfig wsdlConfig = config.getWSDLConfigForID(wsdlID);
			if (wsdlConfig!=null) {
				wsdlConfig.getAddress();
				response.setContentType("text/xml");
				
				WSDLProxy proxy = getWSDLProxy(wsdlConfig);				
				
				BufferedReader reader;
				try {
					reader = new BufferedReader(new InputStreamReader(proxy.getStream()));
				} catch (Exception e) {
					logger.error("Error proxying wsdl",e);
					throw new ServletException(e);
				}
				String line=null;
				while ((line = reader.readLine())!=null) {
					response.getWriter().write(line+"\r\n");
				}
			}
			else {
				response.getWriter().write("No wsdl stored for ID="+wsdlID);
			}
		}
		else {
			response.getWriter().write("No id provided");
		}
	}   
	
	private WSDLProxy getWSDLProxy(WSDLConfig config) {
		return new WSDLProxyImpl(config);
	}
}