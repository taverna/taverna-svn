import java.io.*;
import java.util.jar.Manifest;
import java.util.jar.JarOutputStream;
import java.util.jar.JarEntry;
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
    private File[] files;

    private String rootDirName;

    public DalecArchiver(File jarFile, File baseDir)
    {
        this.jarFile = jarFile;
        this.rootDirName = baseDir.toString();

        ArrayList fileArray = new ArrayList();
        addFiles(fileArray, baseDir);
        files = new File[fileArray.size()];
        for (int i = 0; i < files.length; i++)
        {
            files[i] = (File) fileArray.get(i);
        }
    }

    public void setBufferSize(int bufferSize)
    {
        buffer = bufferSize;
    }

    public void createArchive() throws IOException
    {
        Manifest manifest = createManifest();
        if(!jarFile.exists())
        {
            jarFile.getParentFile().mkdirs();
        }

        // Set up the output stream
        FileOutputStream fileOutput = new FileOutputStream(jarFile);
        JarOutputStream jarOutput = new JarOutputStream(new BufferedOutputStream(fileOutput), manifest);

        // Create a byte array to buffer data being read
        byte data[] = new byte[buffer];

        // Enter every file in the array into the jar file
        for (int i = 0; i < files.length; i++)
        {
            File f = files[i];
            if (!f.isDirectory())
            {
                // Produce correct entry name
                String entryName = f.toString().replaceFirst(rootDirName, "").substring(1);

                // Create a JarEntry (named relative to the current root directory)
                JarEntry je = new JarEntry(entryName);
                // Set the JarOutputStream ready to write this je
                jarOutput.putNextEntry(je);

                // Create a new input stream for this file using the given buffer size
                BufferedInputStream input = new BufferedInputStream(new FileInputStream(f), buffer);

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
        fileOutput.flush();
        fileOutput.close();
    }

    public File getArchive()
    {
        return jarFile;
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
                        "Created-By: DalecArchiver" +
                        "\n\n";
        ByteArrayInputStream bi = new ByteArrayInputStream(contents.getBytes());
        return new Manifest(bi);
    }
}
