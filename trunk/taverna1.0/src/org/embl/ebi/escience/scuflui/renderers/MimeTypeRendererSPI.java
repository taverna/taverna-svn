package org.embl.ebi.escience.scuflui.renderers;

import org.embl.ebi.escience.baclava.DataThing;

import javax.swing.Icon;
import javax.swing.JComponent;

/**
 * SPI for rendering a data thing.
 *
 * <h2>Users</h2>
 *
 * Instances of this interface will normaly be obtained from methods on
 * MimeTypeRendererRegistry. You can then use methods on a particular instance
 * to get its icon and name, a Component for rendering data, and query if it
 * is capable of rendering particular data. It is a good idea to wrap calls to
 * these methods in a try/catch block for common runtime exceptions like
 * NullPointerException, as each SPI is implemented independantly of the main
 * library, and may fail in exciting and unpredictable ways.
 *
 * <h2>Implementors</h2>
 *
 * Each jar providing implementations of this SPI should list them in
 * <code>META-INF/services/org.embl.ebi.escience.scuflui.renderers.MimeTypeRendererSPI</code>.
 * <p>
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
public interface MimeTypeRendererSPI {
    /**
     * Return true if this SPI can handle the given object with the given mime
     * type, false otherwise.
     *
     * @param renderers  the MimeTypeRendereRegistry to look up sibling
     *      renderers
     * @param dataThing the object to render
     * @return true if we can handle the mime type
     */
    public boolean canHandle(MimeTypeRendererRegistry renderers,
                             DataThing dataThing);

    /**
     * Return a JComponent that renders this object that proports to have a
     * particular mime type. If canHandle() returns true, then getComponent()
     * must not return null.
     *
     * @param renderers  the MimeTypeRendereRegistry to look up sibling
     *      renderers
     * @param dataThing the object to render
     * @return a JComponent for displaying the object, or null
     */
    public JComponent getComponent(MimeTypeRendererRegistry renderers,
                                   DataThing dataThing);

    /**
     * A human-readable name for this SPI.
     *
     * @return the name
     */
    public String getName();

    /**
     * An icon that can be used to identify this SPI.
     *
     * @param renderers  the MimeTypeRendereRegistry to look up sibling
     *      renderers
     * @param dataThing the object to render
     * @return an appropreate icon, or null if this SPI doesn't have an icon
     */
    public Icon getIcon(MimeTypeRendererRegistry renderers,
                        DataThing dataThing);
}
