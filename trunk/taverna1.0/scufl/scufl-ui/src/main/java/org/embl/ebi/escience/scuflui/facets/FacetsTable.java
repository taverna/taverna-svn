package org.embl.ebi.escience.scuflui.facets;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scuflui.renderers.RendererRegistry;
import org.embl.ebi.escience.scuflui.spi.FacetFinderSPI;
import org.embl.ebi.escience.scuflui.spi.RendererSPI;

/**
 * A tabular display of data using FacetFinders to decompose the information
 * into columns.
 * 
 * @author Matthew Pocock
 */
public class FacetsTable extends JPanel {
	private static final Logger LOG = Logger.getLogger(FacetsTable.class);

	private static final int H_PAD = 2;

	private static final int V_PAD = 2;

	private final List headings; // List<width as int, heading as JComponent>

	private final List columns; // List<column as List<cell as JComponent>>

	private int headingHeight; // the height of the highest heading

	private int[] rowHeights; // the height of each data row

	boolean isInScrollPane;

	private JComponent columnHeaders; // the column header component

	private JComponent table; // the actual table component

	private final FTableColumnModelListener nsync;

	private FTableColumnModel columnModel;

	private DataThing dataThing;

	private FacetFinderRegistry finders;

	private RendererRegistry renderers;

	private int rows;

	private transient DataThing exampleThing;

	private transient List exampleRow;

	private void invariant() {
		assert headings.size() == columns.size() : "Must have the same headings and columns in the table: "
				+ headings.size() + ":" + columns.size();
		assert headings.size() == columnModel.getColumnCount() : "Must have the same headings as columns in the model"
				+ headings.size() + ":" + columnModel.getColumnCount();
		assert exampleRow == null || exampleRow.size() == headings.size() : "Must have the right number of examples"
				+ exampleRow.size() + ":" + headings.size();
	}

	public FacetsTable() {
		super(new BorderLayout(), true);
		isInScrollPane = false;
		addHierarchyListener(new ScrollPaneChanger());
		nsync = new NSynch();

		this.headings = new ArrayList();
		this.columns = new ArrayList();
		this.columnModel = new FTableColumnModel();
		this.finders = FacetFinderRegistry.instance();
		this.renderers = RendererRegistry.instance();
		setOpaque(true); // required to make sure bits of the rendering
		// aren't
		// left behind
		setBackground(Color.WHITE);
		resizeAndValidate();
	}

	public FacetsTable(DataThing theDataThing, FacetFinderRegistry finders,
			RendererRegistry renderers, FTableColumnModel columnModel) {
		this();
		this.dataThing = theDataThing;
		if (finders != null) {
			this.finders = finders;
		}
		if (renderers != null) {
			this.renderers = renderers;
		}
		if (columnModel != null) {
			setColumnModel(columnModel);
		}
		setBackground(Color.WHITE);
		setOpaque(true); // required to make sure bits of the rendering
		// aren't
		// left behind
		resizeAndValidate();
	}

	public FacetsTable(DataThing theDataThing, FacetFinderRegistry finders,
			RendererRegistry renderers) {
		this(theDataThing, finders, renderers, null);
	}

	public FTableColumnModel getColumnModel() {
		return columnModel;
	}

	public void setColumnModel(FTableColumnModel columnModel) {
		if (this.columnModel != null) {
			this.columnModel.removeFTableColumnModelListener(nsync);
		}
		this.columnModel = columnModel;
		this.columnModel.addFTableColumnModelListener(nsync);

		resizeAndValidate();
	}

	public DataThing getDataThing() {
		return dataThing;
	}

	public void setDataThing(DataThing dataThing) {
		this.dataThing = dataThing;
		resizeAndValidate();
	}

	public FacetFinderRegistry getFinders() {
		return finders;
	}

	public void setFinders(FacetFinderRegistry finders) {
		if (finders == null) {
			throw new NullPointerException("Can't set finder registry to null");
		}

		this.finders = finders;
	}

	public RendererRegistry getRenderers() {
		return renderers;
	}

	public void setRenderers(RendererRegistry renderers) {
		if (renderers == null) {
			throw new NullPointerException(
					"Can't set renderer registry to null");
		}

		this.renderers = renderers;
	}

	protected void configureScrolling() {
		removeAll();
		if (isInScrollPane) {
			add(table, BorderLayout.CENTER);
			JScrollPane pane = (JScrollPane) getParent().getParent();
			pane.getViewport().setBackground(Color.WHITE);
			pane.setViewportView(FacetsTable.this);
			pane.setColumnHeaderView(columnHeaders);
		} else {
			this.add(columnHeaders, BorderLayout.NORTH);
			this.add(table, BorderLayout.CENTER);
		}
	}

	private Iterator makeRowIterator() {
		Iterator rowIt = null;

		if (dataThing == null) {
			rowIt = Collections.EMPTY_LIST.iterator();
			rows = 0;
		} else if (dataThing.getDataObject() instanceof Collection) {
			rowIt = dataThing.childIterator();
			rows = ((Collection) dataThing.getDataObject()).size();
		} else {
			rowIt = Collections.singleton(dataThing).iterator();
			rows = 1;
		}

		return rowIt;
	}

	protected void resizeAndValidate() {
		LOG.info("resizeAndValidate: redoing layout: " + dataThing);
		// this is bruit force - we will probably want to optimize this for
		// incremental changes to the model

		headings.clear();
		columns.clear();

		exampleThing = null;
		exampleRow = null;

		columnHeaders = new JPanel(null);
		table = new JPanel(null);
		table.setBackground(Color.WHITE);
		// make headings and also allocate column lists
		for (Iterator ci = columnModel.columnIterator(); ci.hasNext();) {
			FTableColumn column = (FTableColumn) ci.next();
			JComponent heading = makeHeading(column);
			LOG.info("heading dimensions: " + heading.getPreferredSize());
			headings.add(new Header(heading, (int) heading.getPreferredSize()
					.getWidth()));
			columns.add(new ArrayList());
			columnHeaders.add(heading);
		}

		LOG.info("resizeAndValidate: adding all data rows");

		Iterator rowIt = makeRowIterator();
		rowHeights = new int[rows];

		for (int r = 0; r < rows; r++) {
			DataThing part = (DataThing) rowIt.next();

			List ex = null;
			if (exampleRow == null) {
				ex = new ArrayList();
			}

			if (exampleThing == null) {
				exampleThing = part;
			}

			for (int c = 0; c < columnModel.getColumnCount(); c++) {
				FTableColumn ftCol = columnModel.getColumn(c);
				List col = (List) columns.get(c);
				DataThing dataObj = ftCol.getFinder().getFacet(part,
						ftCol.getColID());

				if (ex != null) {
					ex.add(dataObj);
				}

				JComponent cmp = makeCell(dataObj, ftCol);
				LOG.info("cell size: " + cmp.getPreferredSize());
				col.add(cmp);
				table.add(cmp);
			}

			if (ex != null) {
				exampleRow = ex;
			}
		}
		redoGeometry();
		redoLayout();
		configureScrolling();

		LOG.info("resizeAndValidate: done");
	}

	private void redoGeometry() {
		invariant();

		for (int i = 0; i < rowHeights.length; i++) {
			rowHeights[i] = 0;
		}
		headingHeight = 0;

		LOG.info("rows: " + rowHeights.length);
		LOG.info("columns: " + columnModel.getColumnCount());

		for (int i = 0; i < columnModel.getColumnCount(); i++) {
			Header header = (Header) headings.get(i);
			List column = (List) columns.get(i);

			assert column.size() == rowHeights.length : "Row heights should match column size";

			Dimension hd = header.heading.getPreferredSize();
			headingHeight = (int) Math.max(headingHeight, hd.getHeight());
			header.width = (int) hd.getWidth();

			for (int r = 0; r < column.size(); r++) {
				JComponent cell = (JComponent) column.get(r);
				Dimension dim = cell.getPreferredSize();
				header.width = (int) Math.max(header.width, dim.getWidth());
				rowHeights[r] = (int) Math.max(rowHeights[r], dim.getHeight());
			}
		}
	}

	private void redoLayout() {
		invariant();

		int xPos = H_PAD;
		int yPos = V_PAD;
		for (int i = 0; i < headings.size(); i++) {
			// header stuff
			Header header = (Header) headings.get(i);
			Component cmp = header.heading;
			cmp.setSize(header.width, headingHeight);
			cmp.setLocation(xPos, 0);

			// column stuff
			List col = (List) columns.get(i);
			assert col.size() == rowHeights.length : "Row heights should match column size";

			yPos = 0;
			for (int r = 0; r < col.size(); r++) {
				Component cell = (Component) col.get(r);
				int height = rowHeights[r];
				cell.setSize(header.width, height);
				cell.setLocation(xPos, yPos);
				yPos += height + V_PAD;
			}

			// move on
			xPos += header.width + H_PAD;
		}

		// set container sizes
		Dimension headersSize = new Dimension(xPos, headingHeight);
		columnHeaders.setMinimumSize(headersSize);
		columnHeaders.setPreferredSize(headersSize);
		columnHeaders.setMaximumSize(headersSize);
		columnHeaders.setSize(headersSize);
		Dimension tableSize = new Dimension(xPos, yPos);
		table.setMinimumSize(tableSize);
		table.setPreferredSize(tableSize);
		table.setMaximumSize(tableSize);
		table.setSize(tableSize);

		LOG.info("set table dims to " + tableSize);
		validate();
		LOG.info("table size is now " + table.getSize());
	}

	private JComponent makeHeading(FTableColumn col) {
		JComponent heading = new JLabel(col.getName());
		heading.addMouseListener(new ColumnListener(col, heading));
		return heading;
	}

	private JComponent makeCell(DataThing dataObj, FTableColumn ftCol) {
		JComponent cmp = null;
		if (dataObj != null) {
			RendererSPI renderer = ftCol.getRenderer();
			if (renderer != null) {
				try {
					cmp = renderer.getComponent(renderers, dataObj);
				} catch (Exception e) {
					LOG.error("Problem creating component from renderer", e);
				}
			}
			if (cmp == null) {
				for (Iterator ri = renderers.getRenderers(dataObj).iterator(); cmp == null
						&& ri.hasNext();) {
					renderer = (RendererSPI) ri.next();
					if (renderer.isTerminal()) {
						try {
							cmp = renderer.getComponent(renderers, dataObj);
						} catch (Exception e) {
							LOG.error(
									"Problem creating component from renderer",
									e);
						}
					}
				}
			}
		}

		if (cmp == null) {
			cmp = new JLabel("No Renderer");
		}

		return cmp;
	}

	private class ColumnListener implements MouseListener {
		private final FTableColumn col;

		private final JComponent owner;

		public ColumnListener(FTableColumn col, JComponent owner) {
			this.col = col;
			this.owner = owner;
		}

		private DataThing getCurrent() {
			if (exampleRow == null) {
				return null;
			} else {
				invariant();
				return (DataThing) exampleRow.get(columnModel
						.getColumnIndex(col));
			}
		}

		public void mouseClicked(MouseEvent e) {
			process(e);
		}

		public void mousePressed(MouseEvent e) {
			process(e);
		}

		public void mouseReleased(MouseEvent e) {
			process(e);
		}

		public void mouseEntered(MouseEvent e) {
			process(e);
		}

		public void mouseExited(MouseEvent e) {
			process(e);
		}

		private void process(MouseEvent e) {
			if (e.isPopupTrigger()) {
				JPopupMenu popup = new JPopupMenu("Edit Column "
						+ col.getName());

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

				popup.show(owner, e.getX(), e.getY());
			}
		}

		private void populateAdd(JMenu add) {
			for (Iterator i = finders.getFinders(exampleThing).iterator(); i
					.hasNext();) {
				final FacetFinderSPI spi = (FacetFinderSPI) i.next();
				DataThing current = getCurrent();
				List possibles = new ArrayList();
				possibles.add(spi.newColumn(current));
				possibles.addAll(spi.getStandardColumns(current));
				for (Iterator pi = possibles.iterator(); pi.hasNext();) {
					final FacetFinderSPI.ColumnID column = (FacetFinderSPI.ColumnID) pi
							.next();
					if (column != null) {
						JMenuItem item = new JMenuItem(new AbstractAction(
								column.getName()) {
							public void actionPerformed(ActionEvent e) {
								columnModel.addColumn(new FTableColumn(column
										.getName(), spi, null, column, true,
										true));
							}
						});
						add.add(item);
					}
				}
			}
		}

		private void populateView(JMenu view) {
			DataThing current = getCurrent();
			for (Iterator i = renderers.getRenderers(current).iterator(); i
					.hasNext();) {
				final RendererSPI renderer = (RendererSPI) i.next();
				JMenuItem choser = new JMenuItem(new AbstractAction(renderer
						.getName(), renderer.getIcon(renderers, current)) {
					public void actionPerformed(ActionEvent e) {
						col.setRenderer(renderer);
					}
				});
				view.add(choser);
			}
		}

		private class RemoveColumn extends AbstractAction {
			public RemoveColumn() {
				super("remove");
			}

			public void actionPerformed(ActionEvent e) {
				getColumnModel().removeColumn(col);
			}
		}

		private class EditColumn extends AbstractAction {
			public EditColumn() {
				super("edit");
			}

			public void actionPerformed(ActionEvent e) {
				DataThing current = exampleThing;
				Component editor = col.getColID().getCustomiser(current);
				if (editor == null) {
					LOG.info("No editor for " + col.getColID());
					return;
				}

				Component owner = FacetsTable.this;
				while (!(owner instanceof Frame) && !(owner instanceof Dialog)
						&& !(owner == null)) {
					owner = owner.getParent();
				}

				JOptionPane.showMessageDialog(FacetsTable.this, editor,
						"Configure " + col.getName(),
						JOptionPane.DEFAULT_OPTION);
			}
		}

		private class RenameColumn extends AbstractAction {
			public RenameColumn() {
				super("rename");
			}

			public void actionPerformed(ActionEvent e) {
				String newName = JOptionPane.showInputDialog("New name", col
						.getName());
				col.setName(newName);
			}
		}
	}

	private static final class Header {
		public int width;

		public Component heading;

		public Header(Component heading) {
			this(heading, 0);
		}

		public Header(Component heading, int width) {
			this.heading = heading;
			this.width = width;
		}
	}

	/**
	 * This is an ugly hack to make sure that when we are in a ScrollPannel, we
	 * put the headings at the top of the panel in the unscrolling bit, and when
	 * we are stand-alone, the heading is in a normal place.
	 */
	private class ScrollPaneChanger implements HierarchyListener {
		/**
		 * Prevent us from infinite-looping by responding to events we cause.
		 */
		boolean active = false;

		public void hierarchyChanged(HierarchyEvent e) {
			if (!active) {
				try {
					active = true;
					if (e.getChangeFlags() == HierarchyEvent.PARENT_CHANGED
							&& e.getComponent() == FacetsTable.this) {
						boolean inSP = false;
						Component p = getParent();
						if (p != null) {
							p = p.getParent();
						}
						if (p instanceof JScrollPane) {
							inSP = true;
						}

						if (inSP != isInScrollPane) {
							isInScrollPane = inSP;
							configureScrolling();
						}
					}
				} finally {
					active = false;
				}
			}
		}
	}

	private class NSynch implements FTableColumnModelListener {
		public void columnAdded(FTableColumnModelEvent evt) {
			int indx = evt.getToIndex();
			FTableColumn col = columnModel.getColumn(indx);
			Header header = new Header(makeHeading(col));
			headings.add(indx, header);
			List components = new ArrayList();

			boolean first = true;
			for (Iterator rowIt = makeRowIterator(); rowIt.hasNext();) {
				DataThing item = (DataThing) rowIt.next();
				DataThing dt = col.getFinder().getFacet(item, col.getColID());
				if (first) {
					exampleRow.add(indx, dt);
					first = false;
				}
				Component cell = makeCell(dt, col);
				components.add(cell);
				table.add(cell);
			}
			columnHeaders.add(header.heading);
			columns.add(indx, components);

			redoGeometry();
			redoLayout();
		}

		public void columnRemoved(FTableColumnModelEvent evt) {
			int indx = evt.getFromIndex();
			Header header = (Header) headings.remove(indx);
			columnHeaders.remove(header.heading);
			List column = (List) columns.get(indx);
			for (Iterator i = column.iterator(); i.hasNext();) {
				Component cmp = (Component) i.next();
				table.remove(cmp);
			}

			columns.remove(indx);
			if (exampleRow != null) {
				exampleRow.remove(indx);
			}

			redoGeometry();
			redoLayout();
		}

		public void columnMoved(FTableColumnModelEvent evt) {
			int from = evt.getFromIndex();
			int to = evt.getToIndex();

			headings.add(to, headings.remove(from));
			columns.add(to, columns.remove(from));
			if (exampleRow != null) {
				exampleRow.add(to, exampleRow.remove(from));
			}

			redoGeometry();
			redoLayout();
		}

		public void columnChanged(FTableColumnModelEvent evt) {
			int indx = evt.getToIndex();
			FTableColumn col = columnModel.getColumn(indx);
			PropertyChangeEvent cause = evt.getCause();
			LOG.info("change: " + cause.getPropertyName());
			if ("name".equals(cause.getPropertyName())) {
				Header header = (Header) headings.remove(indx);
				columnHeaders.remove(header.heading);
				JComponent heading = makeHeading(columnModel.getColumn(indx));
				Header newHeader = new Header(heading);
				headings.add(indx, newHeader);
				columnHeaders.add(newHeader.heading);

				redoGeometry();
				redoLayout();
			} else {
				List column = (List) columns.get(indx);
				for (Iterator i = column.iterator(); i.hasNext();) {
					Component cmp = (Component) i.next();
					table.remove(cmp);
				}

				if (exampleRow != null) {
					exampleRow.remove(indx);
				}

				column.clear();

				boolean fixExampleRow = exampleRow != null;
				for (Iterator rowIt = makeRowIterator(); rowIt.hasNext();) {
					DataThing item = (DataThing) rowIt.next();
					DataThing dt = col.getFinder().getFacet(item,
							col.getColID());
					if (fixExampleRow) {
						exampleRow.add(indx, dt);
						fixExampleRow = false;
					}
					Component cell = makeCell(dt, col);
					column.add(cell);
					table.add(cell);
				}

				redoGeometry();
				redoLayout();
			}
		}
	}
}
