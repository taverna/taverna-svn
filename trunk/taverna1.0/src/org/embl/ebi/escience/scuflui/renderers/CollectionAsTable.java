package org.embl.ebi.escience.scuflui.renderers;

import org.embl.ebi.escience.scuflui.FacetsTable;
import org.embl.ebi.escience.scuflui.facets.FacetFinderSPI;
import org.embl.ebi.escience.scuflui.facets.FacetFinderRegistry;
import org.embl.ebi.escience.baclava.DataThing;
import org.apache.log4j.Logger;

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
    private static Logger LOG = Logger.getLogger(CollectionAsTable.class);

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
        LOG.info(getName() + " getComponent for " + dataThing);

        FacetsTable table = new FacetsTable();
        FacetFinderRegistry facetReg = table.getFinders();
        List finders = facetReg.getFinders(dataThing);
        List facets = new ArrayList();

        LOG.info(getName() + " Finders: (" + finders.size() + ") " + finders);
        int count = 0;
        for(Iterator i = finders.iterator(); i.hasNext(); ) {
            FacetFinderSPI finder = (FacetFinderSPI) i.next();
            LOG.info(getName() + " finder: " + finder);
            Set cols = finder.getStandardColumns(dataThing);
            LOG.info(getName() + " Columns: (" + cols.size() + ") " + cols);
            for(Iterator j = cols.iterator(); j.hasNext(); ) {
                FacetsTable.Column col = new FacetsTable.Column();
                col.setColID((FacetFinderSPI.ColumnID) j.next());
                col.setHorizontalSrollable(true);
                col.setVerticalScrollable(true);
                col.setFinder(finder);
                col.setName("Col " + count++);
                col.setRenderer(null); // use default renderer
                facets.add(col);
            }
        }

        if(facets.isEmpty()) {
            return null;
        } else {
            table.setColumns(facets);
            table.setDataThing(dataThing);

            return table;
        }
    }
}
