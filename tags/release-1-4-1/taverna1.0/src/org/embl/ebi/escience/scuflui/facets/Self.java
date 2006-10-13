package org.embl.ebi.escience.scuflui.facets;

import org.embl.ebi.escience.baclava.DataThing;

import java.util.Set;
import java.util.Collections;
import java.awt.*;
import java.beans.PropertyChangeListener;

/**
 *
 *
 * @author Matthew Pocock
 */
public class Self
        implements FacetFinderSPI
{
    private static ColID COL;
    private static Set COL_SET;

    static
    {
        COL = new ColID();
        COL_SET = Collections.singleton(COL);
    }

    public boolean canMakeFacets(DataThing dataThing)
    {
        return true;
    }

    public Set getStandardColumns(DataThing dataThing)
    {
        return COL_SET;
    }

    public FacetFinderSPI.ColumnID newColumn(DataThing dataThing)
    {
        return null;
    }

    public boolean hasColumn(FacetFinderSPI.ColumnID colID)
    {
        return colID == COL;
    }

    public DataThing getFacet(DataThing dataThing, FacetFinderSPI.ColumnID colID)
    {
        return dataThing;
    }

    public String getName()
    {
        return "Self";
    }

    private static final class ColID
            implements ColumnID
    {
        public Component getCustomiser(DataThing dataThing)
        {
            return null;
        }

        public String getName()
        {
            return "Self";
        }

        public void addPropertyChangeListener(
                PropertyChangeListener listener)
        {
            // null implementation
        }

        public void removePropertyChangeListener(
                PropertyChangeListener listener)
        {
            // null implementation
        }

        public void addPropertyChangeListener(
                String propertyName,
                PropertyChangeListener listener)
        {
            // null implementation
        }

        public void removePropertyChangeListener(
                String propertyName,
                PropertyChangeListener listener)
        {
            // null implementation
        }
    }
}
