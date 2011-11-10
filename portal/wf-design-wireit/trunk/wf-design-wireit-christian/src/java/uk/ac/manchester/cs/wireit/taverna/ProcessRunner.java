package uk.ac.manchester.cs.wireit.taverna;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class to run a process while capturing its output, with support methods to start, destroy, wait for it.
 * @author Christian
 */
public class ProcessRunner {
    
    private Process process;
    private StringBuilder builder;
    public static String NEW_LINE = System.getProperty("line.separator");
    private String[] args;
    private File directory;
 
    /** 
     * Sets up (but does not start) a process saving its arguements and the directory to run it in.
     * 
     * Saves the directory and arguements so that they can be retreived by getRunInfo() even before the process start.
     * <p>
     * Requires a second call to start otherwise the process will never run.
     * 
     * @param args List of arguements to be passed unchanged to the process 
     *    where arguement 0 is normally the process itslef.
     * @param directory Optional directory from which to run the process.
     *    If null the current directory will be used.
     * @throws IOException Thrown if a none null directory can not be found or is not a directory.
     */
    public ProcessRunner(String[] args, File directory) throws IOException {
        this.args = args;
        this.directory = directory;
        builder = new StringBuilder();
        if (directory != null){
            Utils.checkDirectory(directory);
            builder.append("Run directory: ");
            builder.append(directory.getAbsolutePath());
        } else {
            builder.append("No Run Directory used: ");
        }
        builder.append(NEW_LINE);
        for (String command:args){
            builder.append(command);            
            builder.append(NEW_LINE);
        }
    }
        
    /**
     * Creates and starts the process, logging the time it started.
     * <p>
     * Adds a note of when the process was started to the output.
     * Creates and starts the process
     * Adds StreamBuffers to both the process's output and error streams so that anything written there is recorded.
     * <p>
     * Does not block until the process is finished. 
     * @throws IOException Thrown by the process itself.
     */
    public void start() throws IOException{
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        if (directory != null){
            processBuilder.directory(directory);
        }
        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat();
        synchronized(builder){
            builder.append("====");
            builder.append(NEW_LINE);
            builder.append("Started: ");
            builder.append(formatter.format(now));
            builder.append(NEW_LINE);
        }
        process = processBuilder.start();
        StreamBufferer outputBufferer = new StreamBufferer(process.getInputStream(), "Output", builder);
        outputBufferer.start();
        StreamBufferer errorBufferer = new StreamBufferer(process.getErrorStream(), "Error", builder);
        errorBufferer.start();        
    }
    
    /**
     * Destories (stops) the process if it has been started.
     * 
     * Logs the time the process was destroyed to the output.
     * <p>
     * No effect if the process was never started.
     */
    public void destroy(){
        if (process != null){
            Date now = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat();
            synchronized(builder){
                builder.append("====");
                builder.append(NEW_LINE);
                builder.append("Proecess Destoryed at: ");
                builder.append(formatter.format(now));
                builder.append(NEW_LINE);
            }
            process.destroy();   
        }
     }
    
    /**
     * Returns the output of the [process run up to this point.
     * 
     * Non blocking method that returns the output of the process up to this point.
     * Expected to hold the parameters used to start. 
     * Depending on when it is called may (but is never guaranteed to) include intermediate ouput.
     * <p> 
     * Unlike other methods this one is not expected to fail or throw an Exception even if the run fails 
     *    or was interrupted but this behaviour is not guaranteed.
     * @return Partial process output
     */
    public String getRunInfo(){
        return builder.toString();
    }
    
    /**
     * Blocking process that waits for the process to finish and then returns the result valued returned by that process.
     * @return Result value returned by the process
     * @throws InterruptedException Thrown by the process if it was Interruppted
     * @throws ProcessException Thrown if this method called before the process was started.
     */
    public int waitFor() throws InterruptedException, ProcessException{
        if (process == null){
            throw new ProcessException ("Process never started!");
        }
        return process.waitFor();
    }
    
    /**
     * Blocking method that logs a process is finished and returns its output.
     * 
     * Blocks until the process run has finished.
     * Works even if the process result was not 0.
     * 
     * @return Output of this process including the arguements used to start is
     * 
     * @throws ProcessException If the process was not started.
     */
    public String getOutput() throws InterruptedException, ProcessException{
        int result = waitFor();
        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat();
        synchronized(builder){
            builder.append("====");
            builder.append(NEW_LINE);
            builder.append("Finished by: ");
            builder.append(formatter.format(now));
            builder.append(NEW_LINE);
            builder.append("Run result was: ");
            builder.append(result);
            builder.append(NEW_LINE);
        }
        return builder.toString();
    }
    
}