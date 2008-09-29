package net.sf.taverna.t2.workbench.file.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.spi.SPIRegistry;
import net.sf.taverna.t2.workbench.file.DataflowPersistenceHandler;
import net.sf.taverna.t2.workbench.file.FileType;

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.lang.ClassUtils;

// TODO: Cache lookups / build one massive structure
public class DataflowPersistenceHandlerRegistry extends
		SPIRegistry<DataflowPersistenceHandler> {

	private static final MapFactory MAP_FACTORY = new MapFactory();

	private static final SetFactory SET_FACTORY = new SetFactory();

	public static DataflowPersistenceHandlerRegistry getInstance() {
		return DataflowPersistenceHandlerRegistryHolder.SINGLETON;
	}

	@SuppressWarnings("unchecked")
	protected static List<Class<?>> findAllParentClasses(final Class<?> sourceClass) {
		List<Class<?>> superClasses = new ArrayList<Class<?>>();
		superClasses.add(sourceClass);
		superClasses.addAll(ClassUtils.getAllSuperclasses(sourceClass));
		superClasses.addAll(ClassUtils.getAllInterfaces(sourceClass));
		return superClasses;
	}
	private Map<Class<?>, Set<FileType>> openClassToTypes;
	private Map<FileType, Map<Class<?>, Set<DataflowPersistenceHandler>>> openFileClassToHandler;

	private Map<FileType, Set<DataflowPersistenceHandler>> openFileToHandler;
	private Map<Class<?>, Set<FileType>> saveClassToTypes;
	private Map<FileType, Map<Class<?>, Set<DataflowPersistenceHandler>>> saveFileClassToHandler;

	private Map<FileType, Set<DataflowPersistenceHandler>> saveFileToHandler;

	protected DataflowPersistenceHandlerRegistry() {
		super(DataflowPersistenceHandler.class);
		addObserver(new SPIRegistryObserver());
		// Force an update
		getInstances();
	}

	public Set<FileType> getOpenFileTypes() {
		return getOpenFileClassToHandler().keySet();
	}

	public Set<FileType> getOpenFileTypesFor(Class<?> sourceClass) {
		Set<FileType> fileTypes = new LinkedHashSet<FileType>();
		for (Class<?> candidateClass : findAllParentClasses(sourceClass)) {
			fileTypes.addAll(getOpenClassToTypes().get(candidateClass));
		}
		return fileTypes;
	}

	public Set<DataflowPersistenceHandler> getOpenHandlersFor(
			FileType fileType, Class<? extends Object> sourceClass) {
		Set<DataflowPersistenceHandler> handlers = new LinkedHashSet<DataflowPersistenceHandler>();
		for (Class<?> candidateClass : findAllParentClasses(sourceClass)) {
			handlers.addAll(getOpenFileClassToHandler().get(fileType).get(
					candidateClass));
		}
		return handlers;
	}

	public Set<DataflowPersistenceHandler> getOpenHandlersForType(
			FileType fileType) {
		return getOpenFileToHandler().get(fileType);
	}

	public synchronized Set<DataflowPersistenceHandler> getOpenHandlersForType(
			FileType fileType, Class<?> sourceClass) {
		Set<DataflowPersistenceHandler> handlers = new LinkedHashSet<DataflowPersistenceHandler>();
		for (Class<?> candidateClass : findAllParentClasses(sourceClass)) {
			handlers.addAll(getOpenFileClassToHandler().get(fileType).get(
					candidateClass));
		}
		return handlers;
	}

	public Set<FileType> getSaveFileTypes() {
		return getSaveFileClassToHandler().keySet();
	}

	public Set<FileType> getSaveFileTypesFor(Class<?> destinationClass) {
		Set<FileType> fileTypes = new LinkedHashSet<FileType>();
		for (Class<?> candidateClass : findAllParentClasses(destinationClass)) {
			fileTypes.addAll(getSaveClassToTypes().get(candidateClass));
		}
		return fileTypes;
	}

	public Set<DataflowPersistenceHandler> getSaveHandlersForType(
			FileType fileType, Class<?> destinationClass) {
		Set<DataflowPersistenceHandler> handlers = new LinkedHashSet<DataflowPersistenceHandler>();
		for (Class<?> candidateClass : findAllParentClasses(destinationClass)) {
			handlers.addAll(getSaveFileClassToHandler().get(fileType).get(
					candidateClass));
		}
		return handlers;
	}

	@SuppressWarnings("unchecked")
	private synchronized void createCollections() {
		openFileClassToHandler = LazyMap.decorate(new HashMap(), MAP_FACTORY);
		openFileToHandler = LazyMap.decorate(new HashMap(), SET_FACTORY);
		openClassToTypes = LazyMap.decorate(new HashMap(), SET_FACTORY);

		saveFileClassToHandler = LazyMap.decorate(new HashMap(), MAP_FACTORY);
		saveFileToHandler = LazyMap.decorate(new HashMap(), SET_FACTORY);
		saveClassToTypes = LazyMap.decorate(new HashMap(), SET_FACTORY);
	}

	private synchronized Map<Class<?>, Set<FileType>> getOpenClassToTypes() {
		return openClassToTypes;
	}

	private synchronized Map<FileType, Map<Class<?>, Set<DataflowPersistenceHandler>>> getOpenFileClassToHandler() {
		return openFileClassToHandler;
	}

	private Map<FileType, Set<DataflowPersistenceHandler>> getOpenFileToHandler() {
		return openFileToHandler;
	}

	private synchronized Map<Class<?>, Set<FileType>> getSaveClassToTypes() {
		return saveClassToTypes;
	}

	private synchronized Map<FileType, Map<Class<?>, Set<DataflowPersistenceHandler>>> getSaveFileClassToHandler() {
		return saveFileClassToHandler;
	}

	private synchronized void updateColletions() {
		createCollections();
		for (DataflowPersistenceHandler handler : this.getInstances()) {
			for (FileType openFileType : handler.getOpenFileTypes()) {
				Set<DataflowPersistenceHandler> set = openFileToHandler
						.get(openFileType);
				set.add(handler);
				for (Class<?> openClass : handler.getOpenSourceTypes()) {
					openFileClassToHandler.get(openFileType).get(openClass)
							.add(handler);
					openClassToTypes.get(openClass).add(openFileType);
				}
			}
			for (FileType saveFileType : handler.getSaveFileTypes()) {
				saveFileToHandler.get(saveFileType).add(handler);
				for (Class<?> saveClass : handler.getSaveDestinationTypes()) {
					saveFileClassToHandler.get(saveFileType).get(saveClass)
							.add(handler);
					saveClassToTypes.get(saveClass).add(saveFileType);
				}
			}
		}
	}

	private static class DataflowPersistenceHandlerRegistryHolder {
		private final static DataflowPersistenceHandlerRegistry SINGLETON = new DataflowPersistenceHandlerRegistry();
	}

	private static class MapFactory implements Factory {
		@SuppressWarnings("unchecked")
		public Object create() {
			return LazyMap.decorate(new HashMap(), SET_FACTORY);
		}
	}

	private static class SetFactory implements Factory {
		@SuppressWarnings("unchecked")
		public Object create() {
			return new LinkedHashSet();
		}
	}

	private class SPIRegistryObserver implements Observer<SPIRegistryEvent> {
		public void notify(Observable<SPIRegistryEvent> sender,
				SPIRegistryEvent message) {
			if (message == SPIRegistry.UPDATED) {
				updateColletions();
			}
		}
	}

}
