package org.embl.ebi.escience.scuflui.facets;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.util.Set;

import org.embl.ebi.escience.baclava.DataThing;

/**
 * SPI for decomposing opaque data into facets.
 * <p>
 * Each jar providing implementations of this SPI should list them in
 * <code>META-INF/services/org.embl.ebi.escience.scuflui.facets.FacetFinderSPI</code>.
 * If a particular SPI needs extra information, it should be in a resource in
 * the same location as the .class file, and prefixed by the local class name,
 * so that it could be retrieved as:
 * <pre>
 * getClass().getClassLoader().getResourceAsStream(
 *     getClass().getName().replace('.', '/') + postFix );
 * </pre>
 *
 * or alternatively
 *
 * <pre>
 * getClass().getResourceAsStream(getClass().getName().substring(
 *     getClass().getPackage().toString() + 1) + postFix );
 * </pre>
 *
 * @author Matthew Pocock
 */
public interface FacetFinderSPI
{
    /**
     * Indicate if this facet finder can decompose this DataThing.
     *
     * @param dataThing  the object to decompose
     * @return true if it can, false otherwise
     */
    public boolean canMakeFacets(DataThing dataThing);

    /***
     * Get a Set of columns this facet finder can use on any data.
     * The returned Set should be immutable, and should not change. Users of
     * this API should make a copy of this if they want to edit it. Calling
     * mutators on the column ID should not affect the columns returned
     * by previous or future invocations.
     *
     * @param dataThing  the dataThing to get standard columns for, or null if
     *      default columns should be returned
     * @return a Set (never null) containing the standard columns
     */
    public Set getStandardColumns(DataThing dataThing);

    /**
     * Get a new, independant column for a data item. Call this method to get
     * a column ID that can then be customised.
     *
     * @param dataThing  the data item to work with
     * @return  a new column ID
     */
    public ColumnID newColumn(DataThing dataThing);

    /**
     * Indicate if this facet finder can return data for this column.
     *
     * @param colID  the ColumnID identifying the column
     * @return true if the colID is known by this facet finder, false otherwise
     */
    public boolean hasColumn(ColumnID colID);

    /**
     * Get the value associated with a particular column of a data object.
     *
     * @param dataThing     the object to decompose
     * @param colID         the column identifier
     * @return  a DataThing holding the value of that facet, or null if that
     *      column ID could not be fetched for dataThing
     */
    public DataThing getFacet(DataThing dataThing, ColumnID colID);

    /**
     * Get a human readable name for this facetiser.
     *
     * @return the name
     */
    public String getName();

    public static interface ColumnID
    {
        /**
         * Get a component able to customise this column, given a hint object,
         * or null if the column is not customisable.
         *
         * @param dataThing  an example DataThing this column will act upon
         * @return  a Component suitable for editing this column
         */
        public Component getCustomiser(DataThing dataThing);

        /**
         * Get a human readable name for this column.
         * This may change as column properties change.
         *
         * @return  a name for this column
         */
        public String getName();

        /**
         * Add a property change listener that will be informed whenever any
         * property of the column alters that could affect the data the column
         * would return.
         *
         * @param listener  the PropertyChangeListener to register
         */
        public void addPropertyChangeListener(
                PropertyChangeListener listener);

        /**
         * Remove a property change listener.
         *
         * @param listener  the PropertyChangeListener to unregister
         */
        public void removePropertyChangeListener(
                PropertyChangeListener listener);

        public void addPropertyChangeListener(
                String propertyName,
                PropertyChangeListener listener);

        public void removePropertyChangeListener(
                String propertyName,
                PropertyChangeListener listener);
    }
}
