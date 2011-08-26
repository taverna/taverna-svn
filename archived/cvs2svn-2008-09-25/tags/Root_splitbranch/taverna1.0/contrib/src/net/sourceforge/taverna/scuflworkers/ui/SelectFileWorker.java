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
 * This processor allows the user to select a file.  
 * File reading/parsing is performed
 * by other other processors.  It should only be used
 * with interactive workflows that are being run from Taverna.  Server-side
 * or command-line workflows should not use this processor.
 * 
 * @author Mark
 * @version $Revision: 1.5 $
 * 
 * @tavinput title				The title to be displayed in the titlebar of the dialog.
 * @tavinput fileExtensions		An array of file extensions that you want to filter.  For example "GIF", "JPG", "JPEG"
 * @tavinput fileExtLabels		An array of display text to be used to aid the user in selecting a filter. 
 * 								For example, "Images (GIF)", "Images (JPG)","Images (JPEG)"
 * 
 * @tavoutput selectedFile		The file that the user selected.
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
        
        
        chooser.showOpenDialog(null);
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
