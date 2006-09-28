package org.embl.ebi.escience.scuflui.renderers;


import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scuflui.facets.FTableColumn;
import org.embl.ebi.escience.scuflui.facets.FTableColumnModel;
import org.embl.ebi.escience.scuflui.facets.FacetFinderRegistry;
import org.embl.ebi.escience.scuflui.facets.FacetsTable;
import org.embl.ebi.escience.scuflui.spi.FacetFinderSPI;
import org.embl.ebi.escience.scuflui.spi.RendererSPI;

/**
 *
 *
 * @author Matthew Pocock
 */
public class CollectionAsTable
        implements RendererSPI
{
    private static Logger LOG = Logger.getLogger(CollectionAsTable.class);

    private static int depth = 0;

    public boolean isTerminal()
    {
        return false;
    }

    public boolean canHandle(RendererRegistry renderers,
                             DataThing dataThing)
    {
        return true;
    }

    public String getName()
    {
        return "Table";
    }

    public Icon getIcon(RendererRegistry renderers,
                        DataThing dataThing)
    {
        return null;
    }

    public JComponent getComponent(RendererRegistry renderers,
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
        FTableColumnModel columns = new FTableColumnModel();

        LOG.info(getName() + " Finders: (" + finders.size() + ") " + finders);
        for(Iterator i = finders.iterator(); i.hasNext(); ) {
            FacetFinderSPI finder = (FacetFinderSPI) i.next();
            LOG.info(getName() + " finder: " + finder);
            Set cols = finder.getStandardColumns(ourThing);
            LOG.info(getName() + " Columns: (" + cols.size() + ") " + cols);
            for(Iterator j = cols.iterator(); j.hasNext(); ) {
                FacetFinderSPI.ColumnID colID = (FacetFinderSPI.ColumnID) j.next();
                FTableColumn col = new FTableColumn();
                col.setColID(colID);
                col.setHorizontalSrollable(true);
                col.setVerticalScrollable(true);
                col.setFinder(finder);
                col.setName(colID.getName());
                col.setRenderer(null); // use default renderer
                columns.addColumn(col);
            }
        }

        if(columns.getColumnCount() == 0) {
            LOG.info("nothing" + --depth);
            return null;
        } else {
            table.setColumnModel(columns);
            table.setDataThing(dataThing);

            LOG.info("table" + --depth);
            return table;
        }
    }
}
