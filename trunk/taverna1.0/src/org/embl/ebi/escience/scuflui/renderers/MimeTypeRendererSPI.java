package org.embl.ebi.escience.scuflui.renderers;

import javax.swing.*;

/**
 * SPI for rendering data by mime type.
 * <p>
 * Each jar providing implementations of this SPI should list them in
 * <code>META-INF/services/org.embl.ebi.escience.scuflui.renderers.MimeTypeRendererSPI</code>
 *
 * @author Matthew Pocock
 */
public interface MimeTypeRendererSPI {
    /**
     * Return true if this SPI can handle the given object with the given mime
     * type, false otherwise.
     *
     * @param userObject the object to render
     * @param mimetypes
     * @return true if we can handle the mime type
     */
    public boolean canHandle(Object userObject, String mimetypes);

    /**
     * Return a JComponent that renders this object that proports to have a
     * particular mime type. If canHandle() returns true, then getComponent()
     * must not return null.
     *
     * @param userObject the Object to render
     * @param mimetypes the mime type this object is meant to have
     * @return a JComponent for displaying the object, or null
     */
    public JComponent getComponent(Object userObject, String mimetypes);

    /**
     * A human-readable name for this SPI.
     *
     * @return the name
     */
    public String getName();
}
