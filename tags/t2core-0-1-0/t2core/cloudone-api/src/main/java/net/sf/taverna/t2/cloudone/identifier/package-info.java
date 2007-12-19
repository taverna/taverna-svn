/**
 * An {@link net.sf.taverna.t2.cloudone.identifier.EntityIdentifier} is
 * identifying an {@link net.sf.taverna.t2.cloudone.entity.Entity}. There is
 * one identifier class for each Entity class, such as
 * {@link net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier},
 * {@link net.sf.taverna.t2.cloudone.identifier.ErrorDocumentIdentifier} and
 * {@link net.sf.taverna.t2.cloudone.identifier.EntityListIdentifier},
 * with the exception of {@link net.sf.taverna.t2.cloudone.entity.Literal},
 * which is its own identifier.
 * <p>
 * All identifiers are serialisable as URI strings. The
 * {@link net.sf.taverna.t2.cloudone.identifier.EntityIdentifiers} utility
 * class can help reconstruct the right instance from an URI.
 *
 */
package net.sf.taverna.t2.cloudone.identifier;

