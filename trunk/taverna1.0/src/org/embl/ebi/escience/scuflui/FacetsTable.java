package org.embl.ebi.escience.scuflui;

import org.embl.ebi.escience.scuflui.facets.FacetFinderSPI;
import org.embl.ebi.escience.scuflui.facets.FacetFinderRegistry;
import org.embl.ebi.escience.scuflui.renderers.MimeTypeRendererRegistry;
import org.embl.ebi.escience.scuflui.renderers.MimeTypeRendererSPI;
import org.embl.ebi.escience.baclava.DataThing;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * A tabular display of data using FacetFinders to decompose the information
 * into columns.
 *
 * @author Matthew Pocock
 */
public class FacetsTable
        extends JPanel
{
    private static final Logger LOG = Logger.getLogger(FacetsTable.class);
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

    // todo: There is no sensible incremental model editing - needs adding
    // todo: Add standard table stuff - scrolleable, scrollalbe cells, reorder
    // todo: the columns, better borders, row/col headings in seperate panes

    private List columns;
    private DataThing dataThing;
    private FacetFinderRegistry finders;
    private MimeTypeRendererRegistry renderers;

    private DataThing exampleThing;
    private List exampleRow;

    public FacetsTable()
    {
        super(new GridBagLayout(), true);
        this.columns = new ArrayList();
        this.finders = FacetFinderRegistry.instance();
        this.renderers = MimeTypeRendererRegistry.instance();
        setOpaque(true); // required to make sure bits of the rendering aren't
                         // left behind
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
        setOpaque(true); // required to make sure bits of the rendering aren't
                         // left behind
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
        LOG.info("resizeAndValidate: redoing layout: " + dataThing);
        // this is bruit force - we will probably want to optimize this for
        // incremental changes to the model
        removeAll();
        exampleThing = null;
        exampleRow = null;

        if(dataThing != null) {
            LOG.info("resizeAndValidate: adding all data rows");
            GridBagConstraints gbc = (GridBagConstraints) GBC.clone();
            gbc.gridx = 0;
            gbc.gridy = 1;

            if(dataThing.getDataObject() instanceof Collection) {
                for(Iterator i = dataThing.childIterator(); i.hasNext(); ) {
                    DataThing part = (DataThing) i.next();
                    addRow(part, gbc);
                    gbc.gridx = 0;
                    gbc.gridy++;
                }
            } else {
                addRow(dataThing, gbc);
            }

            LOG.info("resizeAndValidate: adding header row");
            gbc.gridx = 0;
            gbc.gridy = 0;

            addheader(gbc);
        }

        LOG.info("resizeAndValidate: done");
    }

    private void addheader(GridBagConstraints gbc)
    {
        GridBagConstraints gbcLoc = (GridBagConstraints) gbc.clone();
        gbcLoc.anchor = GridBagConstraints.NORTH;
        gbcLoc.fill = GridBagConstraints.NONE;
        gbcLoc.weightx = 0.0;
        gbcLoc.weighty = 0.0;
        for(ListIterator i = columns.listIterator(); i.hasNext(); ) {
            int indx = i.nextIndex();
            Column col = (Column) i.next();
            JLabel title = new JLabel(col.getName());
            title.addMouseListener(new ColumnListener(col, title, indx));
            add(title, gbcLoc);
            gbcLoc.gridx++;
        }
    }

    private void addRow(DataThing dataThing,
                        GridBagConstraints gbc)
    {
        List ex = null;
        if(exampleRow == null) {
            ex = new ArrayList();
        }

        if(exampleThing == null) {
            exampleThing = dataThing;
        }

        for(Iterator i = columns.iterator(); i.hasNext(); ) {
            Column col = (Column) i.next();
            DataThing dataObj = col.getFinder().getFacet(dataThing, col.getColID());

            if(ex != null) {
                ex.add(dataObj);
            }

            if(dataObj != null) {
                JComponent cmp = null;
                MimeTypeRendererSPI renderer = col.getRenderer();
                if(renderer != null) {
                    try {
                        cmp = renderer.getComponent(renderers, dataObj);
                    } catch (Exception e) {
                        LOG.error("Problem creating component from renderer",
                                  e);
                    }
                }

                if(cmp == null) {
                    for(Iterator ri = renderers.getRenderers(dataObj).iterator();
                        cmp == null && ri.hasNext(); )
                    {
                        renderer = (MimeTypeRendererSPI) ri.next();
                        try {
                            cmp = renderer.getComponent(renderers, dataObj);
                        } catch (Exception e) {
                            LOG.error("Problem creating component from renderer",
                                      e);
                        }
                    }
                }

                if(cmp == null) {
                    cmp = new JLabel("No Renderer");
                }

                add(cmp, gbc);
            }
            gbc.gridx++;
        }

        if(ex != null) {
            exampleRow = ex;
        }
    }

    public static final class Column
    {
        private String name;
        private FacetFinderSPI finder;
        private MimeTypeRendererSPI renderer;
        private FacetFinderSPI.ColumnID colID;
        private boolean horizontalSrollable;
        private boolean verticalScrollable;

        public Column()
        {
        }

        public Column(String name,
                      FacetFinderSPI finder,
                      MimeTypeRendererSPI renderer,
                      FacetFinderSPI.ColumnID colID,
                      boolean rowSrollable,
                      boolean colScrollable)
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

        public FacetFinderSPI.ColumnID getColID()
        {
            return colID;
        }

        public void setColID(FacetFinderSPI.ColumnID colID)
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

    private class ColumnListener implements MouseListener
    {
        private final Column col;
        private final JLabel title;
        private final int indx;

        public ColumnListener(Column col, JLabel title, int indx)
        {
            this.col = col;
            this.title = title;
            this.indx = indx;
        }

        private DataThing getCurrent()
        {
            if(exampleRow == null) {
                return null;
            } else {
                return (DataThing) exampleRow.get(indx);
            }
        }

        public void mouseClicked(MouseEvent e) { process(e); }
        public void mousePressed(MouseEvent e) { process(e); }
        public void mouseReleased(MouseEvent e) { process(e); }
        public void mouseEntered(MouseEvent e) { process(e); }
        public void mouseExited(MouseEvent e) { process(e); }

        private void process(MouseEvent e)
        {
            if(e.isPopupTrigger()) {
                JPopupMenu popup = new JPopupMenu("Edit Column " + col.getName());

                JMenuItem changeName = new JMenuItem(new RenameColumn());
                popup.add(changeName);

                JMenu view = new JMenu("View");
                populateView(view);
                popup.add(view);

                JMenuItem edit = new JMenuItem(new EditColumn());
                popup.add(edit);

                JMenu add = new JMenu("add");
                populateAdd(add);
                popup.add(add);

                JMenuItem remove = new JMenuItem(new RemoveColumn());
                popup.add(remove);

                popup.show(title, e.getX(), e.getY());
            }
        }

        private void populateAdd(JMenu add)
        {
            for (Iterator i = finders.getFinders(exampleThing).iterator();
                 i.hasNext();) {
                final FacetFinderSPI spi = (FacetFinderSPI) i.next();
                DataThing current = getCurrent();
                List possibles = new ArrayList();
                possibles.add(spi.newColumn(current));
                possibles.addAll(spi.getStandardColumns(current));
                for(Iterator pi = possibles.iterator(); pi.hasNext(); ) {
                  final FacetFinderSPI.ColumnID column
                          = (FacetFinderSPI.ColumnID) pi.next();
                    if (column != null) {
                        JMenuItem item = new JMenuItem(
                                new AbstractAction(column.getName())
                        {
                            public void actionPerformed(ActionEvent e)
                            {
                                List columns = getColumns();
                                columns.add(new Column(column.getName(), spi, null,
                                                       column, true, true));
                                setColumns(columns);
                            }
                        });
                        add.add(item);
                    }
                }
            }
        }

        private void populateView(JMenu view)
        {
            DataThing current = getCurrent();
            for(Iterator i = renderers.getRenderers(current).iterator();
                i.hasNext(); )
            {
                final MimeTypeRendererSPI renderer
                        = (MimeTypeRendererSPI) i.next();
                JMenuItem choser = new JMenuItem(new AbstractAction(
                        renderer.getName(),
                        renderer.getIcon(renderers, current))
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        col.setRenderer(renderer);
                        resizeAndValidate();
                    }
                });
                view.add(choser);
            }
        }

        private class RemoveColumn extends AbstractAction
        {
            public RemoveColumn()
            {
                super("remove");
            }

            public void actionPerformed(ActionEvent e)
            {
                List cols = getColumns();
                cols.remove(col);
                setColumns(cols);
            }
        }

        private class EditColumn extends AbstractAction
        {
            public EditColumn()
            {
                super("edit");
            }

            public void actionPerformed(ActionEvent e)
            {
                DataThing current = exampleThing;
                Component editor = col.getColID().getCustomiser(current);
                if(editor == null) {
                    LOG.info("No editor for " + col.getColID());
                    return;
                }

                Component owner = FacetsTable.this;
                while(
                        !(owner instanceof Frame) &&
                        !(owner instanceof Dialog) &&
                        !(owner == null))
                {
                    owner = owner.getParent();
                }

                JOptionPane.showMessageDialog(
                        FacetsTable.this,
                        editor,
                        "Configure " + col.getName(),
                        JOptionPane.DEFAULT_OPTION);
                resizeAndValidate();
            }
        }

        private class RenameColumn extends AbstractAction
        {
            public RenameColumn()
            {
                super("rename");
            }

            public void actionPerformed(ActionEvent e)
            {
                String newName = JOptionPane.showInputDialog(
                        "New name", col.getName());
                col.setName(newName);
                resizeAndValidate(); // todo: this is heavyweight
            }
        }
    }
}
