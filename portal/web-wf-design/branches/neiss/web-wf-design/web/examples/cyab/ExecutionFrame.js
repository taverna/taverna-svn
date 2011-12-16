/**
 * An "ExecutionFrame" is the equivalent to the jsBox layer.
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
    * @method run
    * @param {Object} params The input parameters
    */
   run: function(params) {
      
      try {
          value = this.editor.getValue()
          console.log("running frame "+ value.name, " with params ", params);
          console.log(value);
          this.tempRunWiring = {name: value.name, working: JSON.stringify(value.working), language: this.editor.options.languageName };
          this.editor.adapter.runWiring(this.tempRunWiring, {
              success: this.runModuleSuccess,
              failure: this.runModuleFailure,
              scope: this
          });
      }
      catch(ex) {
         console.log("Error while running: ", ex);
      }
          
   },
   
/**
 * runModule success callback
 * @method runModuleSuccess
 */
runModuleSuccess: function(encoded) {
	try {
		console.log(encoded);
		for (property in encoded) {
			console.log(property + ': ' + encoded[property]+'; ');
		}
		decoded = window.decodeURIComponent(encoded.working);

		console.log(decoded);
		var wiring = eval("(" +decoded +")");
		console.log(wiring);
		this.editor.loadWiring(wiring);
		//for (property in decoded) {
		//  console.log(property + ': ' + decoded[property]+'; ');
		//}

	//var p =output.split('&');
	//				var oP = {};
	//				for(var i = 0 ; i < p.length ; i++) {
	//					var v = p[i].split('=');
	//					oP[v[0]]=window.decodeURIComponent(v[1]);
	//				}

		this.editor.alert("ran!");
	} catch(ex) {
		console.log("Error while running: ", ex);
	}
},
   
    /**
     * runModule failure callback
     * @method runModuleFailure
     */
    runModuleFailure: function(errorStr) {
       this.editor.alert("Unable to run the wiring : "+errorStr);
    },

   
};
