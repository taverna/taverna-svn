package net.sf.taverna.t2.activities.matlab.reference;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.taverna.t2.reference.AbstractExternalReference;
import net.sf.taverna.t2.reference.ReferenceContext;
import java.io.ByteArrayInputStream;
import net.sf.taverna.matlabactivity.matserver.api.MatArray;
import net.sf.taverna.t2.reference.DereferenceException;
import net.sf.taverna.t2.reference.ReferencedDataNature;
import net.sf.taverna.t2.reference.ValueCarryingExternalReference;

/**
 * XXX this reference moves data around!!!
 * @author petarj
 */
public class MatArrayReference extends AbstractExternalReference implements 
        ValueCarryingExternalReference<MatArray> {

    private String contents;

    public InputStream openStream(ReferenceContext context) {
        System.err.println(">>>>MatArrayReference->openStream");
        try {
            return new ByteArrayInputStream(contents.getBytes(getCharset()));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(MatArrayReference.class.getName()).
                    log(Level.SEVERE, null, ex);
            throw new DereferenceException(ex);
        }
    }

    public String getContents() {
        System.err.println(">>>>MatArrayReference->getContents");
        return contents;
    }

    public void setContents(String contents) {
        System.err.println(">>>>MatArrayReference->setContents");
        this.contents = contents;
    }

    @Override
    public ReferencedDataNature getDataNature() {
        System.err.println(">>>>MatArrayReference->getDataNature");
        return ReferencedDataNature.TEXT;
    }

    @Override
    public String getCharset() {
        System.err.println(">>>>MatArrayReference->getCharset");
        return "UTF-8";
    }

    @Override
    public float getResolutionCost() {
        System.err.println(">>>>MatArrayReference->getResolutionCost");
        return 0.1f; //TODO revise this
    }

    public Class<MatArray> getValueType() {
        System.err.println(">>>>MatArrayReference->getValueType");
        return MatArray.class;
    }

    public MatArray getValue() {
        System.err.println(">>>>MatArrayReference->getValue");
        XStream xstream = new XStream(new JettisonMappedXmlDriver());
        return (MatArray) xstream.fromXML(this.contents);
    }
}
