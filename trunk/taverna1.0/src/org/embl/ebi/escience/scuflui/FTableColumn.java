package org.embl.ebi.escience.scuflui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.embl.ebi.escience.scuflui.facets.FacetFinderSPI;
import org.embl.ebi.escience.scuflui.renderers.RendererSPI;

/**
 * A Java Bean that represents the data associated with a single column in a
 * FacetsTable.
 * 
 * @author Matthew Pocock
 */
public final class FTableColumn {
	private final PropertyChangeSupport pcSupport;

	private final PropertyChangeListener forwarder;

	private String name;

	private FacetFinderSPI finder;

	private RendererSPI renderer;

	private FacetFinderSPI.ColumnID colID;

	private boolean horizontalSrollable;

	private boolean verticalScrollable;

	public FTableColumn() {
		pcSupport = new PropertyChangeSupport(this);
		forwarder = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				pcSupport.firePropertyChange(new PropertyChangeEvent(
						FTableColumn.this, null, null, null));
			}
		};
	}

	public FTableColumn(String name, FacetFinderSPI finder,
			RendererSPI renderer, FacetFinderSPI.ColumnID colID,
			boolean rowSrollable, boolean colScrollable) {
		this();

		this.name = name;
		this.finder = finder;
		this.renderer = renderer;
		setColID(colID);
		this.horizontalSrollable = rowSrollable;
		this.verticalScrollable = colScrollable;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		PropertyChangeEvent pce = new PropertyChangeEvent(this, "name",
				this.name, name);
		this.name = name;
		pcSupport.firePropertyChange(pce);
	}

	public FacetFinderSPI getFinder() {
		return finder;
	}

	public void setFinder(FacetFinderSPI finder) {
		PropertyChangeEvent pce = new PropertyChangeEvent(this, "finder",
				this.finder, finder);
		this.finder = finder;
		pcSupport.firePropertyChange(pce);
	}

	public RendererSPI getRenderer() {
		return renderer;
	}

	public void setRenderer(RendererSPI renderer) {
		PropertyChangeEvent pce = new PropertyChangeEvent(this, "renderer",
				this.renderer, renderer);
		this.renderer = renderer;
		pcSupport.firePropertyChange(pce);
	}

	public FacetFinderSPI.ColumnID getColID() {
		return colID;
	}

	public void setColID(FacetFinderSPI.ColumnID colID) {
		PropertyChangeEvent pce = new PropertyChangeEvent(this, "colID",
				this.colID, colID);
		if (this.colID != null) {
			this.colID.removePropertyChangeListener(forwarder);
		}
		this.colID = colID;
		if (this.colID != null) {
			this.colID.addPropertyChangeListener(forwarder);
		}
		pcSupport.firePropertyChange(pce);
	}

	public boolean isHorizontalSrollable() {
		return horizontalSrollable;
	}

	public void setHorizontalSrollable(boolean horizontalSrollable) {
		PropertyChangeEvent pce = new PropertyChangeEvent(this,
				"horizontalScrollable", new Boolean(this.horizontalSrollable),
				new Boolean(horizontalSrollable));
		this.horizontalSrollable = horizontalSrollable;
		pcSupport.firePropertyChange(pce);
	}

	public boolean isVerticalScrollable() {
		return verticalScrollable;
	}

	public void setVerticalScrollable(boolean verticalScrollable) {
		PropertyChangeEvent pce = new PropertyChangeEvent(this,
				"verticalScrollable", new Boolean(this.verticalScrollable),
				new Boolean(verticalScrollable));
		this.verticalScrollable = verticalScrollable;
		pcSupport.firePropertyChange(pce);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcSupport.removePropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		pcSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		pcSupport.removePropertyChangeListener(propertyName, listener);
	}
}
