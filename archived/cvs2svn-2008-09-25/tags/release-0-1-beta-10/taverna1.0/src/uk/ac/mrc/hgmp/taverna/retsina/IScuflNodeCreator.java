package uk.ac.mrc.hgmp.taverna.retsina;

/**
 * Implementors of this interface have the ability
 * to create new ScuflService nodes on demand. This
 * functionality is used to describe plugins to the
 * ScuflGraphPanel class that it delegates this creation
 * task to.
 * @author Tom Oinn
 */
public interface IScuflNodeCreator {

    /**
     * Create a new instance of a ScuflProcessor implementation
     */
    public IScuflProcessor createProcessor();

}
