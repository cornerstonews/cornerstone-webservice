/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.cornerstonews.webservice.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClassFinder {

	private static final Logger log = LogManager.getLogger(ClassFinder.class);
	
	private String requiredPackage = null;
	
	private Class<?> superClass = null;
	
	private List<Class<?>> classes = new ArrayList<>();
	
	public ClassFinder() {}
	
	public ClassFinder(Class<?> superClass) {
		this(superClass, null);
	}
	
	public ClassFinder(Class<?> superClass, String requiredPackage) {
		this.superClass = superClass;
		this.requiredPackage = requiredPackage;
	}
	
	public List<Class<?>> getClasses() throws IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		assert classLoader != null;
		
		String path = "";
		if(this.requiredPackage != null) {
			path = this.requiredPackage.replace('.', '/');
		}
		log.info("Looking for classes with package path: {}", path);
		
		Enumeration<URL> resources = classLoader.getResources(path);
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			String directoryString = resource.getFile().replace("!/" + path + "/", "").replace("file:", "");
			File directoryFile = new File(directoryString);
			this.classes.addAll(findClasses(directoryFile));
		}
		
		return this.classes;
	}

	private List<Class<?>> findClasses(File directoryFile) {
		log.debug("Looking for classes in '{}'", directoryFile.toString());
		
		ArrayList<Class<?>> classesFound = new ArrayList<>();
		if (!directoryFile.exists()) {
			log.debug("Skipping directory: {}", directoryFile.toString());
			return classesFound;
		}

		if ((directoryFile.getName().endsWith(".jar") || (directoryFile.getName().endsWith(".zip")))) {
			try (Stream<? extends ZipEntry> zipFile = new ZipFile(directoryFile).stream()) {
				zipFile.filter(file -> file.getName().endsWith(".class"))
					.peek(file -> log.debug("  --> file: {}", file.getName()))
					.forEach(file -> classesFound.add(getClassByName(file.getName())));
			} catch (IOException e) {
				log.error("Exception caught creating ZipFile. ", e);
			}
		} 
		else {
			Arrays.stream(directoryFile.listFiles())
				.peek(file -> log.debug("  --> file: {}", file.getName()))
				.map(file -> file.isDirectory() ? findClasses(file) : classesFound.add(getClassByName(file.getName())));
		}

		return classesFound.stream()
				.filter(clazz -> (this.superClass == null || this.superClass.isAssignableFrom(clazz)))
				.collect(Collectors.toList());
	}
	
	private Class<?> getClassByName(String clazzName) {
    	try {
			return Class.forName(clazzName.replace('/', '.').replace(".class", ""));
		} catch (ClassNotFoundException e) {
			log.error("This should not happen since we are locating class from classpath.", e);
			return null;
		}
	}
}
