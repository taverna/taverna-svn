package org.embl.ebi.escience.scuflui;

import java.beans.PropertyChangeEvent;

/**
 * Event informing you of the data associated with a change in a table's
 * columns.
 *
 * @author Matthew Pocock
 */
public class FTableColumnModelEvent
        extends java.util.EventObject
{
    private final int fromIndex;
    private final int toIndex;
    private final PropertyChangeEvent cause;

    public FTableColumnModelEvent(Object source,
                                  int fromIndex, int toIndex,
                                  PropertyChangeEvent cause)
    {
        super(source);
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
        this.cause = cause;
    }

    public int getFromIndex()
    {
        return fromIndex;
    }

    public int getToIndex()
    {
        return toIndex;
    }

    public PropertyChangeEvent getCause()
    {
        return cause;
    }

    public String toString()
    {
        return super.toString()
                + " fromIndex: " + fromIndex
                + " toIndex: " + toIndex
                + " cause: " + cause;
    }
}
