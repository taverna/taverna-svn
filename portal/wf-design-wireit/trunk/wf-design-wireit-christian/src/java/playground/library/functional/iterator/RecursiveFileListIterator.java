package playground.library.functional.iterator;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Iterates over all non-directory files contained in some subdirectory of the 
 * current one.
 *
 * @author david
 */

/**
 * Downloaded from http://snippets.dzone.com/posts/show/3532
 */
public class RecursiveFileListIterator implements Iterator<File>
{
    private final FlatteningIterator flatteningIterator;
    
    public void remove() { } 
    
    public RecursiveFileListIterator(File file, FileFilter filter){
        this.flatteningIterator = new FlatteningIterator(new FileIterator(file, filter)); }
    
    public RecursiveFileListIterator(File file){
        this(file, null);}
    
    
    public boolean hasNext(){ 
        return flatteningIterator.hasNext();}
    
    public File next(){ 
        return (File)flatteningIterator.next();}
    
    
    /**
     * Iterator to iterate over all the files contained in a directory. It returns
     * a File object for non directories or a new FileIterator object for directories.
     */
    private static class FileIterator implements Iterator<Object>
    {
        private final Iterator<File> files;
        private final FileFilter filter;
        
        FileIterator(File file, FileFilter filter){ 
            this.files = Arrays.asList(file.listFiles(filter)).iterator();
            this.filter = filter;}
        
        public void remove() { }
        
        public Object next(){
            File next = this.files.next();
            
            if (next.isDirectory()) return new FileIterator(next, this.filter);
            else return next;}
        
        public boolean hasNext(){
            return this.files.hasNext();}       
    }
}