package org.embl.ebi.escience.scuflui.facets;

import org.embl.ebi.escience.baclava.DataThing;

import java.util.Set;

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
     * this API should make a copy of this if they want to edit it.
     *
     * @return a Set (never null) containing the standard columns
     */
    public Set getStandardColumns();

    /**
     * Indicate if this facet finder can return data for this column.
     *
     * @param colID  the Object identifying the column
     * @return true if the colID is known by this facet finder, false otherwise
     */
    public boolean hasColumn(Object colID);

    /**
     * Get the value associated with a particular column of a data object.
     *
     * @param dataThing     the object to decompose
     * @param colID         the column identifier
     * @return  a DataThing holding the value of that facet, or null if that
     *      column ID could not be fetched for dataThing
     */
    public DataThing getFacet(DataThing dataThing, Object colID);
    public String getName();
}
