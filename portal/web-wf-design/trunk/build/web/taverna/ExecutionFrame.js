/**
 * This provides the methods required to run the pipes.
 * It sends the pipes to the run servlet and receives and reloades the retunred json.
 * It contains a set module instances and a set of wires linking them.
 * @class ExecutionFrame
 * @constructor
 * @param {Object} editor
 */
var ExecutionFrame = function(editor, frameLevel, parentFrame, parentIndex) {

	// save the initial config
	this.editor = editor;
	
	// save the parent frame
	this.frameLevel = frameLevel || 0;
	this.parentFrame = parentFrame;
	this.parentIndex = parentIndex;
	
	// Will contains the execution values (this.execValues[module][outputName] = the value)
	this.execValues = {};
	
};

ExecutionFrame.prototype = {

	/**
	 * This method does run a pipe (Send Json to the server)
	 * And what method to call depending on success or failure.
	 * @method run
	 * @param {Object} params The input parameters
	 */
	run: function(params) {
 
		try {
			value = this.editor.getValue()
			console.log("running frame "+ value.name, " with params ", params);
			console.log(value);
			this.tempRunWiring = {name: value.name, working: JSON.stringify(value.working), language: this.editor.options.languageName };
			var runStatus = {};
			runStatus.status = "Running."
			runStatus.details = "Please wait while the pipes runs. Any changes you make will be lost."
			this.editor.runStatusForm.setValue(runStatus, false); // the false tells inputEx to NOT fire the updatedEvt
			this.editor.adapter.runWiring(this.tempRunWiring, {
				success: this.runModuleSuccess,
				failure: this.runModuleFailure,
				scope: this
			});
			this.editor.alert("running!");
		}
		catch(ex) {
			console.log("Error while running: ", ex);
		}
	},
   
/**
 * runModule success callback
 * It loads the json back as the main wiring.
 * It fills in the run Status window (on the right hand side)
 * @method runModuleSuccess
 */
runModuleSuccess: function(encoded) {
	try {
		//console.log(encoded);
		//for (property in encoded) {
		//	console.log(property + ': ' + encoded[property]+'; ');
		//}
		decoded = window.decodeURIComponent(encoded.working);

		//console.log(decoded);
		var wiring = eval("(" +decoded +")");
		console.log(wiring);
		this.editor.loadWiring(wiring);

		this.editor.runStatusForm.setValue(wiring.properties, false); // the false tells inputEx to NOT fire the updatedEvt

		if (wiring.properties.error) {
			this.editor.alert(wiring.properties.status + "\n" + wiring.properties.error);
		} else {
			this.editor.alert(wiring.properties.status);
		}
	} catch(ex) {
		console.log("Error while running: ", ex);
	}
},
   
    /**
     * runModule failure callback
     * Oops something went wrong so popup an alert.
     *Hopefully there will be much more information in Tomcat's satnadr output log file.
     * @method runModuleFailure
     */
    runModuleFailure: function(errorStr) {
       this.editor.alert("Unable to run the wiring : "+errorStr);
    },

   
};
