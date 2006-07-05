package org.embl.ebi.escience.scufl.tools;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Extensions that try to make Java a better or easier programming language
 * 
 * @author Stian Soiland
 * 
 */
public class Lang {

	/*
	 * Find method by name. Almost like Class.getMethod() - but ignore parameter
	 * matching and return first match. If obj is not a Class instance,
	 * obj.getClass() will be searched. If the method is not found, null is
	 * returned.
	 */
	public static Method getMethod(String method_name, Object obj) {
		Class theClass;
		if (obj instanceof Class) {
			theClass = (Class) obj;
		} else {
			theClass = obj.getClass();
		}
		Method[] methods = theClass.getMethods();
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().equals(method_name)) {
				return methods[i];
			}
		}
		return null;
	}

	// TODO: Make a map() where obj is set to element, as to do element.method()
	// with no arguments
	/*
	 * Map method to each element in iterable. The method_name is looked up in
	 * obj as by getMethod(). Return a List of method(element) for each element.
	 * 
	 * Note that if IllegalArgumentException. IllegalAccessException or
	 * InvocationTargetException is thrown, the stacktrace is printed and null
	 * is added to the results.
	 */
	public static List map(String method_name, Collection iterable, Object obj) {		
		Method method = getMethod(method_name, obj);
		return map(method, iterable, obj);
	}

	/*
	 * Map method to each element in iterable. Return a List of method(element)
	 * for each element.
	 * 
	 * If parameter obj is specified, it is the object in which the method
	 * belongs (this), otherwise method must be static.
	 * 
	 * Note that if IllegalArgumentException. IllegalAccessException or
	 * InvocationTargetException is thrown, the stacktrace is printed and null
	 * is added to the results.
	 */
	public static List map(Method method, Collection iterable, Object obj) {		
		Iterator it = iterable.iterator();
		List results = new ArrayList();
		Object[] parameters = new Object[1];
		while (it.hasNext()) {
			parameters[0] = it.next();
			Object res = null;
			try {
				res = method.invoke(obj, parameters);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			results.add(res);
		}
		return results;
	}

	/**
	 * Convert primitive type to the proper Object instance.
	 * 
	 */
	public static Object asObject(boolean b) {
		return new Boolean(b);
	}

	public static Object asObject(byte b) {
		return new Byte(b);
	}

	public static Object asObject(char c) {
		return new Character(c);
	}

	public static Object asObject(double d) {
		return new Double(d);
	}

	public static Object asObject(float f) {
		return new Float(f);
	}

	public static Object asObject(int i) {
		return new Integer(i);
	}

	public static Object asObject(long l) {
		return new Long(l);
	}

	public static Object asObject(short s) {
		return new Short(s);
	}

	/**
	 * Convert arrays containing primitive types such as int[] into a List
	 * containing the Object version of the primitive type.
	 * <p>
	 * For instance, if the array is of type int[], the result will be a List
	 * containing Integer instances.
	 * <p>
	 * Deep arrays will also be converted, so a List containing String[] and
	 * char[] will be converted to a List containing another List of Strings and
	 * a List of Characters.
	 * 
	 * @param list
	 *            Array or List to be converted
	 * @return A List where arrays are converted to Lists and primitive types to
	 *         their Object version.
	 */
	public static List asObjectList(Object list) {
		List newList = new ArrayList();
		if (list.getClass().isArray()) {
			Class componentType = list.getClass().getComponentType();
			if (componentType.isPrimitive()) {
				if (componentType == Boolean.TYPE) {
					boolean[] array = (boolean[]) list;
					for (int i = 0; i < array.length; i++) {
						newList.add(asObject(array[i]));
					}
				} else if (componentType == Byte.TYPE) {
					byte[] array = (byte[]) list;
					for (int i = 0; i < array.length; i++) {
						newList.add(asObject(array[i]));
					}
				} else if (componentType == Character.TYPE) {
					char[] array = (char[]) list;
					for (int i = 0; i < array.length; i++) {
						newList.add(asObject(array[i]));
					}
				} else if (componentType == Double.TYPE) {
					double[] array = (double[]) list;
					for (int i = 0; i < array.length; i++) {
						newList.add(asObject(array[i]));
					}
				} else if (componentType == Float.TYPE) {
					float[] array = (float[]) list;
					for (int i = 0; i < array.length; i++) {
						newList.add(asObject(array[i]));
					}
				} else if (componentType == Integer.TYPE) {
					int[] array = (int[]) list;
					for (int i = 0; i < array.length; i++) {
						newList.add(asObject(array[i]));
					}
				} else if (componentType == Long.TYPE) {
					long[] array = (long[]) list;
					for (int i = 0; i < array.length; i++) {
						newList.add(asObject(array[i]));
					}
				} else if (componentType == Short.TYPE) {
					short[] array = (short[]) list;
					for (int i = 0; i < array.length; i++) {
						newList.add(asObject(array[i]));
					}
				} else {
					throw new UnknownError("Unknown primitive type "
							+ componentType);
				}
				return newList;
			}
			// Not primitive, it contains some kind of Object (possibly inner
			// arrays), but we can anyway safely convert it to a List for
			// further processing.
			list = Arrays.asList((Object[]) list);
		}
		if (!(list instanceof Collection)) {
			throw new UnknownError("Not a Collection!" + list);
		}
		Iterator it = ((Collection) list).iterator();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof Collection || obj.getClass().isArray()) {
				obj = asObjectList(obj);
			}
			newList.add(obj);
		}
		return newList;
	}
}
