package net.sf.taverna.t2.activities.matlab.reference;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.taverna.matlabactivity.matserver.api.MatArray;
import net.sf.taverna.t2.reference.StreamToValueConverterSPI;

/**
 *
 * @author petarj
 */
public class StreamToMatArrayConverter implements
        StreamToValueConverterSPI<MatArray> {

    private static final int END_OF_FILE = -1;
    private static final int CHUNK_SIZE = 4096;

    static String readFile(Reader reader) throws IOException {
        StringBuffer buffer = new StringBuffer();
        char[] chunk = new char[CHUNK_SIZE];
        int character;
        while ((character = reader.read(chunk)) != END_OF_FILE) {
            buffer.append(chunk, 0, character);
        }
        return buffer.toString();
    }

    public Class<MatArray> getPojoClass() {
        System.err.println(">>>>StreamToMatArrayConverter->getPojoClass");
        return MatArray.class;
    }

    public MatArray renderFrom(InputStream stream) {
        System.err.println(">>>>StreamToMatArrayConverter->renderFrom");
        BufferedReader in = new BufferedReader(new InputStreamReader(stream));
        try {
            String json = readFile(in);
            XStream xstream = new XStream(new JettisonMappedXmlDriver());
            MatArray ma = (MatArray) xstream.fromXML(json);
            return ma;
        } catch (IOException ex) {
            Logger.getLogger(StreamToMatArrayConverter.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
