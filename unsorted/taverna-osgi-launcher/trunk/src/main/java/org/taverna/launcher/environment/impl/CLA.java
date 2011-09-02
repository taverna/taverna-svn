package org.taverna.launcher.environment.impl;

import static java.lang.Math.max;
import static java.util.Collections.sort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.taverna.launcher.Main;
import org.taverna.launcher.environment.CommandLineArgumentProvider;

public class CLA implements CommandLineArgumentProvider {
	private final Main main;

	public CLA(Main main, List<String> args, String template) {
		this.main = main;
		this.arguments = new ArrayList<String>(args);
		this.argumentHelp = new HashMap<String, String>();
		this.template = template;
	}

	private List<String> arguments;
	private Map<String, String> argumentHelp;
	private String template;

	@Override
	public void printHelp() {
		List<String> argNames = new ArrayList<String>(argumentHelp.keySet());
		sort(argNames);
		System.out.println("Usage: " + template);
		int len = 0;
		for (String arg : argNames) {
			if (argumentHelp.get(arg) == null)
				continue;
			len = max(len, arg.length() + 3);
		}
		for (String arg : argNames) {
			String help = argumentHelp.get(arg);
			if (help == null)
				continue;
			System.out.printf("  %-" + (len - 3) + "s ", arg);
			int linelen = len;
			for (String word : help.split("\\s+")) {
				if (linelen + word.length() + 1 > 78) {
					System.out.println();
					System.out.printf("%" + len + "s", " ");
					linelen = len;
				}
				System.out.print(" ");
				System.out.print(word);
				linelen += 1 + word.length();
			}
			System.out.println();
		}
	}

	@Override
	public void markAsStarted() {
		main.markStarted();
	}

	@Override
	public List<String> consumeArgumentOnce(String name, int parameters,
			String help) {
		if (!name.startsWith("-"))
			throw new IllegalArgumentException(
					"parameter name must start with '-' character");
		argumentHelp.put(name, help);
		List<String> result = null;
		synchronized (arguments) {
			for (int i = 0; i < arguments.size() - parameters; i++) {
				if (!arguments.get(i).startsWith("-"))
					continue;
				if (arguments.get(i).equals("--"))
					break;
				if (arguments.get(i).equals(name)) {
					if (result != null)
						throw new RuntimeException("argument " + name
								+ " occurs multiple times");
					if (i + 1 + parameters > arguments.size())
						throw new RuntimeException("argument " + name
								+ " is missing at least one parameter");
					result = arguments.subList(i + 1, i + 1 + parameters);
					i += parameters;
				}
			}
			if (result == null)
				return null;
			try {
				return new ArrayList<String>(result);
			} finally {
				result.clear();
			}
		}
	}

	@Override
	public List<List<String>> consumeArgumentMultiple(String name,
			int parameters, String help) {
		if (!name.startsWith("-"))
			throw new IllegalArgumentException(
					"parameter name must start with '-' character");
		argumentHelp.put(name, help);
		List<List<String>> result = new ArrayList<List<String>>();
		synchronized (arguments) {
			for (int i = 0; i < arguments.size() - 1 - parameters; i++) {
				if (!arguments.get(i).startsWith("-"))
					continue;
				if (arguments.get(i).equals("--"))
					break;
				if (arguments.get(i).equals(name)) {
					if (i + 1 + parameters > arguments.size())
						throw new RuntimeException("argument " + name
								+ " is missing at least one parameter");
					List<String> sub = arguments.subList(i + 1, i + 1
							+ parameters);
					result.add(new ArrayList<String>(sub));
					sub.clear();
				}
			}
		}
		return result;
	}

	@Override
	public List<String> getRemainingArguments(String template) {
		if (template != null)
			this.template = template;
		return new ArrayList<String>(arguments);
	}
}