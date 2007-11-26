package net.sf.taverna.t2.activities.beanshell;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalArtifactClassLoader;

public class BeanshellClassloader extends URLClassLoader {
	private List<Artifact> artifacts;
	private Repository repo;

	public BeanshellClassloader(List<Artifact> artifacts) {
		super(new URL[] {});
		this.artifacts = artifacts;
		if (this.getClass().getClassLoader() instanceof LocalArtifactClassLoader) {

			repo = ((LocalArtifactClassLoader) this.getClass()
					.getClassLoader()).getRepository();
			for (Artifact a : artifacts) {
				repo.addArtifact(a);
			}
			repo.update();
		}
	}

	@Override
	protected Class<?> findClass(String classname) throws ClassNotFoundException {
		if (repo!=null) {
			for (Artifact a : artifacts) {
				ClassLoader cl;
				try {
					cl = repo.getLoader(a, null);
					Class<?> result = cl.loadClass(classname);
					return result;
				} catch (ArtifactNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ArtifactStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					
				}
			}
			throw new ClassNotFoundException("No class found for:"+classname);
			
		}
		else {
			return this.getClass().getClassLoader().loadClass(classname);
		}
	}
	
	
}
