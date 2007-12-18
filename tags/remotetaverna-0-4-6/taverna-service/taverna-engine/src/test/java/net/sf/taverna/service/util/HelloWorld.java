package net.sf.taverna.service.util;

import java.util.Arrays;


public class HelloWorld {
	public static void main(String[] args) {
		System.out.println("Hello world!");
		System.out.println(Arrays.asList(args));
		System.out.println(System.getProperty("fish") + " " + System.getProperty("very"));
	}
}
