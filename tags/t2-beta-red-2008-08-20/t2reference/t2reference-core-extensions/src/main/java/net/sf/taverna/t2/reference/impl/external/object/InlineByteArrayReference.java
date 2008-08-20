package net.sf.taverna.t2.reference.impl.external.object;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.apache.commons.codec.binary.Base64;

import net.sf.taverna.t2.reference.AbstractExternalReference;
import net.sf.taverna.t2.reference.DereferenceException;
import net.sf.taverna.t2.reference.ReferenceContext;
import net.sf.taverna.t2.reference.ValueCarryingExternalReference;

/**
 * A reference implementation that inlines an array of bytes. Rather
 * unpleasantly this currently exposes the byte array to Hibernate through a
 * textual value, as Derby allows long textual values but not long binary ones
 * (yuck). As it uses a fixed character set (UTF-8) to store and load I believe
 * this doesn't break things.
 * 
 * Unfortunately this does break things (binaries get corrupted) so I've added
 * base64 encoding of the value as a workaround.
 * 
 * @author Tom Oinn
 * @author David Withers
 */
public class InlineByteArrayReference extends AbstractExternalReference
		implements ValueCarryingExternalReference<byte[]> {

	private byte[] bytes = new byte[0];

	public void setValue(byte[] newBytes) {
		this.bytes = newBytes;
	}

	public byte[] getValue() {
		return bytes;
	}

	public Class<byte[]> getValueType() {
		return byte[].class;
	}

	public InputStream openStream(ReferenceContext context)
			throws DereferenceException {
		return new ByteArrayInputStream(bytes);
	}

	private static Charset charset = Charset.forName("UTF-8");

	public String getContents() {
		try {
			return new String(Base64.encodeBase64(bytes), charset.toString());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void setContents(String contentsAsString) {
		try {
			this.bytes = Base64.decodeBase64(contentsAsString.getBytes(charset.toString()));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
