package net.sf.taverna.platform.spring;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalRepository;

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

	private List<String> systemArtifactStrings = null;
	private String baseLocation = null;
	private List<String> remoteRepositories = null;

	public Object getObject() throws Exception {
		File base = new File(baseLocation);
		Set<Artifact> systemArtifacts = new HashSet<Artifact>();
		for (String systemArtifactSpec : systemArtifactStrings) {
			String[] s = systemArtifactSpec.split(":");
			Artifact a = new BasicArtifact(s[0], s[1], s[2]);
			systemArtifacts.add(a);
		}
		Repository r = LocalRepository.getRepository(base, this.getClass()
				.getClassLoader(), systemArtifacts);

		for (String repositoryLocationString : remoteRepositories) {
			r.addRemoteRepository(new URL(repositoryLocationString));
		}
		r.update();
		System.out.println("Constructed raven repository at '" + base + "'");
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
