/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.taverna.t2.portal;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author alex
 */
public class FileServingServlet extends HttpServlet {

     @Override
    public void init(){
        System.out.println("inittitttttttt");

     }
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
                System.out.println("hereeeeeeeeee");

        response.setContentType("text/html;charset=UTF-8");
        /*PrintWriter out = response.getWriter();
        try {
            //TODO output your page here
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet FileServingServlet</title>");  
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet FileServingServlet at " + request.getContextPath () + "</h1>");
            out.println("</body>");
            out.println("</html>");
          
        } finally { 
            out.close();
        }*/
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
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
        processRequest(request, response);

        try {
            HttpSession session = request.getSession();
            //String filename = (String) session.getAttribute("FILENAME");
            String filename = "/Users/alex/Desktop/" + (String) request.getParameter("filename");
            System.out.println("Serving file: " + filename);

            if ((filename != null) && (filename.length() > 0)) {
                response.setContentType(getServletContext().getMimeType(filename));

                OutputStream os = response.getOutputStream();
                byte b[] = new byte[1024];
                InputStream is = new FileInputStream(filename);
                int numRead = 0;

                while ((numRead=is.read(b)) > 0) {
                    os.write(b, 0, numRead);
                }

                os.flush();
            }
            else {
                System.err.println("ERROR! No filename detected.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
