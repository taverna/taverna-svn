/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.manchester.cs.wireit.module;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import uk.ac.manchester.cs.wireit.URLEncoder;

/**
 *
 * @author Christian
 */
public class Resolver {
    
    private String absoluteRootUrl;
    private String absoluteRootFilePath;
    private String localURIPrefix;       
    
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
    
    public URI FileAndParentToURI(String grandParent, File file) throws WireItRunException{
        String uriSt = absoluteRootUrl + grandParent + "/" + file.getParentFile().getName() + "/" + file.getName();
        try {
          return new URI(uriSt);
       } catch (URISyntaxException ex) {
            throw new WireItRunException ("Error converting " + uriSt + " to uri.", ex);
       }
    }

    public String getURIObjectToRelativeURIString(Object object) throws WireItRunException{
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
    
    public File getRelativeFile(String relative) throws WireItRunException {
        String absolute = absoluteRootFilePath + relative;
        System.out.println(absolute);
        return new File(absolute);
    }


}
