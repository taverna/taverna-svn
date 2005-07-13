package net.sf.taverna.dalec;

import org.biojava.servlets.dazzle.datasource.DataSourceException;
import org.biojava.bio.seq.Sequence;

import javax.servlet.ServletContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Enumeration;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.MalformedURLException;

import junit.framework.TestCase;

/**
 * Javadocs go here.
 *
 * @author Tony Burdett date: 28-Jun-2005
 */
public class TestDalecAnnotationSource extends TestCase
{
    final String mapMaster = "http://www.ebi.ac.uk/das-srv/uniprot/das/aristotle/";
    final File xsFile = new File("C:\\home\\tony\\documents\\dalec1.0\\workflow.xml");
    final File dbGenLoc = new File("C:\\home\\tony\\documents\\dalec1.0\\outputTest\\");

    public void testSetMapMaster()
    {
        DalecAnnotationSource dalec = new DalecAnnotationSource();

        dalec.setMapMaster(mapMaster);

        assertTrue(mapMaster == dalec.getMapMaster());
    }

    public void testXScuflFileSet()
    {
        DalecAnnotationSource dalec = new DalecAnnotationSource();

        dalec.setXScuflFile(xsFile);
    }

    public void testSequenceDBLocationSet()
    {
        DalecAnnotationSource dalec = new DalecAnnotationSource();

        dalec.setSequenceDBLocation(dbGenLoc);
    }

    public void testInit() throws DataSourceException
    {
        DalecAnnotationSource dalec = new DalecAnnotationSource();
        dalec.setMapMaster(mapMaster);
        dalec.setSequenceDBLocation(dbGenLoc);
        dalec.setXScuflFile(xsFile);

        dalec.init(new ServletContext()
        {

            public Object getAttribute(String s)
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Enumeration getAttributeNames()
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public ServletContext getContext(String s)
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String getInitParameter(String s)
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Enumeration getInitParameterNames()
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public int getMajorVersion()
            {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String getMimeType(String s)
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public int getMinorVersion()
            {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public RequestDispatcher getNamedDispatcher(String s)
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String getRealPath(String s)
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public RequestDispatcher getRequestDispatcher(String s)
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public URL getResource(String s) throws MalformedURLException
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public InputStream getResourceAsStream(String s)
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Set getResourcePaths(String s)
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String getServerInfo()
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Servlet getServlet(String s) throws ServletException
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String getServletContextName()
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Enumeration getServletNames()
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Enumeration getServlets()
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public void log(Exception e, String s)
            {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void log(String s)
            {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void log(String s, Throwable throwable)
            {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void removeAttribute(String s)
            {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void setAttribute(String s, Object o)
            {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
    }

    public void testGetSequence() throws DataSourceException
    {
        String sequenceID = "Q12345";

        DalecAnnotationSource dalec = new DalecAnnotationSource();
        dalec.setMapMaster(mapMaster);
        dalec.setSequenceDBLocation(dbGenLoc);
        dalec.setXScuflFile(xsFile);

        dalec.init(new ServletContext()
        {

            public Object getAttribute(String s)
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Enumeration getAttributeNames()
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public ServletContext getContext(String s)
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String getInitParameter(String s)
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Enumeration getInitParameterNames()
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public int getMajorVersion()
            {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String getMimeType(String s)
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public int getMinorVersion()
            {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public RequestDispatcher getNamedDispatcher(String s)
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String getRealPath(String s)
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public RequestDispatcher getRequestDispatcher(String s)
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public URL getResource(String s) throws MalformedURLException
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public InputStream getResourceAsStream(String s)
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Set getResourcePaths(String s)
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String getServerInfo()
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Servlet getServlet(String s) throws ServletException
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public String getServletContextName()
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Enumeration getServletNames()
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public Enumeration getServlets()
            {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public void log(Exception e, String s)
            {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void log(String s)
            {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void log(String s, Throwable throwable)
            {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void removeAttribute(String s)
            {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void setAttribute(String s, Object o)
            {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        System.out.println(dalec.getSequence(sequenceID));
    }

    public void testXMLReader()
    {

    }
}
