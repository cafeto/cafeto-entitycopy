/**
 * Copyright (c) 2010, Fabio Ospitia Trujillo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   * Neither the name of the Cafeto.Net nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.cafeto.entitycopy;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

/**
 * @author fospitia
 * 
 */
public class EntityCopy {

	/**
	 * @param <T>
	 * @param copy
	 * @param deepCopy
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static synchronized <T> T copy(final T copy, DeepCopy deepCopy) throws InstantiationException,
			IllegalAccessException {
		return copy(copy, deepCopy, null);
	}

	/**
	 * @param <T>
	 * @param copy
	 * @param deepCopy
	 * @param parentClass
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	public static synchronized <T> T copy(final T copy, DeepCopy deepCopy, Class<?> parentClass)
			throws InstantiationException, IllegalAccessException {
		if (copy == null) {
			return null;
		}

		T source = copy;
		Class<T> clazz = (Class<T>) source.getClass();
		if (!clazz.isAnnotationPresent(Entity.class)) {
			return source;
		}

		T target = (T) clazz.newInstance();

		if (deepCopy == null)
			deepCopy = DeepCopy.createDefault();

		final List<Field> fields = allFields(clazz);
		for (final Field field : fields) {
			if (Modifier.isStatic(field.getModifiers())) {
				continue;
			}

			DeepCopy fieldDeepCopy = deepCopy.getDeepCopies().get(field.getName());
			if (fieldDeepCopy == null && parentClass != null && parentClass.equals(field.getType())) {
				continue;
			}

			if (fieldDeepCopy != null && fieldDeepCopy.getType().equals(DeepCopyType.NONE)) {
				continue;
			}

			if (!isAssociation(field)) {
				field.setAccessible(true);
				field.set(target, field.get(source));
				continue;
			}

			if (deepCopy.getType().equals(DeepCopyType.NONE)) {
				continue;
			}

			if (fieldDeepCopy == null && deepCopy.getType().equals(DeepCopyType.DEFAULT)) {
				if (getFetchType(field).equals(FetchType.LAZY)) {
					continue;
				}
			}

			field.setAccessible(true);
			Object value = field.get(source);
			if (value == null) {
				continue;
			}

			if (fieldDeepCopy == null) {
				fieldDeepCopy = DeepCopy.createDefault();
			}

			if (value instanceof Set<?>) {
				Set<Object> objs = new HashSet<Object>();
				for (Object obj : ((Set<?>) value)) {
					objs.add(copy(obj, fieldDeepCopy, clazz));
				}
				field.set(target, objs);
			} else if (value instanceof List<?>) {
				List<Object> objs = new ArrayList<Object>();
				for (Object obj : (List<?>) value) {
					objs.add(copy(obj, fieldDeepCopy, clazz));
				}
				field.set(target, objs);
			} else if (value instanceof SortedSet<?>) {
				SortedSet<Object> objs = new TreeSet<Object>();
				for (Object obj : ((SortedSet<?>) value)) {
					objs.add(copy(obj, fieldDeepCopy, clazz));
				}
				field.set(target, objs);
			} else if (value instanceof Map<?, ?>) {
				Map<Object, Object> objs = new HashMap<Object, Object>();
				for (Entry<Object, Object> entry : objs.entrySet()) {
					objs.put(entry.getKey(), copy(entry.getValue(), fieldDeepCopy, clazz));
				}
				field.set(target, objs);
			} else if (value instanceof SortedMap<?, ?>) {
				SortedMap<Object, Object> objs = new TreeMap<Object, Object>();
				for (Entry<Object, Object> entry : objs.entrySet()) {
					objs.put(entry.getKey(), copy(entry.getValue(), fieldDeepCopy, clazz));
				}
				field.set(target, objs);
			} else {
				field.set(target, copy(value, fieldDeepCopy, clazz));
			}
		}
		return target;
	}

	/**
	 * @param field
	 * @return
	 */
	private static boolean isAssociation(Field field) {
		if (field.isAnnotationPresent(Transient.class)) {
			return false;
		}

		Type genericType = field.getGenericType();
		if (genericType instanceof ParameterizedType) {
			ParameterizedType pType = (ParameterizedType) genericType;
			for (Type type : pType.getActualTypeArguments()) {
				if (((Class<?>) type).isAnnotationPresent(Entity.class)) {
					return true;
				}
			}
		} else {
			if (field.getType().isAnnotationPresent(Entity.class)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @param field
	 * @return
	 */
	private static FetchType getFetchType(Field field) {
		FetchType fetchType = null;
		if (field.isAnnotationPresent(Basic.class)) {
			fetchType = field.getAnnotation(Basic.class).fetch();
		} else if (field.isAnnotationPresent(OneToMany.class)) {
			fetchType = field.getAnnotation(OneToMany.class).fetch();
		} else if (field.isAnnotationPresent(OneToOne.class)) {
			fetchType = field.getAnnotation(OneToOne.class).fetch();
		} else if (field.isAnnotationPresent(ManyToOne.class)) {
			fetchType = field.getAnnotation(ManyToOne.class).fetch();
		} else if (field.isAnnotationPresent(ManyToMany.class)) {
			fetchType = field.getAnnotation(ManyToMany.class).fetch();
		}

		if (fetchType != null)
			return fetchType;

		Type genericType = field.getGenericType();
		if (genericType instanceof ParameterizedType) {
			return FetchType.LAZY;
		} else {
			if (field.getType().isAnnotationPresent(Entity.class)) {
				return FetchType.EAGER;
			} else {
				return FetchType.LAZY;
			}
		}
	}

	/**
	 * @param c
	 * @return
	 */
	private static List<Field> allFields(final Class<?> c) {
		List<Field> l = new LinkedList<Field>();
		final Field[] fields = c.getDeclaredFields();
		addAll(l, fields);
		Class<?> sc = c;
		while ((sc = sc.getSuperclass()) != Object.class && sc != null) {
			addAll(l, sc.getDeclaredFields());
		}
		return l;
	}

	/**
	 * @param l
	 * @param fields
	 */
	private static void addAll(final List<Field> l, final Field[] fields) {
		for (final Field field : fields) {
			l.add(field);
		}
	}
}
