package net.sf.taverna.service.util;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class TestJavaProcess {
	
	@Test
	public void findJava() throws IOException, InterruptedException {
		String java = JavaProcess.findJava();
		Process javaProcess = new ProcessBuilder(java, "-version").start();		
		String version = IOUtils.toString(javaProcess.getErrorStream());
		javaProcess.waitFor();
		assertTrue(version.startsWith("java version \"" + System.getProperty("java.version") + "\""));
	}
	
	@Test
	public void runProcessClasspath() throws IOException, ClassNotFoundException {
		URL path = HelloWorld.class.getResource("/net/sf/taverna/service/util/HelloWorld.class");
		assertNotNull(path);
		URL root = new URL(path, "../../../../../");
		
		JavaProcess p = new JavaProcess(new URL[]{root},
			"net.sf.taverna.service.util.HelloWorld");
		assertFalse(p.isInherittingClasspath());
		assertTrue(p.isRedirectingError());
		p.addArguments("my", "arguments");
		p.addArguments("added", "here");
		p.addSystemProperty("fish", "soup");
		p.addSystemProperty("very", "nice");
		Process proc = p.run();
		assertEquals("Hello world!\n" +
				"[my, arguments, added, here]\n" +
				"soup nice\n", IOUtils.toString(proc.getInputStream()));
	}
	
	@Test
	public void runProcessInherited() throws IOException, ClassNotFoundException {
		JavaProcess p = new JavaProcess(
			"net.sf.taverna.service.util.HelloWorld", getClass().getClassLoader());
		assertTrue(p.isInherittingClasspath());
		assertTrue(p.isRedirectingError());
		p.addArguments("my", "arguments");
		p.addArguments("added", "here");
		p.addSystemProperty("fish", "soup");
		p.addSystemProperty("very", "nice");
		Process proc = p.run();
		assertEquals("Hello world!\n" +
				"[my, arguments, added, here]\n" +
				"soup nice\n", IOUtils.toString(proc.getInputStream()));
	}
}
