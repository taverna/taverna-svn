/**
 * An {@link net.sf.taverna.t2.cloudone.entity.Entity} is anything that can
 * be stored in a {@link net.sf.taverna.t2.cloudone.datamanager.DataManager}
 * and contained by an {@link net.sf.taverna.t2.cloudone.entity.EntityList}
 * (which itself is an Entity).
 * The {@link net.sf.taverna.t2.cloudone.entity.DataDocument} contains
 * {@link net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme}s for binaries,
 * {@link net.sf.taverna.t2.cloudone.entity.ErrorDocument} represents errors
 * and {@link net.sf.taverna.t2.cloudone.entity.Literal}s are used
 * for numbers and small String values. Larger values can be
 * stored in a {@link net.sf.taverna.t2.cloudone.datamanager.BlobStore}.
 *
 */
package net.sf.taverna.t2.cloudone.entity;

