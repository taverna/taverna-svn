package org.embl.ebi.escience.scuflui.facets;

import org.embl.ebi.escience.scuflui.facets.FacetFinderSPI;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;

import java.util.Collections;
import java.util.Set;

/**
 *
 *
 * @author Matthew Pocock
 */
public class ToString
        implements FacetFinderSPI
{
    private static final Object COL_ID;
    private static final Set COLUMNS;

    static
    {
        COL_ID = ToString.class.toString() + ":TO_STRING";
        COLUMNS = Collections.singleton(COL_ID);
    }

    public boolean canMakeFacets(DataThing dataThing)
    {
        return true;
    }

    public Set getStandardColumns()
    {
        return COLUMNS;
    }

    public boolean hasColumn(Object colID)
    {
        return COL_ID.equals(colID);
    }

    public DataThing getFacet(DataThing dataThing, Object colID)
    {
        if(hasColumn(colID)) {
            return DataThingFactory.bake(dataThing.getDataObject().toString());
        }

        return null;
    }

    public String getName()
    {
        return "ToString";
    }
}
