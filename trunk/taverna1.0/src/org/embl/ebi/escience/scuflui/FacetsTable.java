package org.embl.ebi.escience.scuflui;

import org.embl.ebi.escience.scuflui.facets.FacetFinderSPI;
import org.embl.ebi.escience.scuflui.facets.FacetFinderRegistry;
import org.embl.ebi.escience.scuflui.renderers.MimeTypeRendererRegistry;
import org.embl.ebi.escience.scuflui.renderers.MimeTypeRendererSPI;
import org.embl.ebi.escience.baclava.DataThing;

import javax.swing.*;
import java.util.*;
import java.util.List;
import java.awt.*;

/**
 * A tabular display of data using FacetFinders to decompose the information
 * into columns.
 *
 * @author Matthew Pocock
 */
public class FacetsTable
        extends JPanel
{
    private static final GridBagConstraints GBC;

    static
    {
        GBC = new GridBagConstraints();
        GBC.weightx = 1.0;
        GBC.weighty = 1.0;
        GBC.anchor = GBC.CENTER;
        GBC.fill = GBC.BOTH;
        GBC.insets = new Insets(1,1,1,1);
    }

    private List columns;
    private DataThing dataThing;
    private FacetFinderRegistry finders;
    private MimeTypeRendererRegistry renderers;

    public FacetsTable()
    {
        super(new GridBagLayout(), true);
        this.columns = new ArrayList();
        this.finders = FacetFinderRegistry.instance();
        this.renderers = MimeTypeRendererRegistry.instance();
        resizeAndValidate();
    }

    public FacetsTable(List columns,
                       DataThing theDataThing,
                       FacetFinderRegistry finders,
                       MimeTypeRendererRegistry renderers)
    {
        this();
        this.columns.addAll(columns);
        this.dataThing = theDataThing;
        if(finders != null)   { this.finders = finders; }
        if(renderers != null) { this.renderers = renderers; }
        resizeAndValidate();
    }

    public FacetsTable(DataThing theDataThing,
                       FacetFinderRegistry finders,
                       MimeTypeRendererRegistry renderers)
    {
        this(null, theDataThing, finders, renderers);
    }

    public List getColumns()
    {
        return new ArrayList(columns);
    }

    public void setColumns(List columns)
    {
        if(columns == null) {
            this.columns.clear();
        } else {
            this.columns = new ArrayList(columns);
        }

        resizeAndValidate();
    }

    public DataThing getDataThing()
    {
        return dataThing;
    }

    public void setDataThing(DataThing dataThing)
    {
        this.dataThing = dataThing;
        resizeAndValidate();
    }

    public FacetFinderRegistry getFinders()
    {
        return finders;
    }

    public void setFinders(FacetFinderRegistry finders)
    {
        if(finders == null) {
            throw new NullPointerException("Can't set finder registry to null");
        }

        this.finders = finders;
    }

    public MimeTypeRendererRegistry getRenderers()
    {
        return renderers;
    }

    public void setRenderers(MimeTypeRendererRegistry renderers)
    {
        this.renderers = renderers;
        resizeAndValidate();
    }

    protected void resizeAndValidate()
    {
        // this is bruit force - we will probably want to optimize this for
        // incremental changes to the model
        removeAll();

        if(dataThing == null) {
            return;
        } else {
            GridBagConstraints gbc = (GridBagConstraints) GBC.clone();
            gbc.gridx = 0;
            gbc.gridy = 0;

            addheader(gbc);
            gbc.gridx++;
            gbc.gridy = 0;

            if(dataThing.getDataObject() instanceof Collection) {
                for(Iterator i = dataThing.childIterator(); i.hasNext(); ) {
                    DataThing part = (DataThing) i.next();
                    addRow(part, gbc);
                }
                gbc.gridx++;
                gbc.gridy = 0;
            } else {
                addRow(dataThing, gbc);
            }
        }
    }

    private void addheader(GridBagConstraints gbc)
    {
        for(Iterator i = columns.iterator(); i.hasNext(); ) {
            Column col = (Column) i.next();
            add(new JLabel(col.getName()), gbc);
            gbc.gridy++;
        }
    }

    private void addRow(DataThing dataThing,
                        GridBagConstraints gbc)
    {
        for(Iterator i = columns.iterator(); i.hasNext(); ) {
            Column col = (Column) i.next();
            DataThing dataObj = col.getFinder().getFacet(dataThing, col.getColID());
            MimeTypeRendererSPI renderer = col.getRenderer();
            if(renderer == null) {
                renderer = renderers.getRenderer(dataThing);
            }
            if(renderer != null) {
                    add(renderer.getComponent(renderers,dataObj),
                        gbc);
            }
            gbc.gridy++;
        }
    }

    public static final class Column
    {
        private String name;
        private FacetFinderSPI finder;
        private MimeTypeRendererSPI renderer;
        private Object colID;
        private boolean horizontalSrollable;
        private boolean verticalScrollable;

        public Column()
        {
        }

        public Column(String name, FacetFinderSPI finder, MimeTypeRendererSPI renderer, Object colID, boolean rowSrollable, boolean colScrollable)
        {
            this.name = name;
            this.finder = finder;
            this.renderer = renderer;
            this.colID = colID;
            this.horizontalSrollable = rowSrollable;
            this.verticalScrollable = colScrollable;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public FacetFinderSPI getFinder()
        {
            return finder;
        }

        public void setFinder(FacetFinderSPI finder)
        {
            this.finder = finder;
        }

        public MimeTypeRendererSPI getRenderer()
        {
            return renderer;
        }

        public void setRenderer(MimeTypeRendererSPI renderer)
        {
            this.renderer = renderer;
        }

        public Object getColID()
        {
            return colID;
        }

        public void setColID(Object colID)
        {
            this.colID = colID;
        }

        public boolean isHorizontalSrollable()
        {
            return horizontalSrollable;
        }

        public void setHorizontalSrollable(boolean horizontalSrollable)
        {
            this.horizontalSrollable = horizontalSrollable;
        }

        public boolean isVerticalScrollable()
        {
            return verticalScrollable;
        }

        public void setVerticalScrollable(boolean verticalScrollable)
        {
            this.verticalScrollable = verticalScrollable;
        }
    }
}
