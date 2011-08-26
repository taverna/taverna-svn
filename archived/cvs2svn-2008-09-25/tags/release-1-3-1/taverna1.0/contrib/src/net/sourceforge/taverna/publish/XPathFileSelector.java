package net.sourceforge.taverna.publish;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileUtil;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;


/**
 * This class accepts a file if the file contains at least one node that
 * matches the XPath query.
 * 
 * Last edited by $Author: phidias $
 * 
 * @author Mark
 * @version $Revision: 1.1 $
 */
public class XPathFileSelector implements FileSelector {
    
    private String path;
    private boolean recurseDirectories = false;

    /**
     * Constructor
     * @param path
     */
    public XPathFileSelector(String path){
        this.path = path;
    }
    
    /**
     * Constructor
     * @param path
     * @param recurseDirectories
     */
    public XPathFileSelector(String path, boolean recurseDirectories){
        this.path = path;
        this.recurseDirectories = recurseDirectories;
    }

    /**
     * @see org.apache.commons.vfs.FileSelector#includeFile(org.apache.commons.vfs.FileSelectInfo)
     */
    public boolean includeFile(FileSelectInfo info) throws Exception {
        boolean accept = false;
        try {
        	
        	SAXReader reader = new SAXReader();
        	FileObject file = info.getFile();
        	byte[] content = FileUtil.getContent(file);
        	ByteArrayInputStream is = new ByteArrayInputStream(content);
            Document document = reader.read(is);
            List nodelist = document.selectNodes(this.path);
            
            accept = (nodelist.size() > 0);
            
        }catch (Throwable th){
        	th.printStackTrace();
        }
        
        return accept;
    }

    /**
     * @see org.apache.commons.vfs.FileSelector#traverseDescendents(org.apache.commons.vfs.FileSelectInfo)
     */
    public boolean traverseDescendents(FileSelectInfo arg0) throws Exception {
        return this.recurseDirectories;
    }

}
