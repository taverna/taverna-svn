/**
 * Container represented by an image
 * @class URILinkContainer
 * @extends WireIt.FormContainer
 * @constructor
 * @param {Object} options
 * @param {WireIt.Layer} layer
 */
WireIt.URILinkContainer = function(options, layer) {
	WireIt.URILinkContainer.superclass.constructor.call(this, options, layer);
};

YAHOO.lang.extend(WireIt.URILinkContainer, WireIt.FormContainer, {

	/**
	 * @method setOptions
	 * @param {Object} options the options object
	 */
	setOptions: function(options) {
		WireIt.URILinkContainer.superclass.setOptions.call(this, options);

		this.options.xtype = "WireIt.URILinkContainer";

		this.options.className = options.className || "WireIt-Container URI Link Container";

		// Overwrite default value for options:
		this.options.resizable = (typeof options.resizable == "undefined") ? false : options.resizable;
		
		this.options.fields = this.options.fields || [];
		this.uriField = {
			"type": "uriLink",
			"inputParams" : {
				"name":"uri",
				"value":options.uri || "Link will go here"
			}
		};
		this.options.fields.push(this.uriField);
		//console.log(this.options.fields)
	},
	
	endsWith: function(str, suffix) {
		return str.indexOf(suffix, str.length - suffix.length) !== -1;
	},

	/**
	 * @method render
	 */
	render: function() {
		WireIt.URILinkContainer.superclass.render.call(this);
		//this.uriField.setValue("hello World",true);
	}

});