package org.embl.ebi.escience.scuflui.renderers;

import org.embl.ebi.escience.scuflui.FacetsTable;
import org.embl.ebi.escience.scuflui.facets.FacetFinderSPI;
import org.embl.ebi.escience.scuflui.facets.FacetFinderRegistry;
import org.embl.ebi.escience.baclava.DataThing;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.util.*;

/**
 *
 *
 * @author Matthew Pocock
 */
public class CollectionAsTable
        implements MimeTypeRendererSPI
{
    private static Logger LOG = Logger.getLogger(CollectionAsTable.class);

    private static int depth = 0;

    public boolean isTerminal()
    {
        return false;
    }

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
        LOG.info("" + depth++);
        if(depth > 4) {
            throw new AssertionError("Depth exceeded");
        }
        LOG.info(getName() + " getComponent for " + dataThing);

        DataThing ourThing = dataThing;
        if(dataThing.getDataObject() instanceof Collection) {
            Iterator ci = dataThing.childIterator();
            if(ci.hasNext()) {
                ourThing = (DataThing) ci.next();
            }
        }

        FacetsTable table = new FacetsTable();
        FacetFinderRegistry facetReg = table.getFinders();
        List finders = facetReg.getFinders(ourThing);
        List facets = new ArrayList();

        LOG.info(getName() + " Finders: (" + finders.size() + ") " + finders);
        int count = 0;
        for(Iterator i = finders.iterator(); i.hasNext(); ) {
            FacetFinderSPI finder = (FacetFinderSPI) i.next();
            LOG.info(getName() + " finder: " + finder);
            Set cols = finder.getStandardColumns(ourThing);
            LOG.info(getName() + " Columns: (" + cols.size() + ") " + cols);
            for(Iterator j = cols.iterator(); j.hasNext(); ) {
                FacetFinderSPI.ColumnID colID = (FacetFinderSPI.ColumnID) j.next();
                FacetsTable.Column col = new FacetsTable.Column();
                col.setColID(colID);
                col.setHorizontalSrollable(true);
                col.setVerticalScrollable(true);
                col.setFinder(finder);
                col.setName("Col " + count++ + "(" + colID.getName() + ")");
                col.setRenderer(null); // use default renderer
                facets.add(col);
            }
        }

        if(facets.isEmpty()) {
            LOG.info("nothing" + --depth);
            return null;
        } else {
            table.setColumns(facets);
            table.setDataThing(dataThing);

            LOG.info("table" + --depth);
            return table;
        }
    }
}
