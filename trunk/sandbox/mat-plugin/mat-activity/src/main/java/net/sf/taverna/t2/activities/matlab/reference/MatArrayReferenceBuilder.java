/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.taverna.t2.activities.matlab.reference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import net.sf.taverna.t2.reference.ExternalReferenceBuilderSPI;
import net.sf.taverna.t2.reference.ExternalReferenceConstructionException;
import net.sf.taverna.t2.reference.ReferenceContext;

/**
 *
 * @author user
 */
public class MatArrayReferenceBuilder implements
        ExternalReferenceBuilderSPI<MatArrayReference> {

    public MatArrayReference createReference(InputStream byteStream,
            ReferenceContext context) {
        try {
            String contents = StreamToMatArrayConverter.readFile(new BufferedReader(new InputStreamReader(
                    byteStream)));
            MatArrayReference ref = new MatArrayReference();
            ref.setContents(contents);
            return ref;
        } catch (IOException ex) {
            throw new ExternalReferenceConstructionException(ex);
        }
    }

    public Class<MatArrayReference> getReferenceType() {
        return MatArrayReference.class;
    }

    public boolean isEnabled(ReferenceContext context) {
        return true;
    }

    public float getConstructionCost() {
        return 0.1f;
    }
}
