/**
 * Container represented by a uri as a clickable link.
 * Can be used both as a passt=Through and as an Output.
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
				"value":options.uri || "",
			},
		};
		this.options.fields.push(this.uriField);
		//console.log(this.options.fields)
	},
	
	endsWith: function(str, suffix) {
		return str.indexOf(suffix, str.length - suffix.length) !== -1;
	},

	/**
	 * This method formats the terminals.using defualts that can be overwritten in the language definitions.
	 * @method render
	 */
	render: function() {
		for(var i = 0 ; i < this.options.terminals.length ; i++) {
			var terminal = this.options.terminals[i];
			if (terminal.name == "output") {
				terminal.offsetPosition = terminal.offsetPosition || {"right": -14, "top": 25};
				terminal.alwaysSrc = terminal.alwaysSrc || true;
				terminal.wireConfig = terminal.wireConfig || { drawingMethod: "arrows", color: "#EE11EE", bordercolor:"#FF00FF"};
				terminal.ddConfig = terminal.ddConfig || {"type": "outputBaclava", "allowedTypes": ["inputBaclava"]};				
			};
			if (terminal.name == "input") {
				terminal.offsetPosition = terminal.offsetPosition || {"left": -14, "top": 25 };
				terminal.nMaxWires = terminal.nMaxWires || 1;
				terminal.wireConfig = terminal.wireConfig || { drawingMethod: "arrows", color: "#FF0000", bordercolor:"#FF00FF"};
				terminal.ddConfig = terminal.ddConfig || {"type": "outputURL", "allowedTypes": ["inputURL","inputDepthZero","inputDepthOne", "InputBaclava"]};				
			};
		};
		WireIt.URILinkContainer.superclass.render.call(this);
		//this.uriField.setValue("hello World",true);
	},

});