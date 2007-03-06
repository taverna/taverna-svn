package uk.org.mygrid.dataproxy.web.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.org.mygrid.dataproxy.configuration.ProxyConfig;
import uk.org.mygrid.dataproxy.configuration.ProxyConfigFactory;
import uk.org.mygrid.dataproxy.configuration.WSDLConfig;

/**
 * Servlet implementation class for Servlet: ViewWSDLServlet
 *
 */
 @SuppressWarnings("serial")
public class ViewWSDLServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
    /* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public ViewWSDLServlet() {
		super();
	}   	
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request,response);
	}  	
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String wsdlID=request.getParameter("id");
		if (wsdlID!=null) {
			ProxyConfig config = ProxyConfigFactory.getInstance();
			WSDLConfig wsdlConfig = config.getWSDLConfigForID(wsdlID);
			if (wsdlConfig!=null) {
				URL filebase = new URL(config.getStoreBaseURL(),wsdlID+"/");
				URL file = new URL(filebase,wsdlConfig.getWSDLFilename());
				BufferedReader reader = new BufferedReader(new InputStreamReader(file.openStream()));
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
}