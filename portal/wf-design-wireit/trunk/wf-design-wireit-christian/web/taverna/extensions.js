/**
 * The wiring editor is overriden to add a button "RUN" to the control bar
 */
tavernaLanguage.WiringEditor = function(options) {
	tavernaLanguage.WiringEditor.superclass.constructor.call(this, options);
};

inputEx.spacerUrl = "../lib/inputex/images/space.gif";

YAHOO.lang.extend(tavernaLanguage.WiringEditor, WireIt.WiringEditor, {
	/**
	 * Add the "run" button
	 */
	renderButtons: function() {
		tavernaLanguage.WiringEditor.superclass.renderButtons.call(this);

		// Add the run button to the toolbar
		var toolbar = YAHOO.util.Dom.get('toolbar');
		var runButton = new YAHOO.widget.Button({ label:"Run", id:"WiringEditor-runButton", container: toolbar });
		runButton.on("click", tavernaLanguage.run, tavernaLanguage, true);
	},

	/**
	 * Copied from WiringEditor loadPipe
	 * @method loadWiring
	 * @param {Object} Wiring (pipe)
	 */
	loadWiring: function(wiring) {
		// TODO: check if current wiring is saved...
		this.layer.clear();

		this.propertiesForm.setValue(wiring.properties, false); // the false tells inputEx to NOT fire the updatedEvt

		//console.log(wiring)
		//console.log(wiring.modules)
		
		if(YAHOO.lang.isArray(wiring.modules)) {
			// Containers
			for(i = 0 ; i < wiring.modules.length ; i++) {
				var m = wiring.modules[i];
				if(this.modulesByName[m.name]) {
					var baseContainerConfig = this.modulesByName[m.name].container;
					YAHOO.lang.augmentObject(m.config, baseContainerConfig); 
					m.config.title = m.name;
					var container = this.layer.addContainer(m.config);
					YAHOO.util.Dom.addClass(container.el, "WiringEditor-module-"+m.name);
					container.setValue(m.value);
				}
				else {
					throw new Error("WiringEditor: module '"+m.name+"' not found !");
				}
			}

			// Wires
			if(YAHOO.lang.isArray(wiring.wires)) {
				for(i = 0 ; i < wiring.wires.length ; i++) {
					// On doit chercher dans la liste des terminaux de chacun des modules l'index des terminaux...
					this.layer.addWire(wiring.wires[i]);
				}
			}
		}

		this.preventLayerChangedEvent = false;
	},
 
});
