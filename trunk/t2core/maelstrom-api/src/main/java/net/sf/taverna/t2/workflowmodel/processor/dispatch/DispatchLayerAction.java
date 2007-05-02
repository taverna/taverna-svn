package net.sf.taverna.t2.workflowmodel.processor.dispatch;

/**
 * Defines the possible actions a dispatch layer can take on receiving a message
 * from an adjacent layer
 * 
 * TODO - should this maybe be moved into a metadata file? It's not obvious that
 * this is the best way to describe the behaviour of the layer.
 * 
 * @author Tom Oinn
 * 
 */
public enum DispatchLayerAction {

	/**
	 * The layer ignores the message, passing it directly through to the
	 * appropriate adjacent layer
	 */
	PASSTHROUGH,

	/**
	 * The layer passes all messages through but in some ways alters its
	 * internal state as a result, an example might be a retry layer which only
	 * observes job messages heading downwards but logs them to enable
	 * appropriate handling of error messages coming back up.
	 */
	OBSERVE,

	/**
	 * The layer intercepts and alters the message before passing it onto the
	 * next layer in the sequence. An example of this would be abstract to
	 * concrete binding altering the service set on the way downstream. This
	 * value should only be used if the message is altered, if the routing is
	 * changed i.e. an error message being discarded in a retry layer the ACT
	 * value is applicable.
	 */
	REWRITE,

	/**
	 * The layer intercepts the message, and may either relay the message to the
	 * next layer or perform some other action. If there are any circumstances
	 * under which a layer does not pass the message to the next layer the
	 * appropriate method must return this value.
	 */
	ACT,

	/**
	 * The layer intercepts the message and never relays it to the next layer.
	 * The layer may respond to the message by producing other messages in turn,
	 * i.e. the parallelize operator consumes the queue and generates JOB
	 * messages. If this property is the defined behaviour of a message for a
	 * given layer and the corresponding canProduce property is false this layer
	 * will never send that message class either directly or indirectly to the
	 * next layer.
	 */
	ACTNORELAY,

	/**
	 * The layer must never receive this message, any messages coming into the
	 * layer marked with this value must cause a runtime error. If this property
	 * is the defined behaviour of a message for a given layer and the
	 * corresponding canProduce property is false this layer will never send
	 * that message class either directly or indirectly to the next layer.
	 */
	FORBIDDEN;

}
