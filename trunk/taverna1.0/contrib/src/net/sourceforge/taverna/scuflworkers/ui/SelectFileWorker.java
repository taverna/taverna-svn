package net.sourceforge.taverna.scuflworkers.ui;

import java.awt.CardLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This class allows the user to select a file.  File reading/parsing is performed
 * by other classes.
 * 
 * Last edited by $Author: phidias $
 * 
 * @author Mark
 * @version $Revision: 1.3 $
 */
public class SelectFileWorker implements LocalWorker {
    
    public SelectFileWorker(){
        
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
     */
    public Map execute(Map inputMap) throws TaskExecutionException {
        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
        HashMap outputMap = new HashMap();
        DataThingAdapter outAdapter = new DataThingAdapter(outputMap);

        String component = inAdapter.getString("usePreviewComponent");
        boolean useComp = (component!= null)?Boolean.getBoolean(component):false;
               
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(inAdapter.getString("title"));
        
        String[] fileTypeList = inAdapter.getString("fileExtensions").split(",");
        String[] filterLabelList = inAdapter.getString("fileExtLabels").split(",");
        
        if (fileTypeList != null && filterLabelList != null && fileTypeList.length != filterLabelList.length){
            throw new TaskExecutionException("The list of extensions and file filter labels must be the same length");
        }      
        
        if (useComp){
            chooser.setAccessory(new Previewer());
        }
        
        // create the file filters
        for (int i=0; i < fileTypeList.length; i++){
            FileExtFilter filter = new FileExtFilter(fileTypeList[i], filterLabelList[i],true);
            chooser.setFileFilter(filter);           
        }
        
        chooser.addPropertyChangeListener(new PropertyChangeListener(){
            
            public void propertyChange(PropertyChangeEvent e){
                if(e.getPropertyName().equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)){
                    File f = (File)e.getNewValue();
                    if (f != null && f.isFile()){
                        String s = f.getPath();
                        String suffix = null;
                    }
                    
                }
            }
            
        });
        
        
        int state = chooser.showOpenDialog(null);
        File file = chooser.getSelectedFile();
        outAdapter.putString("selectedFile", file.getAbsolutePath());
        
        return outputMap;
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
     */
    public String[] inputNames() {
        return new String[]{"title","fileExtensions","fileExtLabels"};
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputTypes()
     */
    public String[] inputTypes() {
        return new String[]{"'text/plain'","'text/plain'","'text/plain'"};
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputNames()
     */
    public String[] outputNames() {
       return new String[]{"selectedFile"};
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputTypes()
     */
    public String[] outputTypes() {
       return new String[]{"'text/plain'"};
    }
    
    /**
     * This method determines whether or not a string array contains a specific value.
     * @param values
     * @param searchString
     * @return
     */
    private boolean contains(String[] values, String searchString){
        boolean doesContain = false;
        
        for (int i=0; i < values.length; i++){
           doesContain = (values[i].indexOf(searchString) != -1);
           if (doesContain){
               break;
           }
        }
        
        return doesContain;
    }
    
 
    
    class Previewer extends JPanel{
        
        public Previewer(){
            comp.setLayout(cardLayout);
            imagePanel.add(label);
            
            cardLayout.addLayoutComponent("image",label);
            cardLayout.addLayoutComponent("doc",doc);
                
        }
        
        public void show(File file){
            String filename = file.getName();
            if (filename.endsWith("gif") || filename.endsWith("jpg")){
                cardLayout.show(comp, "image");
                icon = Toolkit.getDefaultToolkit().getImage(filename);
                imageIcon.setImage(icon);
                label.setIcon(imageIcon);
               
            }else if (filename.endsWith("rtf") || filename.endsWith("html")||filename.endsWith("txt")){
                cardLayout.show(comp,"doc");
                
            }
        }
        
       
        JLabel label = new JLabel();
        JPanel comp = new JPanel();
        JPanel imagePanel = new JPanel();
        ImageIcon imageIcon = null;
        Image icon;
        JEditorPane doc = new JEditorPane();
        CardLayout cardLayout = new CardLayout();
    }
    
    
    class FileExtFilter extends FileFilter{
     
        public FileExtFilter(String ext, String label, boolean includeDir){
            this.ext = ext;
            this.label = label;
            this.includeDir = includeDir;
        }
        
        public String getDescription(){
            return this.label;
        }
        
        public boolean accept(File file){
            if (file.isDirectory() && includeDir){
                return true;
            }else {
            return file.getName().endsWith(this.ext);
            }
        }
        
        String ext, label;
        boolean includeDir;
    }

}
