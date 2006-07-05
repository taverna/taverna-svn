package org.embl.ebi.escience.scuflui.facets;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Set;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;

/**
 *
 *
 * @author Matthew Pocock
 */
public class ToString
        implements FacetFinderSPI
{
    private static final ColumnID COL_ID;
    private static final Set COLUMNS;

    static
    {
        COL_ID = new ColID();
        COLUMNS = Collections.singleton(COL_ID);
    }

    public boolean canMakeFacets(DataThing dataThing)
    {
        return !(dataThing.getDataObject() instanceof String) ||
                dataThing.getMetadata().getMIMETypes().length != 0;
    }

    public Set getStandardColumns(DataThing dataThing)
    {
        if(canMakeFacets(dataThing)) {
            return COLUMNS;
        } else {
            return Collections.EMPTY_SET;
        }
    }

    public ColumnID newColumn(DataThing dataThing)
    {
        return null;
    }

    public boolean hasColumn(ColumnID colID)
    {
        return COL_ID.equals(colID);
    }

    public DataThing getFacet(DataThing dataThing, ColumnID colID)
    {
        if(hasColumn(colID) && canMakeFacets(dataThing)) {
            return DataThingFactory.bake(dataThing.getDataObject().toString());
        } else {
            return null;
        }
    }

    public String getName()
    {
        return "ToString";
    }

    private static class ColID
            implements ColumnID
    {
        public Component getCustomiser(DataThing dataThing)
        {
            return null;
        }

        public String getName()
        {
            return "As String";
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
