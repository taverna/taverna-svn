(function() {

/**
 * Create a uneditable field where you can stick the html you want
 * Added Options:
 * <ul>
 *    <li>visu: inputEx visu type</li>
 * </ul>
 * Based on inputEx.UneditableField
 * @class inputEx.URILinkField
 * @extends inputEx.Field
 * @constructor
 * @param {Object} options inputEx.Field options object
 */
inputEx.URILinkField = function(options) {
	inputEx.URILinkField.superclass.constructor.call(this,options);
};
YAHOO.lang.extend(inputEx.URILinkField, inputEx.Field, {

	/**
	 * Set the default values of the options
	 * @param {Object} options Options object (inputEx inputParams) as passed to the constructor
	 */
	setOptions: function(options) {
		inputEx.URILinkField.superclass.setOptions.call(this,options);
		this.options.visu = options.visu;
	},

	endsWith: function(str, suffix) {
		return str.indexOf(suffix, str.length - suffix.length) !== -1;
	},

	/**
	 * Store the value and update the visu
	 * @param {Any} val The value that will be sent to the visu
	 * @param {boolean} [sendUpdatedEvt] (optional) Wether this setValue should fire the updatedEvt or not (default is true, pass false to NOT send the event)
	 */
	setValue: function(val, sendUpdatedEvt) {
		this.value = val;
		//console.log(this.value);
		
		var link = "No Value set"
		
		 if (this.value){
			var text = this.value
			if (this.endsWith(this.value, "BaclavaOutput.xml")){
					text = "Baclava Output";
				}
			link = '<a href="' + this.value + '" target="_blank">' + text + '</a>';
		}

		inputEx.renderVisu(this.options.visu, link, this.fieldContainer);

		inputEx.URILinkField.superclass.setValue.call(this, val, sendUpdatedEvt);
	},

	/**
	 * Return the stored value
	 * @return {Any} The previously stored value
	 */
	getValue: function() {
		//console.log(this.value);
		return this.value;
	}

});

// Register this class as "url" type
inputEx.registerType("uriLink", inputEx.URILinkField);

})();