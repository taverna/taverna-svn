/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.taverna.portal;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil2;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.ImageIcon;

/**
 * This servlet reads the file path from the session object and
 * sends the file back to the user as a response.
 *
 * @author Alex Nenadic
 */
public class FileServingServlet extends HttpServlet {

    private static final String APPLICATION_OCTETSTREAM = "application/octet-stream";
    public static final String DATA_FILE_PATH = "data_file_path"; // absolute path to the file with data
    public static final String MIME_TYPE = "mime_type";
    public static final String DATA_SIZE_IN_KB = "data_size_in_kb";
    // Max size of data to be sent as preview (in KB)
    public static long MAX_PREVIEW_DATA_SIZE_IN_KB = 250;
    
    
    @Override
    public void init() {
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

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Get the file to fetch/serve to the user
        String dataFilePath = URLDecoder.decode((String) request.getParameter(DATA_FILE_PATH), "UTF-8");
        
        // Get the content type of the file to fetch (this is also passed as a parameter)
        String mimeType = URLDecoder.decode((String) request.getParameter(MIME_TYPE), "UTF-8");

        // Is this the thumbnail of the image or the actual size?
        boolean isThumbnail = request.getQueryString().contains("thumbnail");

        System.out.println("File Serving Servlet: Received request " + request.getRequestURI() + "?" + request.getQueryString());
        System.out.println("File Serving Servlet: Received request for thumbnail " + isThumbnail);

        sendFile(dataFilePath, mimeType, isThumbnail, request, response);
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
            if (bytesString.contains("http://www.w3.org/2000/svg")) {
                MimeType svgMimeType = new MimeType("image/svg+xml");
                if (!mimeList.contains(svgMimeType)) {
                    mimeList.add(svgMimeType);
                }
            }
            if (mimeList.isEmpty()) { // if it is not recognised
                mimeList.add(new MimeType(APPLICATION_OCTETSTREAM));
            }

        } catch (IOException ex) {
            mimeList.add(new MimeType(APPLICATION_OCTETSTREAM));
        }

        return mimeList;
    }

    public void sendFile(String dataFilePath, String mimeType, boolean isThumbnail, HttpServletRequest request, HttpServletResponse response) {

        try {
            File dataFile = new File(dataFilePath);

            File thumbnailFile = null;
            if (isThumbnail) {
                thumbnailFile = new File(dataFilePath + "_thumbnail.jpg");
                if (!thumbnailFile.exists()) { // have we already generated the thumbnail?
                    resizeImage(dataFile, thumbnailFile, 250);
                }
            }

            // We do not serve arbritary files here - just those in the data directory (for which we use
            // the operating system's temp directory) so make sure we check that here.
            File fileToSend;
            if (isThumbnail && thumbnailFile.exists()) {
                fileToSend = thumbnailFile;
            } else {
                fileToSend = dataFile;
            }

            File tmpDir = new File(System.getProperty("java.io.tmpdir"));

            if (fileToSend.getCanonicalPath().startsWith(tmpDir.getAbsolutePath())) {
                if (fileToSend.exists()) {
                    OutputStream os = response.getOutputStream();

                    byte b[] = new byte[1024];
                    InputStream is = new FileInputStream(fileToSend);
                    int numRead = 0;
                    response.setContentType(mimeType);
                    response.setContentLength(is.available());

                    while ((numRead = is.read(b)) > 0) {
                        /*if (mimeType == null){
                        byte[] copy = new byte[b.length];
                        System.arraycopy(b, 0, copy, 0, b.length);
                        mimeType = getMimeTypes(copy).get(0).toString();
                        System.out.println("File Serving Servlet: MIME type set to " + mimeType);
                        }*/
                        os.write(b, 0, numRead);
                    }
                    os.flush();
                    System.out.println("File Serving Servlet: Finished serving file " + fileToSend.getAbsolutePath());
                } else {
                    response.setContentType("text/plain");
                    response.getWriter().write("Error: The file with the requested data does not exist.");
                    System.err.println("File Serving Servlet: The file " + fileToSend.getAbsolutePath() + " does not exist.");
                }
            } else {
                response.setContentType("text/plain");
                response.getWriter().write("Error: You do not have the permission to view this file.");
                System.err.println("File Serving Servlet: The user from remote host " + request.getRemoteHost() + "(IP address: " + request.getRemoteAddr() + ") is trying to view the file " + fileToSend.getAbsolutePath() + " that they do not have access permission to.");
            }
        } catch (IOException ex) {
            try {
                response.setContentType("text/plain");
                response.getWriter().write("An error occured while trying to read the requested data.\n" + ex.getMessage());
                System.err.println("File Serving Servlet: An error occured while trying to read the file " + dataFilePath);
                ex.printStackTrace();
            } catch (Exception ex2) {
                ex2.printStackTrace();
            }
        }
    }

    /**
     * Based on Sergejs Aleksejevs
     * public static String getResizedImageIconTempFileURL(URL sourceImageURL, int iRequiredWidth, int iRequiredHeight) {
     * from net.sf.taverna.t2.portal.Util
     */
    public static void resizeImage(File sourceImageFile, File destinationImageFile, int fixedSizeInPixels) {
        System.out.println("File Serving Servlet: Resizing image data to a preview thumbnail of size " + fixedSizeInPixels);
        try {
            // resize the image icon
            byte fileContent[] = null;
            FileInputStream fin = null;
            try {
                fin = new FileInputStream(sourceImageFile);
                fileContent = new byte[(int) sourceImageFile.length()];
                fin.read(fileContent);

            } catch (Exception ex) {
                System.out.println("Failed to read image file " + sourceImageFile.getAbsolutePath() + " in order to create its thumbnail.");
                ex.printStackTrace();
                return;
            } finally {
                fin.close();
            }

            ImageIcon sourceImageIcon = new ImageIcon(fileContent);
            int width, height, ratio;
            if (sourceImageIcon.getIconWidth() > fixedSizeInPixels) {
                ratio = sourceImageIcon.getIconWidth() / fixedSizeInPixels;
                width = fixedSizeInPixels;
                height = fixedSizeInPixels / ratio;
            } else if (sourceImageIcon.getIconHeight() > fixedSizeInPixels) {
                ratio = sourceImageIcon.getIconHeight() / fixedSizeInPixels;
                height = fixedSizeInPixels;
                width = fixedSizeInPixels / ratio;
            } else { // nothing to resize - image is already smaller that the requested size
                System.out.println("File Serving Servlet: Nothing to resize, image is already smaller than the requested thumbnail size.");
                return;
            }

            ImageIcon resizedImageIcon = getResizedImageIcon(sourceImageIcon, width, height);

            Image img = resizedImageIcon.getImage();
            BufferedImage bi = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = bi.createGraphics();
            // Draw img into bi so we can write it to file.
            g2.drawImage(img, 0, 0, null);
            g2.dispose();
            // Now bi contains the img.
            // Note: img may have transparent pixels in it; if so, okay.
            // If not and you can use TYPE_INT_RGB you will get better
            // performance with it in the jvm.
            ImageIO.write(bi, "jpg", destinationImageFile);
        } catch (Exception ex) {
            System.out.println("Failed to resize image file " + sourceImageFile.getAbsolutePath() + " in order to create its thumbnail.");
            ex.printStackTrace();
            destinationImageFile.delete();
        }
    }

    // ******** RESIZING OF IMAGES ********
    /**
     * The method will scale down the <b>sourceImageIcon</b> in a way that it will
     * fit into the rectangle with dimensions <b>(iRequiredWidth,
     * iRequiredHeight)</b> with the original aspect ratio being preserved.
     *
     * @param iRequiredWidth
     *          Maximum desired width of the resized image.
     * @param iRequiredHeight
     *          Maximum desired height of the resized image.
     * @author Sergejs Aleksejevs
     */
    public static ImageIcon getResizedImageIcon(ImageIcon sourceImageIcon, int iRequiredWidth, int iRequiredHeight) {
        // *** calculate the desired width and height of the resized image ***
        int iWidth = sourceImageIcon.getIconWidth();
        int iHeight = sourceImageIcon.getIconHeight();

        float fWidthResizeRatio = iWidth / (float) iRequiredWidth;
        float fHeightResizeRatio = iHeight / (float) iRequiredHeight;

        // the chosen resize ratio will be the greatest of the two -
        // this way we will preserve aspect ratio of the sides of the
        // original image
        float fResizeRatio = Math.max(fWidthResizeRatio, fHeightResizeRatio);

        // obtain the width and height of the new image
        int iNewWidth = Math.round(iWidth / fResizeRatio);
        int iNewHeight = Math.round(iHeight / fResizeRatio);

        // *** make the actual resizing work ***
        BufferedImage resizedImage = new BufferedImage(iNewWidth, iNewHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = resizedImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(sourceImageIcon.getImage(), 0, 0, iNewWidth, iNewHeight, null);
        g2.dispose();
        return (new ImageIcon(resizedImage, ""));
    }
}
