package net.sf.taverna.t2.activities.matlab.reference;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import net.sf.taverna.matlabactivity.matserver.api.MatArray;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.ReferenceContext;
import net.sf.taverna.t2.reference.ValueToReferenceConversionException;
import net.sf.taverna.t2.reference.ValueToReferenceConverterSPI;

/**
 *
 * @author petarj
 */
public class MatArrayToMatArrayReference implements ValueToReferenceConverterSPI {

    public boolean canConvert(Object o, ReferenceContext context) {
        System.err.println(">>>>MatArrayToMatArrayReference->canConvert");
        return (o instanceof MatArray);
    }

    public ExternalReferenceSPI convert(Object o, ReferenceContext context)
            throws ValueToReferenceConversionException {
        System.err.println(">>>>MatArrayToMatArrayReference->convert");
        MatArrayReference reference = new MatArrayReference();
        MatArray ma = (MatArray) o;
        XStream xstream = new XStream(new JettisonMappedXmlDriver());
        reference.setContents(xstream.toXML(ma));
        return reference;
    }
}
