package net.sf.taverna.dalec.configurator;

import java.io.*;
import java.util.jar.Manifest;
import java.util.jar.JarOutputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.ArrayList;

/**
 * Javadocs go here.
 *
 * @author Tony Burdett
 * @version 1.0 date: 09-Jan-2006
 */
public class DalecArchiver
{
    private static int buffer = 2048;

    private File jarFile;

    private File rootDir;

    public DalecArchiver(File jarFile, File baseDir)
    {
        this.jarFile = jarFile;
        this.rootDir = baseDir;
    }

    public void setBufferSize(int bufferSize)
    {
        buffer = bufferSize;
    }

    public void createArchive() throws IOException
    {
        ArrayList fileArray = new ArrayList();
        addFiles(fileArray, rootDir);
        File[] files = new File[fileArray.size()];
        for (int i = 0; i < files.length; i++)
        {
            files[i] = (File) fileArray.get(i);
        }

        JarOutputStream jarOutput = null;
        byte data[] = new byte[0];
        Manifest manifest = createManifest();
        if (!jarFile.exists())
        {
            jarFile.getParentFile().mkdirs();
        }

        // Set up the output stream
        jarOutput = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(jarFile)), manifest);

        // Create a byte array to buffer data being read
        data = new byte[buffer];

        // Enter every file in the array into the jar file
        for (int i = 0; i < files.length; i++)
        {
            File f = files[i];
            if (!f.isDirectory())
            {
                // Produce correct entry name
                String entryName = f.toString().replaceFirst(rootDir.toString(), "");
                entryName = entryName.replace('\\', '/');
                if (entryName.startsWith("/"))
                {
                    entryName = entryName.substring(1);
                }

                BufferedInputStream input = null;
                // Create a JarEntry (named relative to the current root directory)
                JarEntry je = new JarEntry(entryName);
                // Set the JarOutputStream ready to write this je
                try
                {
                    jarOutput.putNextEntry(je);
                }
                catch (IOException e)
                {
                    System.out.println("Can't add jar entry: " + je.getName());
                    throw e;
                }

                // Create a new input stream for this file using the given buffer size
                try
                {
                    input = new BufferedInputStream(new FileInputStream(f), buffer);
                }
                catch (FileNotFoundException e)
                {
                    System.out.println("Unable to read from file: " + f.getName());
                    throw e;
                }

                // While data can still be read, read it into the data array and then write it into the jar file
                int count;
                while ((count = input.read(data, 0, buffer)) != -1)
                {
                    jarOutput.write(data, 0, count);
                }

                // Now close the current JarEntry and close this input stream
                input.close();
                jarOutput.closeEntry();
            }
        }

        // Close the JarOutputStream and FileOutputStream
        jarOutput.finish();
        jarOutput.close();
    }

    public void inflateArchive() throws IOException
    {
        // create the input stream for the jar file
        JarInputStream jarInput = new JarInputStream(new FileInputStream(jarFile));
        BufferedInputStream input = new BufferedInputStream(jarInput, buffer);

        // get the next entry
        JarEntry nextEntry = jarInput.getNextJarEntry();
        while (nextEntry != null)
        {
            // create the output stream for the current entry
            BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(new File(rootDir, nextEntry.getName())));

            // Create a byte array to buffer data being read
            byte data[] = new byte[buffer];

            int count;
            while ((count = input.read(data, 0, buffer)) != -1)
            {
                output.write(data, 0, count);
            }

            // Now close this file stream
            output.flush();
            output.close();

            // get next entry
            nextEntry = jarInput.getNextJarEntry();
        }

        jarInput.close();
    }

    private void addFiles(ArrayList files, File f)
    {
        if (f.listFiles() == null)
        {
            files.add(f);
        }
        else
        {
            for (int i = 0; i < f.listFiles().length; i++)
            {
                File child = f.listFiles()[i];
                addFiles(files, child);
            }
        }
    }

    private Manifest createManifest() throws IOException
    {
        String contents =
                "Manifest-Version: 1.0\n" +
                        "Created-By: net.sf.taverna.dalec.configurator.DalecArchiver" +
                        "\n\n";
        ByteArrayInputStream bi = new ByteArrayInputStream(contents.getBytes());
        return new Manifest(bi);
    }
}
