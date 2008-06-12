package net.sf.taverna.t2.reference;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;
import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactStatus;
import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.RepositoryListener;

import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Trivial test to check whether there was an obscure problem with the
 * t2reference-impl loading through raven. Apparently there is a problem to do
 * with avoiding parsing out parents when not required, it's not critical but
 * I'll keep this here for now as a reminder.
 * 
 * @author Tom Oinn
 */
public class BaseContextTest {

	@Test
	public void doTest() {

		ApplicationContext context = new RavenAwareClassPathXmlApplicationContext(
				"baseContextTest1.xml");

		Repository rep = (Repository) context.getBean("ravenRepository");

		Artifact a = new BasicArtifact("net.sf.taverna.t2", "t2reference-impl",
				"0.1-SNAPSHOT");

		rep.addRepositoryListener(new RepositoryListener() {

			public void statusChanged(Artifact arg0, ArtifactStatus arg1,
					ArtifactStatus arg2) {
				System.out.println(arg0.toString() + ":" + arg1.toString()
						+ "->" + arg2.toString());
			}

		});

		rep.addArtifact(a);

		System.out.println(rep.getStatus(a));

		rep.update();

		System.out.println(rep.getStatus(a));

	}

}
