package net.sf.taverna.t2.platform.util.download.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class with a couple of methods to get the contents of a URL as a
 * string, removing all non-hex characters and whitespace, and to get the hash
 * of a file as a hex string using the MD5 algorithm.
 * 
 * @author Tom Oinn
 */
public final class MD5Support {

	private MD5Support() {
		// Not meant to be constructed, use as a library of static methods
	}

	private static int chunkSize = 1024;

	/**
	 * Implementation of FilterInputStream that passes all bytes through an MD5
	 * message digest filter, used by the public static method in this class to
	 * compute the MD5 hash of a file by passing the file input stream through
	 * this filter.
	 */
	private static class MD5InputStream extends FilterInputStream {

		private MessageDigest md5;

		private MD5InputStream(InputStream in) {
			super(in);
			try {
				md5 = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException("Cannot create MD5 verifier", e);
			}
		}

		byte[] digest() {
			return md5.digest();
		}

		/**
		 * @see java.io.FilterInputStream#read()
		 */
		public int read() throws IOException {
			int c = in.read();
			if (c == -1) {
				return -1;
			}
			if ((c & ~0xff) != 0) {
				// Something strange happening here
			} else {
				md5.update((byte) c);
			}
			return c;
		}

		/**
		 * @see java.io.FilterInputStream#read(byte[], int, int)
		 */
		public int read(byte bytes[], int offset, int length)
				throws IOException {
			int r;
			if ((r = in.read(bytes, offset, length)) == -1)
				return r;
			md5.update(bytes, offset, r);
			return r;
		}
	}

	/**
	 * Uses the internal MD5 input stream filter to compute the MD5 sum of the
	 * supplied file and return it as a lower case hex string
	 * 
	 * @param file
	 *            the file to hash
	 * @return a lower case hex string representation of the MD5 sum of the
	 *         supplied file
	 * @throws IOException
	 *             if any issues occur when reading the file
	 */
	public static String getMD5FileHashAsAlphaString(File file)
			throws IOException {
		InputStream close_me = null;
		try {
			long buf_size = file.length();
			if (buf_size < 512)
				buf_size = 512;
			if (buf_size > 65536)
				buf_size = 65536;
			byte[] buf = new byte[(int) buf_size];
			MD5InputStream in = new MD5InputStream(new FileInputStream(file));
			close_me = in;
			while (in.read(buf) != -1) {
				// Consume all the file through the input stream filter
			}
			in.close();
			return byteArrayToHexString(in.digest());
		} catch (IOException e) {
			if (close_me != null)
				try {
					close_me.close();
				} catch (Exception e2) {
				}
			throw e;
		}
	}

	/**
	 * Returns the contents of the URL as a string, with all non-hex characters
	 * removed
	 * 
	 * @param location
	 *            a URL to read, interpreting its contents as a hex string as
	 *            found in typical MD5 hashes
	 * @return a string containing the contents of the URL, all non hex
	 *         characters are removed and the string is converted to lower case
	 * @throws IOException
	 *             if any problems occur when opening the supplied URL
	 */
	public static String getURLContentsAsHexString(URL location)
			throws IOException {
		byte[] bytes;
		InputStream in = location.openStream();
		int count;
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		byte[] b = new byte[chunkSize];
		try {
			while ((count = in.read(b, 0, chunkSize)) > 0)
				bo.write(b, 0, count);
			bytes = bo.toByteArray();

		} finally {
			bo.close();
			bo = null;
		}
		return new String(bytes).replaceAll("[^a-fA-F0-9]", "").toLowerCase();
	}

	/**
	 * Render a byte array to lower case hex string
	 */
	private static String byteArrayToHexString(byte in[]) {
		byte ch = 0x00;
		String digits[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
				"a", "b", "c", "d", "e", "f" };
		StringBuffer out = new StringBuffer(in.length * 2);
		for (byte b : in) {
			ch = (byte) (b & 0xF0);
			ch = (byte) (ch >>> 4);
			ch = (byte) (ch & 0x0F);
			out.append(digits[(int) ch]);
			ch = (byte) (b & 0x0F);
			out.append(digits[(int) ch]);
		}
		return out.toString();
	}

}
