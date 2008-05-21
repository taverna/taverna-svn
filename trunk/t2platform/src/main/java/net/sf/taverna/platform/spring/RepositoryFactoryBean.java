package net.sf.taverna.platform.spring;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalRepository;
import static net.sf.taverna.platform.spring.PropertyInterpolator.interpolate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * A FactoryBean used to configure and instantiate a raven Repository object.
 * Allows construction of file base path, remote repository list and system
 * artifact list through setter injection from within spring and delegates to
 * LocalRepository.getRepository(File, ClassLoader, List&lt;Artifact&gt;) to
 * construct the Repository object.
 * <p>
 * Because this implements FactoryBean, spring will return the result of the
 * getObject method rather than the bean itself.
 * 
 * @author Tom Oinn
 */
public class RepositoryFactoryBean implements FactoryBean {

	private static Log log = LogFactory.getLog(RepositoryFactoryBean.class);

	private List<String> systemArtifactStrings = null;
	private String baseLocation = null;
	private List<String> remoteRepositories = null;

	public Object getObject() throws Exception {
		File base = new File(interpolate(baseLocation));
		Set<Artifact> systemArtifacts = new HashSet<Artifact>();
		for (String systemArtifactSpec : systemArtifactStrings) {
			String[] s = interpolate(systemArtifactSpec).split(":");
			Artifact a = new BasicArtifact(s[0], s[1], s[2]);
			systemArtifacts.add(a);
		}
		Repository r = LocalRepository.getRepository(base, this.getClass()
				.getClassLoader(), systemArtifacts);

		for (String repositoryLocationString : remoteRepositories) {
			try {
				r.addRemoteRepository(new URL(
						interpolate(repositoryLocationString)));
			} catch (RuntimeException ex) {
				// Don't add repositories which cause an error on instantiation,
				// this can be because the URL is invalid but can also occur if
				// the interpolation attempts to use a property that isn't
				// defined. This can be used intentionally to add repositories
				// only if a property is set.
				log
						.warn("Missing property in interpolation, ignoring remote repository entry "
								+ repositoryLocationString);
			} catch (MalformedURLException mue) {
				log.error("Malformed remote repository URL",mue);
			}
		}
		r.update();
		log.info("Constructed raven repository at '" + base + "'");
		return r;
	}

	public void setSystemArtifacts(List<String> systemArtifacts) {
		this.systemArtifactStrings = systemArtifacts;
	}

	public void setBase(String baseLocation) {
		this.baseLocation = baseLocation;
	}

	public void setRemoteRepositoryList(List<String> remoteRepositories) {
		this.remoteRepositories = remoteRepositories;
	}

	@SuppressWarnings("unchecked")
	public Class getObjectType() {
		return Repository.class;
	}

	public boolean isSingleton() {
		return true;
	}

}
