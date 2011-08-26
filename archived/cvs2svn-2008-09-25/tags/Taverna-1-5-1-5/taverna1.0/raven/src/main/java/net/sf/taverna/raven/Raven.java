package net.sf.taverna.raven;

import net.sf.taverna.raven.repository.RepositoryListener;
import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactStatus;

import java.util.LinkedList;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.net.URL;
import java.lang.reflect.Method;

/**
 * Applicaton to run a normal Java application from a classloader acquired by
 * Raven.
 *
 * @author Matthew Pocock
 */
public class Raven
{
  public static void main(String[] args)
          throws Throwable
  {
    LinkedList<String> argL = new LinkedList<String>(Arrays.asList(args));

    File localRepository;
    if(argL.isEmpty()) failure();
    localRepository = new File(argL.removeFirst());

    List<URL> repos = new ArrayList<URL>();
    if(argL.isEmpty()) failure();
    while(argL.getFirst().matches("\\w+://.*"))
    {
      repos.add(new URL(argL.removeFirst()));
    }
    if(repos.isEmpty()) failure();

    String groupId;
    if(argL.isEmpty()) failure();
    groupId = argL.removeFirst();

    String artifactId;
    if(argL.isEmpty()) failure();
    artifactId = argL.removeFirst();

    String version;
    if(argL.isEmpty()) failure();
    version = argL.removeFirst();

    String appClassName;
    if(argL.isEmpty()) failure();
    appClassName = argL.removeFirst();

    boolean debug;
    if(argL.isEmpty()) failure();
    debug = Boolean.valueOf(argL.removeFirst());

    RepositoryListener listener;

    if(debug)
    {
      listener = new RepositoryListener()
      {
        public void statusChanged(Artifact artifact, ArtifactStatus oldStatus, ArtifactStatus newStatus)
        {
          System.err
                  .println("Artifact " + artifact + " has changed state from " + oldStatus + " to " + newStatus);
        }
      };
    }
    else
    {
      listener = new RepositoryListener()
      {
        public void statusChanged(Artifact artifact, ArtifactStatus oldStatus, ArtifactStatus newStatus)
        {
          // noop
        }
      };
    }

    Class<?> appClass = Loader.doRavenMagic(localRepository,
                                            repos.toArray(new URL[] {}),
                                            groupId,
                                            artifactId,
                                            version,
                                            appClassName,
                                            listener);

    Method main = appClass.getMethod("main", String[].class);
    main.invoke(null, argL.toArray(new String[] {}));
  }

  public static void failure()
  {
    System.out.println(
            "Use exactly the following command-line, with options in exactly " +
            "this order:\n" +
            "localRepository remoteRepositoryURL1..n groupId artifactId" +
            " version applicationClassName debug [options-to-pass-through]\n" +
            "where remoteRepositoryX is a URL containing a protocol and" +
            " debug is true/false");
    System.exit(-1);
  }
}
