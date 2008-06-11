package net.sf.taverna.t2.reference.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import net.sf.taverna.raven.spi.InstanceRegistry;
import net.sf.taverna.raven.spi.InstanceRegistryListener;
import net.sf.taverna.t2.reference.ExternalReferenceBuilderSPI;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.ExternalReferenceTranslatorSPI;
import net.sf.taverna.t2.reference.ReferenceContext;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.ReferenceSetAugmentationException;
import net.sf.taverna.t2.reference.ReferenceSetAugmentor;
import net.sf.taverna.t2.reference.ReferenceSetAugmentorCallback;

/**
 * Implementation of ReferenceSetAugmentor using Dijkstra's shortest path
 * algorithm over a type graph built from SPI instance registries of reference
 * builders and reference translators.
 * 
 * @author Tom Oinn
 * 
 */
public class ReferenceSetAugmentorImpl implements ReferenceSetAugmentor {

	// An instance registry of ExternalReferenceBuilderSPI instances used to
	// construct ExternalReferenceSPI instances from byte streams
	private InstanceRegistry<ExternalReferenceBuilderSPI<?>> builders;

	// An instance registry of ExternalReferenceTranslatorSPI instances used to
	// construct ExternalReferenceSPI instances from existing
	// ExternalReferenceSPI instances.
	private InstanceRegistry<ExternalReferenceTranslatorSPI<?, ?>> translators;

	// A private listener used to trigger re-compilation of the shortest paths
	// from each node to each other node in the known types set
	private InstanceRegistryListener registryListener = new InstanceRegistryListener() {
		/**
		 * Call the updateAdjacencyList on the enclosing type when any change
		 * occurs in the SPIs
		 */
		@SuppressWarnings("unchecked")
		public void instanceRegistryUpdated(InstanceRegistry theRegistry) {
			update();
		}
	};

	private final Set<Class<ExternalReferenceSPI>> knownReferenceTypes = new HashSet<Class<ExternalReferenceSPI>>();
	@SuppressWarnings("unchecked")
	private final Map<Class<ExternalReferenceSPI>, Set<ExternalReferenceTranslatorSPI>> adjacencySets = new HashMap<Class<ExternalReferenceSPI>, Set<ExternalReferenceTranslatorSPI>>();

	private final Map<Class<ExternalReferenceSPI>, ShortestPathSolver> solvers = new HashMap<Class<ExternalReferenceSPI>, ShortestPathSolver>();

	/**
	 * Default constructor to make life easier when using Spring. To be
	 * functional this implementation should be injected with InstanceRegistry
	 * implementations containing lists of known implementations of the
	 * ExternalReferenceBuilderSPI and ExternalReferenceTranslatorSPI
	 * interfaces.
	 */
	public ReferenceSetAugmentorImpl() {
		//
	}

	/**
	 * Inject an instance registry containing all known implementations of
	 * ExternalReferenceBuilderSPI *
	 * 
	 * @throws IllegalStateException
	 *             if this has already been set, the instance registries should
	 *             only be set on bean construction.
	 */
	public synchronized void setBuilderRegistry(
			InstanceRegistry<ExternalReferenceBuilderSPI<?>> theRegistry) {
		if (this.builders == null) {
			this.builders = theRegistry;
			theRegistry.addRegistryListener(registryListener);
			update();
		} else {
			throw new IllegalStateException(
					"Can't inject the external reference builder registry "
							+ "multiple times.");
		}
	}

	/**
	 * Inject an instance registry containing all known implementations of
	 * ExternalReferenceTranslatorSPI
	 * 
	 * @throws IllegalStateException
	 *             if this has already been set, the instance registries should
	 *             only be set on bean construction.
	 */
	public synchronized void setTranslatorRegistry(
			InstanceRegistry<ExternalReferenceTranslatorSPI<?, ?>> theRegistry) {
		if (this.translators == null) {
			this.translators = theRegistry;
			theRegistry.addRegistryListener(registryListener);
			update();
		} else {
			throw new IllegalStateException(
					"Can't inject the translator registry multiple times.");
		}
	}

	@SuppressWarnings("unchecked")
	protected synchronized final void update() {
		knownReferenceTypes.clear();
		solvers.clear();
		adjacencySets.clear();
		for (ExternalReferenceBuilderSPI erb : builders) {
			knownReferenceTypes.add(erb.getReferenceType());
		}
		for (ExternalReferenceTranslatorSPI ert : translators) {
			knownReferenceTypes.add(ert.getSourceReferenceType());
			knownReferenceTypes.add(ert.getTargetReferenceType());
			Set<ExternalReferenceTranslatorSPI> adjacentTo = adjacencySets
					.get(ert.getTargetReferenceType());
			if (adjacentTo == null) {
				adjacentTo = new HashSet<ExternalReferenceTranslatorSPI>();
				adjacencySets.put(ert.getTargetReferenceType(), adjacentTo);
			}
			// TODO - does not check for duplicate edges in the resultant graph,
			// should do!
			// This isn't so critical as the duplicate paths will be ignored
			// later on in the algorithm but it's not ideal
			adjacentTo.add(ert);
		}
		for (Class<ExternalReferenceSPI> type : knownReferenceTypes) {
			solvers.put(type, new ShortestPathSolver(type));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public final Set<ExternalReferenceSPI> augmentReferenceSet(
			ReferenceSet references,
			Set<Class<ExternalReferenceSPI>> targetReferenceTypes,
			ReferenceContext context) throws ReferenceSetAugmentationException {
		// Synchronize on the reference set itself
		synchronized (references) {

			// First check whether we actually need to modify the reference set
			// at
			// all - it's perfectly valid to call the augmentor when nothing
			// actually needs to be done (ideally you wouldn't do this, but it's
			// likely to happen)
			for (ExternalReferenceSPI er : references.getExternalReferences()) {
				if (targetReferenceTypes.contains(er.getClass())) {
					return new HashSet<ExternalReferenceSPI>();
				}
			}

			// Need to perform augmentation if we reach this point
			List<TranslationPath> candidatePaths = new ArrayList<TranslationPath>();
			for (Class<ExternalReferenceSPI> target : targetReferenceTypes) {
				ShortestPathSolver solver = solvers.get(target);
				if (solver != null) {
					for (TranslationPath path : solver.getTranslationPaths()) {
						for (ExternalReferenceSPI er : references
								.getExternalReferences()) {
							if (er.getClass().equals(path.getSourceType())) {
								candidatePaths.add(path);
							}
						}
						for (TranslationPath dereferenceBasedPath : path
								.getDereferenceBasedPaths(references)) {
							candidatePaths.add(dereferenceBasedPath);
						}
					}
				}
			}
			// Now add candidate paths to represent a no-translator 'direct from
			// byte stream source' path for each target type compatible
			// reference builder
			for (ExternalReferenceBuilderSPI builder : builders) {
				if (targetReferenceTypes.contains(builder.getReferenceType())) {
					// The builder can construct one of the target types, add
					// paths for all possible pairs of 'de-reference existing
					// refence' and the builder
					for (ExternalReferenceSPI er : references
							.getExternalReferences()) {
						TranslationPath newPath = new TranslationPath();
						newPath.initialBuilder = builder;
						newPath.sourceReference = er;
						candidatePaths.add(newPath);
					}
				}
			}

			// Got a list of candidate paths sorted by estimated overall path
			// cost
			Collections.sort(candidatePaths);

			if (candidatePaths.isEmpty()) {
				throw new ReferenceSetAugmentationException(
						"No candidate translation paths were found");
			} else {
				for (TranslationPath path : candidatePaths) {
					try {
						return path.doTranslation(references, context);
					} catch (Exception ex) {
						// Use next path...
					}
				}
				throw new ReferenceSetAugmentationException(
						"All paths threw exceptions, can't perform augmentation");
			}

		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final void augmentReferenceSetAsynch(final ReferenceSet references,
			final Set<Class<ExternalReferenceSPI>> targetReferenceTypes,
			final ReferenceContext context,
			final ReferenceSetAugmentorCallback callback)
			throws ReferenceSetAugmentationException {
		Runnable r = new Runnable() {
			public void run() {
				try {
					callback.augmentationCompleted(augmentReferenceSet(
							references, targetReferenceTypes, context));

				} catch (ReferenceSetAugmentationException rsae) {
					callback.augmentationFailed(rsae);
				}
			}
		};
		executeRunnable(r);
	}

	/**
	 * Schedule a runnable for execution - current naive implementation uses a
	 * new thread and executes immediately, but this is where any thread pool
	 * logic would go if we wanted to add that.
	 * 
	 * @param r
	 */
	private void executeRunnable(Runnable r) {
		new Thread(r).start();
	}

	/**
	 * A path from one external reference to another along with a total
	 * estimated path cost through one or more reference translators.
	 */
	class TranslationPath implements Comparable<TranslationPath>,
			Iterable<ExternalReferenceTranslatorSPI<?, ?>> {

		List<ExternalReferenceTranslatorSPI<?, ?>> translators = new ArrayList<ExternalReferenceTranslatorSPI<?, ?>>();
		ExternalReferenceBuilderSPI<?> initialBuilder = null;
		ExternalReferenceSPI sourceReference = null;

		@SuppressWarnings("unchecked")
		public Set<ExternalReferenceSPI> doTranslation(ReferenceSet rs,
				ReferenceContext context) {
			Set<ExternalReferenceSPI> results = new HashSet<ExternalReferenceSPI>();
			// Firstly check whether we have an initial reference and builder
			// defined
			ExternalReferenceSPI currentReference = null;
			if (initialBuilder != null && sourceReference != null) {
				ExternalReferenceSPI builtReference = initialBuilder
						.createReference(sourceReference.openStream(context),
								context);
				results.add(builtReference);
				currentReference = builtReference;
			}
			if (translators.isEmpty() == false && currentReference == null) {
				// If there are translators in the path (there may not be if
				// this is a pure 'dereference and build' type path) and the
				// currentReference hasn't been set then search the existing
				// references for an appropriate starting point for the
				// translation.
				for (ExternalReferenceSPI er : rs.getExternalReferences()) {
					if (er.getClass().equals(
							translators.get(0).getSourceReferenceType())) {
						currentReference = er;
						break;
					}
				}
			}
			if (currentReference == null) {
				throw new RuntimeException(
						"Can't locate a starting reference for the"
								+ " translation path");
			} else {
				for (ExternalReferenceTranslatorSPI translator : translators) {
					ExternalReferenceSPI translatedReference = translator
							.createReference(currentReference, context);
					results.add(translatedReference);
					currentReference = translatedReference;
				}
			}
			return results;
		}

		/**
		 * Sum of translation costs of all translators in path
		 */
		public float getPathCost() {
			float cost = 0.0f;
			for (ExternalReferenceTranslatorSPI<?, ?> ert : this) {
				cost += ert.getTranslationCost();
			}
			// If the source reference and initial builder are non-null then
			// we're going to start this translation path by downloading a byte
			// stream from the specified (current) reference and using it to
			// construct the starting point for the translation path via the
			// specified builder.
			if (sourceReference != null) {
				cost += sourceReference.getResolutionCost();
			}
			if (initialBuilder != null) {
				cost += initialBuilder.getConstructionCost();
			}
			return cost;
		}

		/**
		 * Return a list of translation paths based on this one but which start
		 * at an existing reference within the supplied reference set. Will only
		 * function if there is a reference builder registered that can build
		 * the initial reference type used by this translation path, otherwise
		 * it returns an empty list.
		 * 
		 * @param rs
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public List<TranslationPath> getDereferenceBasedPaths(ReferenceSet rs) {
			List<TranslationPath> results = new ArrayList<TranslationPath>();
			for (ExternalReferenceBuilderSPI erb : builders) {
				// Check for each reference builder to see if it can build the
				// source type for this path
				if (erb.getReferenceType().equals(this.getSourceType())) {
					// The builder can construct the type used by the start of
					// this translation path, so we can in general create a path
					// from a fooreference to the target by de-referencing the
					// fooreference and building the start type from it.
					for (ExternalReferenceSPI er : rs.getExternalReferences()) {
						// For each external reference in the existing reference
						// set, check whether that type is already going to be
						// created in the translation path - if so then there's
						// not much point in emiting the modified path, as you'd
						// have something like bytes->a->b->a->result which
						// wouldn't make any sense
						boolean overlapsExistingType = false;
						for (ExternalReferenceTranslatorSPI translationStep : this) {
							if (translationStep.getSourceReferenceType()
									.equals(er.getClass())) {
								overlapsExistingType = true;
								break;
							}
						}
						if (!overlapsExistingType) {
							// The type wasn't found anywhere within the
							// translation path, so we're not generating
							// obviously stupid candidate paths.
							TranslationPath newPath = new TranslationPath();
							newPath.translators = this.translators;
							newPath.initialBuilder = erb;
							newPath.sourceReference = er;
							results.add(newPath);
						}
					}
				}
			}
			return results;
		}

		public List<ExternalReferenceTranslatorSPI<?, ?>> pathSteps() {
			return translators;
		}

		/**
		 * Order by total path cost
		 */
		public int compareTo(TranslationPath tp) {
			if (tp.getPathCost() > this.getPathCost()) {
				return -1;
			} else if (tp.getPathCost() < this.getPathCost()) {
				return 1;
			} else {
				return 0;
			}
		}

		/**
		 * Wrap translator list iterator for convenience
		 */
		public Iterator<ExternalReferenceTranslatorSPI<?, ?>> iterator() {
			return translators.iterator();
		}

		public Class<? extends ExternalReferenceSPI> getSourceType() {
			if (translators.isEmpty() == false) {
				return translators.get(0).getSourceReferenceType();
			} else if (this.sourceReference != null) {
				return this.sourceReference.getClass();
			} else {
				return null;
			}
		}

		public Class<? extends ExternalReferenceSPI> getTargetType() {
			if (translators.isEmpty() == false) {
				return translators.get(translators.size() - 1)
						.getTargetReferenceType();
			} else if (this.initialBuilder != null) {
				return this.initialBuilder.getReferenceType();
			} else {
				return null;
			}
		}

	}

	class ShortestPathSolver {

		private Map<Class<ExternalReferenceSPI>, Class<ExternalReferenceSPI>> predecessors;
		private Map<Class<ExternalReferenceSPI>, ExternalReferenceTranslatorSPI<?, ?>> translators;
		private Map<Class<ExternalReferenceSPI>, Float> shortestDistances;
		private final Comparator<Class<ExternalReferenceSPI>> shortestDistanceComparator = new Comparator<Class<ExternalReferenceSPI>>() {
			public int compare(Class<ExternalReferenceSPI> left,
					Class<ExternalReferenceSPI> right) {
				float shortestDistanceLeft = shortestDistances.get(left);
				float shortestDistanceRight = shortestDistances.get(right);
				if (shortestDistanceLeft > shortestDistanceRight) {
					return +1;
				} else if (shortestDistanceLeft < shortestDistanceRight) {
					return -1;
				} else {
					return left.getCanonicalName().compareTo(
							right.getCanonicalName());
				}
			}
		};
		private final PriorityQueue<Class<ExternalReferenceSPI>> unsettledNodes = new PriorityQueue<Class<ExternalReferenceSPI>>(
				10, shortestDistanceComparator);
		private final Set<Class<ExternalReferenceSPI>> settledNodes = new HashSet<Class<ExternalReferenceSPI>>();

		private final List<TranslationPath> translationPaths = new ArrayList<TranslationPath>();

		public List<TranslationPath> getTranslationPaths() {
			return this.translationPaths;
		}

		public ShortestPathSolver(Class<ExternalReferenceSPI> targetType) {
			predecessors = new HashMap<Class<ExternalReferenceSPI>, Class<ExternalReferenceSPI>>();
			translators = new HashMap<Class<ExternalReferenceSPI>, ExternalReferenceTranslatorSPI<?, ?>>();
			shortestDistances = new HashMap<Class<ExternalReferenceSPI>, Float>();
			setShortestDistance(targetType, 0.0f);
			unsettledNodes.add(targetType);
			while (unsettledNodes.isEmpty() == false) {
				Class<ExternalReferenceSPI> u = extractMin();
				settledNodes.add(u);
				relaxNeighbours(u);
			}
			for (Class<ExternalReferenceSPI> c : settledNodes) {
				if (c.equals(targetType) == false) {
					// Don't calculate a path to itself!
					TranslationPath p = new TranslationPath();
					Class<ExternalReferenceSPI> node = c;
					while (predecessors.get(node) != null) {
						p.pathSteps().add(translators.get(node));
						// Recurse, should terminate at the target type
						node = predecessors.get(node);
					}
				}
			}
			Collections.sort(translationPaths);
		}

		@SuppressWarnings("unchecked")
		private void relaxNeighbours(Class<ExternalReferenceSPI> u) {
			Set<Class<ExternalReferenceSPI>> alreadySeen = new HashSet<Class<ExternalReferenceSPI>>();
			for (ExternalReferenceTranslatorSPI ert : adjacencySets.get(u)) {
				// all the translators that translate *to* u
				Class<ExternalReferenceSPI> v = ert.getSourceReferenceType();
				if (alreadySeen.contains(v) == false && isSettled(v) == false) {
					// Avoid duplicate edges, always take the first one where
					// such duplicates exist
					alreadySeen.add(v);
					if (getShortestDistance(v) > getShortestDistance(u)
							+ ert.getTranslationCost()) {
						setShortestDistance(v, getShortestDistance(u)
								+ ert.getTranslationCost());
						setPredecessor(v, u, ert);
						unsettledNodes.add(v);
					}
				}
			}
		}

		private boolean isSettled(Class<ExternalReferenceSPI> node) {
			return settledNodes.contains(node);
		}

		private void setShortestDistance(Class<ExternalReferenceSPI> node,
				float distance) {
			shortestDistances.put(node, distance);
		}

		private float getShortestDistance(Class<ExternalReferenceSPI> node) {
			Float d = shortestDistances.get(node);
			return (d == null) ? Float.MAX_VALUE : d;
		}

		private Class<ExternalReferenceSPI> extractMin() {
			return unsettledNodes.poll();
		}

		private void setPredecessor(Class<ExternalReferenceSPI> child,
				Class<ExternalReferenceSPI> parent,
				ExternalReferenceTranslatorSPI<?, ?> translator) {
			predecessors.put(child, parent);
			translators.put(child, translator);
		}

	}

}
