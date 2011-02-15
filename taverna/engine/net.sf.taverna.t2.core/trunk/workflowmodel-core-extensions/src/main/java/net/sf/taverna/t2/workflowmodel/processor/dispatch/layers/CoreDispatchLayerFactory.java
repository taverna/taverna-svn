/*******************************************************************************
 * Copyright (C) 2011 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.workflowmodel.processor.dispatch.layers;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayerFactory;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.Stop;

/**
 * Factory for creating core dispatch layers.
 * 
 * The core dispatch layers are :
 * <ul>
 * <li>ErrorBounce</li>
 * <li>Parallelize</li>
 * <li>Failover</li>
 * <li>Retry</li>
 * <li>Stop</li>
 * <li>Invoke</li>
 * <li>Loop</li>
 * <li>IntermediateProvenance</li>
 * </ul>
 * 
 * @author David Withers
 */
public class CoreDispatchLayerFactory implements DispatchLayerFactory {

	private static final URI parallelizeLayer = URI.create(Parallelize.URI);
	private static final URI errorBounceLayer = URI.create(ErrorBounce.URI);
	private static final URI failoverLayer = URI.create(Failover.URI);
	private static final URI retryLayer = URI.create(Retry.URI);
	private static final URI invokeLayer = URI.create(Invoke.URI);
	private static final URI loopLayer = URI.create(Loop.URI);
	private static final URI intermediateProvenanceLayer = URI.create(IntermediateProvenance.URI);
	private static final URI stopLayer = URI.create(Stop.URI);

	private final static Set<URI> dispatchLayerURIs = new HashSet<URI>();

	static {
		dispatchLayerURIs.add(parallelizeLayer);
		dispatchLayerURIs.add(errorBounceLayer);
		dispatchLayerURIs.add(failoverLayer);
		dispatchLayerURIs.add(retryLayer);
		dispatchLayerURIs.add(invokeLayer);
		dispatchLayerURIs.add(loopLayer);
		dispatchLayerURIs.add(intermediateProvenanceLayer);
		dispatchLayerURIs.add(stopLayer);
	}

	@Override
	public DispatchLayer<?> createDispatchLayer(URI uri) {
		DispatchLayer<?> dispatchLayer = null;
		if (parallelizeLayer.equals(uri)) {
			dispatchLayer = new Parallelize();
		} else if (errorBounceLayer.equals(uri)) {
			dispatchLayer = new ErrorBounce();
		} else if (failoverLayer.equals(uri)) {
			dispatchLayer = new Failover();
		} else if (retryLayer.equals(uri)) {
			dispatchLayer = new Retry();
		} else if (invokeLayer.equals(uri)) {
			dispatchLayer = new Invoke();
		} else if (loopLayer.equals(uri)) {
			dispatchLayer = new Loop();
		} else if (intermediateProvenanceLayer.equals(uri)) {
			dispatchLayer = new IntermediateProvenance();
		} else if (stopLayer.equals(uri)) {
			dispatchLayer = new Stop();
		}
		return dispatchLayer;
	}

	@Override
	public Object createDispatchLayerConfiguration(URI uri) {
		Object dispatchLayerConfiguration = null;
		if (parallelizeLayer.equals(uri)) {
			dispatchLayerConfiguration = new ParallelizeConfig();
		} else if (retryLayer.equals(uri)) {
			dispatchLayerConfiguration = new RetryConfig();
		} else if (loopLayer.equals(uri)) {
			dispatchLayerConfiguration = new LoopConfiguration();
		}
		return dispatchLayerConfiguration;
	}

	@Override
	public Set<URI> getDispatchLayerURIs() {
		return dispatchLayerURIs;
	}

}
