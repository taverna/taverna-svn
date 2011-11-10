package uk.ac.manchester.cs.wireit.taverna;

/**
 * Interface to say that the object has a method to allow it (or the process it wraps to be destroyed (stopped)
 * 
 * @author Christian
 */
public interface Destoryable {
    
    /**
     * Destroys (Stops) this Object or more likely the process that it wraps)
     */
    public void destroy();
}
