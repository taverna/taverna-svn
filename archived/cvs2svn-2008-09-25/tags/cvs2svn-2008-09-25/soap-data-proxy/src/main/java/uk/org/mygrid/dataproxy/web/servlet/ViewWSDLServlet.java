package uk.org.mygrid.dataproxy.web.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import uk.org.mygrid.dataproxy.configuration.ProxyConfig;
import uk.org.mygrid.dataproxy.configuration.ProxyConfigFactory;
import uk.org.mygrid.dataproxy.configuration.WSDLConfig;
import uk.org.mygrid.dataproxy.wsdl.WSDLProxy;
import uk.org.mygrid.dataproxy.wsdl.impl.WSDLProxyImpl;

/**
 * A servlet that rewrites the original wsdl. Any imported schema declarations are replaced
 * to use the ViewSchemaWSDL. The endpoints are replaced to point the the ProxyServlet
 * 
 * @see uk.org.mygrid.dataproxy.web.servlet.ViewSchemaServlet
 * @see uk.org.mygrid.dataproxy.web.servlet.ProxyServlet
 *
 */
 @SuppressWarnings("serial")
public class ViewWSDLServlet extends ProxyBaseServlet {
    private static Logger logger = Logger.getLogger(ViewWSDLServlet.class);
    	
		
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request,response);
	}  	
		
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		setContextOnServerInfo();
		
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