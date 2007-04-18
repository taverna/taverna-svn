package uk.org.mygrid.dataproxy.web.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import uk.org.mygrid.dataproxy.configuration.ProxyConfigFactory;
import uk.org.mygrid.dataproxy.configuration.WSDLConfig;
import uk.org.mygrid.dataproxy.wsdl.SchemaProxy;
import uk.org.mygrid.dataproxy.wsdl.impl.SchemaProxyImpl;

/**
 * Redirects to the original schema, replacing any includes or imports with new urls pointing to use
 * this shema servlet. Currently the schema is not modified, future improvments include modifying the
 * schema itself to reflect elements configured to have the data referenced.
 *
 */
 @SuppressWarnings("serial")
public class ViewSchemaServlet extends ProxyBaseServlet {
	 
    private static Logger logger = Logger.getLogger(ViewSchemaServlet.class);
	 
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}  	
	
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		setContextOnServerInfo();
		
		String wsdlID=request.getParameter("wsdlid");
		String xsd=request.getParameter("xsd");
		
		if (wsdlID==null) {
			response.getWriter().println("No wsdlid defined");
		}
		
		if (xsd==null) {
			response.getWriter().println("No xsd parameter defined for schema file name");
		}
		
		if (wsdlID!=null && xsd!=null) {
			WSDLConfig config = ProxyConfigFactory.getInstance().getWSDLConfigForID(wsdlID);
			if (config==null) {
				response.getWriter().println("No wsdl found for ID="+wsdlID);
			}
			else {
				response.setContentType("text/xml");
				SchemaProxy proxy = getProxy(config, xsd);				
				BufferedReader reader;
				try {
					reader = new BufferedReader(new InputStreamReader(proxy.getStream()));
				} catch (Exception e) {
					logger.error("Error proxying schema "+xsd,e);
					throw new ServletException(e);
				}
				String line=null;
				while ((line = reader.readLine())!=null) {
					response.getWriter().write(line+"\r\n");
				}
			}
		}
	}   	  	 
	
	private SchemaProxy getProxy(WSDLConfig config, String xsd) {
		return new SchemaProxyImpl(config,xsd);
	}
}