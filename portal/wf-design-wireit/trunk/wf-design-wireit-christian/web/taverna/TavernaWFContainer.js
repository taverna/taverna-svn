/**
 * Container with left inputs and right outputs
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

		this.options.className = options.className || "WireIt-Container WireIt-TavernaWFContainer";

		// Overwrite default value for options:
		this.options.resizable = (typeof options.resizable == "undefined") ? false : options.resizable;

		this.options.inputs = options.inputs || [];
		this.options.outputs = options.outputs || [];
		
		this.options.wfURI = options.wfURI;
		
		console.log(options);
		
		this.options.links = options.links || [];

	},

	render: function() {
		WireIt.TavernaWFContainer.superclass.render.call(this);

		console.log(this.options)
		//Add links if any
		var offset = 33; //I assume this is the header
		for(var i = 0 ; i < this.options.links.length ; i++) {
			var link = this.options.links[i];
			var text = link.text || link.uri;
			var docLink = '<a href="' + link.uri + '" target="_blank">' + text + '</a>';
			this.bodyEl.appendChild(WireIt.cn('div', null, {lineHeight: "30px", textAlign: "center"}, docLink));
			offset = offset + 30;
		}
		
		var baclavaName;
		//Baclava Input if needed
		if (this.options.inputs.length > 0) {
			//This adds the terminal dot.
			this.options.terminals.push({
				"name": "Baclava Input", 
				"offsetPosition": {"left": -14, "top": offset }, 
				"nMaxWires": 1,
				"ddConfig": {
					"type": "inputURL",
					"allowedTypes": ["outputURL"],
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
				"type": "outputURL",
				"allowedTypes": ["inputURL"],
			},
			"alwaysSrc": true,
			"wireConfig": { drawingMethod: "arrows", color: "#EE11EE", bordercolor:"#FF00FF"} 
		});
		//This adds the text name to the form
		this.bodyEl.appendChild(WireIt.cn('div', null, {lineHeight: "30px", textAlign: "center"}, baclavaName));
		
		//Normal input
		for(var i = 0 ; i < this.options.inputs.length ; i++) {
			var input = this.options.inputs[i];
			var showName = {};			
			var newTerminal = {};
			newTerminal.ddConfig = {};
			
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
		
		//Normal Output
		for(i = 0 ; i < this.options.outputs.length ; i++) {
			var output = this.options.outputs[i];
			var showName = {};
			var newTerminal = {};
			newTerminal.ddConfig = {};
			newTerminal.wireConfig = {};
			
			newTerminal.name = output.name;
			newTerminal.offsetPosition = {"right": -14, "top": offset + 30*(i+1+this.options.inputs.length) };
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