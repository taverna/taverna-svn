/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.taverna.t2.portal;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil2;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tool.api.SessionManager;
/**
 * This servlet reads the file path from the session object and
 * sends the file back to the user as a response.
 *
 * @author Alex Nenadic
 */
public class FileServingServlet extends HttpServlet {

    private static final String APPLICATION_OCTETSTREAM = "application/octet-stream";

    // Directory where info for all submitted jobs for all users is persisted
    private File JOBS_DIR;


    @Override
    public void init(){

        // Get the directory where info for submitted jobs for all users is persisted
        JOBS_DIR = new File(getServletContext().getInitParameter(Constants.JOBS_DIRECTORY_PATH));
    }

    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

        // Get the file to fetch
        String dataFilePath = URLDecoder.decode((String) request.getParameter(Constants.DATA_FILE_PATH), "UTF-8");
        // Get the content type of the file to fetch (this is also passed as a parameter)
        String mimeType = URLDecoder.decode((String) request.getParameter(Constants.MIME_TYPE), "UTF-8");

        sendFile(dataFilePath, mimeType, request, response);
    } 

    public static List<MimeType> getMimeTypes(byte[] bytes) {
        List<MimeType> mimeList = new ArrayList<MimeType>();
        MimeUtil2 mimeUtil = new MimeUtil2();
        mimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.ExtensionMimeDetector");
        mimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
        mimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.WindowsRegistryMimeDetector");
        mimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.ExtraMimeTypes");
        try {
            Collection<MimeType> mimeTypes2 = mimeUtil.getMimeTypes(bytes);
            mimeList.addAll(mimeTypes2);

            // Hack for SVG that seems not to be recognised
            String bytesString = new String(bytes, "UTF-8");
            if (bytesString.contains("http://www.w3.org/2000/svg")){
                MimeType svgMimeType = new MimeType("image/svg+xml");
                if (!mimeList.contains(svgMimeType)){
                        mimeList.add(svgMimeType);
                }
            }
            if (mimeList.isEmpty()){ // if it is not recognised
                mimeList.add(new MimeType(APPLICATION_OCTETSTREAM));
            }

        } catch (IOException ex) {
                mimeList.add(new MimeType(APPLICATION_OCTETSTREAM));
        }

        return mimeList;
    }

    public void sendFile(String dataFilePath, String mimeType, HttpServletRequest request, HttpServletResponse response){
           try {

            File dataFile = new File(dataFilePath);
            
            String user0 = (String)request.getSession().getAttribute(Constants.USER); // this does not work in Sakai
            System.out.println("File Serving Servlet: Fetching file " + dataFilePath + " for user (obtained from session) " + user0 + "; file mime type: "+mimeType);
            // Sakai-specific way of getting the current user
            SessionManager sessionManager = (SessionManager) ComponentManager.get(org.sakaiproject.tool.api.SessionManager.class); // Sakai-specific
            String user = sessionManager.getCurrentSession().getUserEid(); // get user's display name - Sakai-specific
            if (user == null){ //if user is null - then make them ANONYMOUS (should not be null now)
                user = Constants.USER_ANONYMOUS;
            } // Still gives me nul1!!!!
            System.out.println("File Serving Servlet: Fetching file " + dataFilePath + " for user " + user + "; file mime type: "+mimeType);

            // We do not serve arbritarty files here - just those in the JOBS_DIR so make sure
            // we check that here. Also check that the file we are serving belongs to the
            // current user.
            //if (dataFile.getCanonicalPath().startsWith(JOBS_DIR.getAbsolutePath() + Constants.FILE_SEPARATOR + user)){
            if (dataFile.getCanonicalPath().startsWith(JOBS_DIR.getAbsolutePath())){
               if (dataFile.exists()) {
                    OutputStream os = response.getOutputStream();

                    byte b[] = new byte[1024];
                    InputStream is = new FileInputStream(dataFile);
                    int numRead = 0;
                    response.setContentType(mimeType);
                    response.setContentLength(is.available());

                    while ((numRead=is.read(b)) > 0) {
                        /*if (mimeType == null){
                            byte[] copy = new byte[b.length];
                            System.arraycopy(b, 0, copy, 0, b.length);
                            mimeType = getMimeTypes(copy).get(0).toString();
                            System.out.println("File Serving Servlet: MIME type set to " + mimeType);
                       }*/
                        os.write(b, 0, numRead);
                    }
                    os.flush();
                    System.out.println("File Serving Servlet: Finished serving file " + dataFilePath);
                }
                else{
                    response.setContentType("text/plain");
                    response.getWriter().write("Error: The file with the result data does not exist.");
                    System.err.println("File Serving Servlet: The file "+ dataFilePath +" does not exist.");
                }
            }
            else {
                response.setContentType("text/plain");
                response.getWriter().write("Error: You do not have the permission to view this file.");
                System.err.println("File Serving Servlet: The user "+user+" is trying to view the file "+ dataFilePath +" that they do not have access permission to.");
            }
        }
        catch (IOException ex) {
            try{
                response.setContentType("text/plain");
                response.getWriter().write("An error occured while trying to read the file with the result data.\n" + ex.getMessage());
                System.out.println("File Serving Servlet: An error occured while trying to read the file " + dataFilePath);
                ex.printStackTrace();
            }
            catch(Exception ex2){ 
                ex2.printStackTrace();
            }
        }
    }

}
