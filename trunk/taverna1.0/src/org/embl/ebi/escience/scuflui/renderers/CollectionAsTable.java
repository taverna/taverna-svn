package org.embl.ebi.escience.scuflui.renderers;

import org.embl.ebi.escience.scuflui.FacetsTable;
import org.embl.ebi.escience.scuflui.facets.FacetFinderSPI;
import org.embl.ebi.escience.scuflui.facets.FacetFinderRegistry;
import org.embl.ebi.escience.baclava.DataThing;

import javax.swing.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 *
 *
 * @author Matthew Pocock
 */
public class CollectionAsTable
        implements MimeTypeRendererSPI
{
    public boolean canHandle(MimeTypeRendererRegistry renderers,
                             DataThing dataThing)
    {
        return true;
    }

    public String getName()
    {
        return "Table";
    }

    public Icon getIcon(MimeTypeRendererRegistry renderers,
                        DataThing dataThing)
    {
        return null;
    }

    public JComponent getComponent(MimeTypeRendererRegistry renderers,
                                   DataThing dataThing)
    {
        FacetsTable table = new FacetsTable();
        FacetFinderRegistry facetReg = table.getFinders();
        List finders = facetReg.getFinders(dataThing);
        List facets = new ArrayList();
        int count = 0;
        for(Iterator i = finders.iterator(); i.hasNext(); ) {
            FacetFinderSPI finder = (FacetFinderSPI) i.next();
            Set cols = finder.getStandardColumns();
            for(Iterator j = cols.iterator(); j.hasNext(); ) {
                FacetsTable.Column col = new FacetsTable.Column();
                col.setColID(j.next());
                col.setHorizontalSrollable(true);
                col.setVerticalScrollable(true);
                col.setFinder(finder);
                col.setName("Col " + count++);
                //col.setRenderer();
            }
        }
        table.setColumns(facets);
        table.setDataThing(dataThing);

        return table;
    }
}
