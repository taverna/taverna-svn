/**
 * Container for Taverna Modules.
 * Handles the adding of most of the terminals based on limited information. (see Developer.html)
 * Based on WireIt's InOutContainer
 * @class TavernaWFContainer
 * @extends WireIt.Container
 * @constructor
 * @param {Object} options
 * @param {WireIt.Layer} layer
 */
WireIt.TavernaWFContainer = function(options, layer) {
   WireIt.TavernaWFContainer.superclass.constructor.call(this, options, layer);
};

YAHOO.lang.extend(WireIt.TavernaWFContainer, WireIt.Container, {

	/**
	 * @method setOptions
	 * @param {Object} options the options object
	 */
	setOptions: function(options) {
		WireIt.TavernaWFContainer.superclass.setOptions.call(this, options);

		this.options.xtype = "WireIt.TavernaWFContainer";
		
		//The load module method writes the name to title so if no name use title
		options.name = options.name || options.title;
		var module = tavernaLanguage.moduleByName(options.name);
		var tavernaInfo = module.tavernaInfo;
		
		this.options.name = options.name;
		this.options.className = options.className || "WireIt-Container WireIt-TavernaWFContainer";

		// Overwrite default value for options:
		this.options.resizable = (typeof options.resizable == "undefined") ? false : options.resizable;

		this.inputs = tavernaInfo.inputs || [];
		this.outputs = tavernaInfo.outputs || [];
		
		//Always take the wfURI from the language.
			//A wfURI is saved as the save and run code are shared.
			//Taking the wfURI from the language allows the wfURI to be updated without breaking existing saved wirings.
		this.options.wfURI = tavernaInfo.wfURI;
				
		this.links = tavernaInfo.links || [];
	
		this.options.name = options.name;
		
		//Add links to workflow and help page is applicable
		this.options.title = tavernaLanguage.titleByName(options.name);
		//Add a taverna icon if above did not already do so
		this.options.icon = tavernaLanguage.iconByName(options.name);
	},

   /**
    * Adds the links and terminals and formats them.
    * @method render
    */	
    render: function() {
		
		WireIt.TavernaWFContainer.superclass.render.call(this);

		//Add links if any
		var offset = 33; //I assume this is the header
		for(var i = 0 ; i < this.links.length ; i++) {
			var link = this.links[i];
			var text = link.text || link.uri;
			var docLink = '<a href="' + link.uri + '" target="_blank">' + text + '</a>';
			this.bodyEl.appendChild(WireIt.cn('div', null, {lineHeight: "30px", textAlign: "center"}, docLink));
			offset = offset + 30;
		}
		
		var baclavaName;
		//Baclava Input if needed
		//It is VITAL that the baclava Input terminal is added first and therefor terminal[0]
			//The code in IndividualTerminal (below) depends on this!
			//If there is not input this requirement does not apply.
		if (this.inputs.length > 0) {
			//This adds the terminal dot.
			this.options.terminals.push({
				"xtype": "WireIt.BaclavaTerminal",
				"name": "Baclava Input", 
				"offsetPosition": {"left": -14, "top": offset }, 
				"nMaxWires": 1,
				"ddConfig": {
					"type": "inputBaclava",
					"allowedTypes": ["outputURL", "outputBaclava"],
					}
			});	
			baclavaName = "Baclava format Input/Output"
		} else {
			baclavaName = "Baclava format Output"
		}
		
		//Baclava Output
		//This adds the terminal dot.
		this.options.terminals.push({
			"name": "Baclava Output", 
			"offsetPosition": {"right": -14, "top": offset }, 
			"ddConfig": {
				"type": "outputBaclava",
				"allowedTypes": ["inputBaclava", "inputURL"],
			},
			"alwaysSrc": true,
			"wireConfig": { drawingMethod: "arrows", color: "#FF0000", bordercolor:"#FF00FF"},
		});
		//This adds the text name to the form
		this.bodyEl.appendChild(WireIt.cn('div', null, {lineHeight: "30px", textAlign: "center"}, baclavaName));
		
		//Normal input includingall styling
		for(var i = 0 ; i < this.inputs.length ; i++) {
			var input = this.inputs[i];
			var showName = {};			
			var newTerminal = {};
			newTerminal.ddConfig = {};
			
			newTerminal.xtype = "WireIt.IndividualTerminal"
			newTerminal.name = input.name;
			newTerminal.offsetPosition = {"left": -14, "top": offset + 30*(i+1) }; 
			newTerminal.nMaxWires = 1;
			
			if (input.depth == 1) {
				newTerminal.ddConfig.type = "inputDepthOne";
				newTerminal.ddConfig.allowedTypes = ["outputString","outputURL","outputList","outputDelimitedURL"];			
				showName = input.name + " (list)";
			} else {
				newTerminal.ddConfig.type = "inputDepthZero";
				newTerminal.ddConfig.allowedTypes = ["outputString","outputURL"];
				showName = input.name;
			}		
			//This adds the terminal dot.
			this.options.terminals.push(newTerminal);
			//This adds the text name to the form
			this.bodyEl.appendChild(WireIt.cn('div', null, {lineHeight: "30px"}, showName));
		}
		
		//Normal Output including all styling
		for(i = 0 ; i < this.outputs.length ; i++) {
			var output = this.outputs[i];
			var showName = {};
			var newTerminal = {};
			newTerminal.ddConfig = {};
			newTerminal.wireConfig = {};
			
			newTerminal.name = output.name;
			newTerminal.offsetPosition = {"right": -14, "top": offset + 30*(i+1+this.inputs.length) };
			newTerminal.alwaysSrc = true;
			newTerminal.wireConfig.drawingMethod = "arrows"
			
			if (output.depth == 1) {
				newTerminal.ddConfig.type = "outputList";
				newTerminal.ddConfig.allowedTypes = ["inputList","inputDepthOne"];
				showName = output.name + " (list)";
				newTerminal.wireConfig.width = 5;
				newTerminal.wireConfig.borderwidth = 3;
			} else {
				newTerminal.ddConfig.type = "outputString";
				newTerminal.ddConfig.allowedTypes = ["inputString","inputDepthOne","inputDepthZero"];	
				showName = output.name;
			}		
			//This adds the terminal dot.
			this.options.terminals.push(newTerminal);
			//This adds the text name to the form
			this.bodyEl.appendChild(WireIt.cn('div', null, {lineHeight: "30px", textAlign: "right"}, showName));
		}
		
	},

	/**
	 * Return the config of this container.
	 * Exstended from Container.getConfig()
	 * @method getConfig
	 */
	getConfig: function() {
		var obj = {};

		// Position
		obj.position = YAHOO.util.Dom.getXY(this.el);
		if(this.layer) {
			// remove the layer position to the container position
			var layerPos = YAHOO.util.Dom.getXY(this.layer.el);
			obj.position[0] -= layerPos[0];
			obj.position[1] -= layerPos[1];
			// add the scroll position of the layer to the container position
			obj.position[0] += this.layer.el.scrollLeft;
			obj.position[1] += this.layer.el.scrollTop;
		}

		// xtype
		if(this.options.xtype) {
			obj.xtype = this.options.xtype;
		}
		
		//TavernaWF Extra   Add the workflowURI
		obj.wfURI = this.options.wfURI;
		
		return obj;
	},
});

/**
 * BaclavaTerminal  
 * Adds the extra that if a wire is added it removes the idiviual input wires.
 * @extends WireIt.Termina
 */
(function() {

WireIt.BaclavaTerminal = function(parentEl, options, container) {
	WireIt.BaclavaTerminal.superclass.constructor.call(this, parentEl, options, container);
	individualTerminals = [];
};

YAHOO.lang.extend(WireIt.BaclavaTerminal, WireIt.Terminal, {

	setOptions: function(options) {
		WireIt.BaclavaTerminal.superclass.setOptions.call(this, options);
	},

	/**
	 * Extra feature is that the wires from the idividual input terminals are removed.
	 */
	addWire: function(wire) {
		//console.log("addWire:");
		WireIt.BaclavaTerminal.superclass.addWire.call(this, wire);
		for(var i = 0 ; i < individualTerminals.length ; i++) {
			individualTerminals[i].removeAllWires();
		}
	},
		
	/**
	 * Register an individual terminal, so the wires can be removed if required.
	 */
	addIndividualTerminal: function(terminal){
		individualTerminals.push(terminal);
	},
	
});

})();

/**
  * IndividualTerminal 
  * This extension register itself with the BaclavaTerminal so that wires can be removed.
  * WARNING: Replies on the Baclava Input terminal to be the first one added.
  * @extends WireIt.Termina
  **/

(function() {

/**
 * Extension is to register this with baclava Input
 */
WireIt.IndividualTerminal = function(parentEl, options, container) {
	WireIt.IndividualTerminal.superclass.constructor.call(this, parentEl, options, container);
	//The following line only works if the Baclava input is added FIRST!
	container.terminals[0].addIndividualTerminal(this);
};

YAHOO.lang.extend(WireIt.IndividualTerminal, WireIt.Terminal, {

	setOptions: function(options) {
		WireIt.IndividualTerminal.superclass.setOptions.call(this, options);
	},
	
	/**
	 * Extension is to remove the BaclavaInputs wire if adding one here.
	 */
	addWire: function(wire) {
		WireIt.BaclavaTerminal.superclass.addWire.call(this, wire);
		container.terminals[0].removeAllWires();
	},

});

})();
