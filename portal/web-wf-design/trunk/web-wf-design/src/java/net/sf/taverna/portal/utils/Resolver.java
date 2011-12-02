package net.sf.taverna.portal.utils;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import net.sf.taverna.portal.wireit.URLEncoder;
import net.sf.taverna.portal.wireit.exception.WireItRunException;

/**
 * Helper class set up by the request which can be used for converting absolute, relative and file URIs.
 * 
 * @author Christian
 */
public class Resolver {
    
    /** This is everything that must be added to a relative URIString to make it absolute */
    private String absoluteRootUrl;
    
    /** This is everything that must be added to a relative URIString to make it an absolute file path */
    private String absoluteRootFilePath;
    
    /** Ideally local URIs can use the  file protocol, but if there are funny characters better to use the absolute URI. */
    private String localURIPrefix;       
    
    /** 
     * Constructor which extracts the three prefix Strings.
     * <p>
     * This has been written to work with older servlet code so requires both the request and the servlet.
     * 
     * @param request The HTTP request.
     * @param servletContext The servlet that caught the request.
     */
    public Resolver(HttpServletRequest request, ServletContext servletContext){
        absoluteRootUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() +
                request.getContextPath() + "/";
        absoluteRootFilePath = servletContext.getRealPath("/");
        //Fix windows placing the wrong slashes
        absoluteRootFilePath = absoluteRootFilePath.replace("\\", "/");
        localURIPrefix = URLEncoder.encode(absoluteRootUrl);
        if (localURIPrefix.equals(absoluteRootFilePath)){
            //nothing changed by encoding so ok to use file uri
            localURIPrefix = "file:" + absoluteRootFilePath;
        } else {
            //To dangerous to use file:uri so lets use full remote uri
            localURIPrefix = absoluteRootUrl;
        }
    }
    
    /**
     * Creates a URI based on a File and the directory that file is in.
     * @param grandParent The part of the files absolute path that should show up in the URI.
     * <p>
     * There may be a cleaner way of doing this by automatically checking which part of the file's 
     * absolute path is required but oh well this works.
     * @param file The file to be exposed as a URI.
     * @return
     * @throws WireItRunException 
     */
    public URI FileAndParentToURI(String grandParent, File file) throws WireItRunException{
        String uriSt = absoluteRootUrl + grandParent + "/" + file.getParentFile().getName() + "/" + file.getName();
        try {
          return new URI(uriSt);
       } catch (URISyntaxException ex) {
            throw new WireItRunException ("Error converting " + uriSt + " to uri.", ex);
       }
    }

    /**
     * Converts a URI object to a URI relative if possible.
     * 
     * @param object URI as an Object as that is how Listeners pass it.
     * @return URU (as String) in the best format for accessing locally.
     * @throws WireItRunException 
     */
    public String getURIObjectToRelativeURIString(Object object){
        URI uri = (URI)object;
        if (uri.isAbsolute()) {
            System.out.println("absolute");
            return uri.toString();
        } else {
            String relative = uri.getPath();
            System.out.println(relative);
            String absolute = localURIPrefix + relative;
            System.out.println(absolute);
            return absolute;
        }
    }
    
    /**
     * Converts a relative file.
     * @param relative relative part of the file name
     * @return The file
     */
    public File getRelativeFile(String relative) {
        String absolute = absoluteRootFilePath + relative;
        System.out.println(absolute);
        return new File(absolute);
    }


}
