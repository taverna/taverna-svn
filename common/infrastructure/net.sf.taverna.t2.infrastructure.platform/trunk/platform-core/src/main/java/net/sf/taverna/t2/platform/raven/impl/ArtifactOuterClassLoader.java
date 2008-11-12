package net.sf.taverna.t2.platform.raven.impl;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Vector;

public class ArtifactOuterClassLoader extends URLClassLoader {

	private ArtifactInnerClassLoader delegate;

	ArtifactOuterClassLoader(ClassLoader parent,
			ArtifactInnerClassLoader delegate) {
		super(new URL[0], parent);
		this.delegate = delegate;
	}

	@Override
	protected Class<?> findClass(String className)
			throws ClassNotFoundException {
		try {
			if (findLoadedClass(className) != null) {
				return findLoadedClass(className);
			}
			return super.findClass(className);
		} catch (ClassNotFoundException cnfe) {
			return delegate.findClass(className);
		}
	}

	@Override
	public URL findResource(String resourceName) {
		URL result = super.findResource(resourceName);
		if (result == null) {
			result = delegate.findResource(resourceName);
		}
		return result;
	}

	@Override
	public Enumeration<URL> findResources(String resourceName)
			throws IOException {
		Vector<URL> resultVector = new Vector<URL>();
		ArtifactInnerClassLoader.addAllToVector(super
				.findResources(resourceName), resultVector);
		ArtifactInnerClassLoader.addAllToVector(delegate
				.findResources(resourceName), resultVector);
		return resultVector.elements();
	}
}
