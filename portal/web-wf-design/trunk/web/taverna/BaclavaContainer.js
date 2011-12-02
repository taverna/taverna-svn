/**
 * Container holding a URI to A Baclava File.
 * The same link is displayed twice, once as a simple link and one as a link to the DsiplayBaclava servlet.
 * Both share the same value "uri".
 * Either cn be removed by commenting out the definition in setOptions (including the relative push)
 * @class BaclavaContainer
 * @extends WireIt.FormContainer
 * @constructor
 * @param {Object} options
 * @param {WireIt.Layer} layer
 */
WireIt.BaclavaContainer = function(options, layer) {
	WireIt.BaclavaContainer.superclass.constructor.call(this, options, layer);
};

YAHOO.lang.extend(WireIt.BaclavaContainer, WireIt.FormContainer, {

	/**
	 * @method setOptions
	 * @param {Object} options the options object
	 */
	setOptions: function(options) {
		WireIt.BaclavaContainer.superclass.setOptions.call(this, options);

		this.options.xtype = "WireIt.BaclavaContainer";

		this.options.className = options.className || "WireIt-Container URI Link Container";

		// Overwrite default value for options:
		this.options.resizable = (typeof options.resizable == "undefined") ? false : options.resizable;
		
		//Add a direct link
		this.options.fields = this.options.fields || [];
		this.uriField = {
			"type": "uriLink",
			"inputParams" : {
				"name":"uri",    //Both fields intentionally have the same name so they share the same data
				"value":options.uri || "",
			},
		};
		this.options.fields.push(this.uriField);
		
		//Add a display Baclava Link
		this.showField = {
			"type": "baclavaShowLink",
			"inputParams" : {
				"name":"uri",    //Both fields intentionally have the same name so they share the same data
				"value":options.uri || "",
			},
		};
		this.options.fields.push(this.showField);

	},

	/**
	 * This function does all the styling for the terminals so that all conatiners will be the same.
	 * Style changes should be here in which case they effect future pipes as well.
	 * Individual ones can be set in the language defintion which overrides any defaults set here.
	 * @method render
	 */
	render: function() {
		for(var i = 0 ; i < this.options.terminals.length ; i++) {
			var terminal = this.options.terminals[i];
			if (terminal.name == "output") {
				terminal.offsetPosition = terminal.offsetPosition || {"right": -14, "top": 25};
				terminal.alwaysSrc = terminal.alwaysSrc || true;
				terminal.wireConfig = terminal.wireConfig || { drawingMethod: "arrows", color: "#FF0000", bordercolor:"#FF00FF"};
				terminal.ddConfig = terminal.ddConfig || {"type": "outputBaclava", "allowedTypes": ["inputBaclava"]};				
			};
			if (terminal.name == "input") {
				terminal.offsetPosition = terminal.offsetPosition || {"left": -14, "top": 25 };
				terminal.nMaxWires = terminal.nMaxWires || 1;
				terminal.wireConfig = terminal.wireConfig || { "drawingMethod": "arrows", "color": "#FF0000", "bordercolor":"#FF00FF"};
				terminal.ddConfig = terminal.ddConfig || {"type": "inputBaclava", "allowedTypes": ["outputURL", "outputBaclava"] };
			};
		};
		WireIt.BaclavaContainer.superclass.render.call(this);
	},

});

//   *******   Display Baclava Link  ******* //
(function() {

/**
 * Create a uneditable field where the uri is show as the parameter to the Baclava Display servlet.
 * Added Options:
 * <ul>
 *    <li>visu: inputEx visu type</li>
 * </ul>
 * Based on inputEx.UneditableField
 * @class inputEx.BaclavaShowLinkField
 * @extends inputEx.Field
 * @constructor
 * @param {Object} options inputEx.Field options object
 */
inputEx.BaclavaShowLinkField = function(options) {
	inputEx.BaclavaShowLinkField.superclass.constructor.call(this,options);
};
YAHOO.lang.extend(inputEx.BaclavaShowLinkField, inputEx.Field, {

	/**
	 * Set the default values of the options
	 * @param {Object} options Options object (inputEx inputParams) as passed to the constructor
	 */
	setOptions: function(options) {
		inputEx.BaclavaShowLinkField.superclass.setOptions.call(this,options);
		this.options.visu = options.visu;
	},

	/**
	 * Store the value and update the visu
	 * @param {Any} val The value that will be sent to the visu
	 * @param {boolean} [sendUpdatedEvt] (optional) Wether this setValue should fire the updatedEvt or not (default is true, pass false to NOT send the event)
	 */
	setValue: function(val, sendUpdatedEvt) {
		this.value = val;
		//console.log(this.value);
		
		var link = "The Link will go here.";
		
		 if (this.value){
			var text = "Fancy Baclava Output";
			//Link needs to be changed to fancy link.
			link = '<a href="DisplayBaclavaFile?baclava_document_url=' + this.value + '" target="_blank">' + text + '</a>';
		}

		inputEx.renderVisu(this.options.visu, link, this.fieldContainer);

		inputEx.BaclavaShowLinkField.superclass.setValue.call(this, val, sendUpdatedEvt);
	},

	/**
	 * Return the stored value
	 * @return {Any} The previously stored value
	 */
	getValue: function() {
		return this.value;
	}

});

// Register this class as "url" type
inputEx.registerType("baclavaShowLink", inputEx.BaclavaShowLinkField);

})();