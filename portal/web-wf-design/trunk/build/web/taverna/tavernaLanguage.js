/**
 * tavernaLanguage
 * This is the base language for all Taverna projects
 * Project specific ones like worklfows and modules should go in a replacement of testLanguage.js (see develop.html)
 */
var tavernaLanguage = {

	language: {
		
		languageName: "tavernaBaseLanguage",

		modules: [
			//Input modules 
			{
				"name": "Simple Input",
				"description": "Place to enter a single value",
				"category": "Input",
				"container": {
					"xtype": "WireIt.FormContainer",
					"title": "input",
					"fields": [
						{
							"inputParams": {
								"label": "Value", 
								"name": "output",
								"required": true
							}
						},
					],
					"terminals": [
						{
							"name": "output",
							"description":"Cool this works",
							"offsetPosition": {"right": -14, "top": 25},
							"alwaysSrc":true,
							 wireConfig: { drawingMethod: "arrows"},
							"ddConfig": {
								"type": "outputString",
								"allowedTypes": ["inputString","inputDepthZero","inputDepthOne"],
							}
						}
					]
				}
			},
			{
				"name": "URL Input",
				"description": "Place to enter a single url.",
				"category": "Input",
				"container": {
					"xtype": "WireIt.FormContainer",
					"width": 350,
					"title": "input",
					"fields": [
						{
							"type": 'url',
							"inputParams": {
								"label": "URL", 
								"name": "output",
								"required": true
							}
						},
					],
					"terminals": [
						{
							"name": "output",
							"offsetPosition": {"right": -14, "top": 25},
							"alwaysSrc":true,
							 wireConfig: { drawingMethod: "arrows", color: "#EE11EE", bordercolor:"#FF00FF"},
							"ddConfig": {
								"type": "outputURL",
								"allowedTypes": ["inputURL","inputDepthZero","inputDepthOne"],
							}
						}
					]
				}
			},
			{
				"name": "URL To List Input",
				"description": "Place to enter the url to a list and the delimiter between list elements.",
				"category": "Input",
				"container": {
					"xtype": "WireIt.FormContainer",
					"width": 350,
					"title": "input",
					"fields": [
						{
							"type": 'url',
							"inputParams": {
								"label": "URL", 
								"name": "url",
								"required": true
							}
						},
						{
							"inputParams": {
								"label": "Delimiter", 
								"name": "delimiter",
								"required": true,
								"maxLength": 2
							}
						},
					],
					"terminals": [
						{
							"name": "output",
							"offsetPosition": {"right": -14, "top": 25},
							"alwaysSrc":true,
							 wireConfig: {width: 5, borderwidth:3, drawingMethod: "arrows", color: "#EE11EE", bordercolor:"#FF00FF"},
							"ddConfig": {
								"type": "outputDelimitedURL",
								"allowedTypes": ["inputURL","inputDepthOne"],
							}
						}
					]
				}
			},
			{
				"name": "List Input",
				"description": "Place to enter a list of item. Each line is considered one item. NewLine within an item not supported.",
				"category": "Input",
				"container": {
					"xtype": "WireIt.FormContainer",
					"title": "Input test",
					"fields": [{"type": "text", "inputParams": {"label": "List values", "name": "output", "wirable": false }} ],
					"terminals": [
						{
							"name": "output",
							"offsetPosition": {"right": -14, "top": 75},
							"alwaysSrc":true,
							"ddConfig":{
								"type": "outputList",
								"allowedTypes": ["inputList", "inputDepthOne"]
							},
							wireConfig:{width: 5, borderwidth:3, drawingMethod: "arrows"}
						}
					],
				}
			},
			//Output modules
			{
				"name": "Simple Output",
				"category": "Output",
				"description": "Single port Workflow output in String format. ",
				"container": {
					"xtype": "WireIt.FormContainer",
					"title": "output",
					"fields": [ 
						{"type": "uneditable", "inputParams": {"label": "Value", "name": "input", "wirable": false}}
					],
					"terminals": [
						{
							"name": "input",
							"offsetPosition": {"left": -14, "top": 25 },
							"ddConfig": {
								"type": "inputString",
								"allowedTypes": ["outputString"]
							},
							"nMaxWires": 1
						}
					]
				}
			},
			{
				"name": "Baclava Output",
				"category": "Output",
				"description": "Baclava Workflow output as a clickable link.",
				"container": {
					"width": 350,
					"xtype": "WireIt.BaclavaContainer",
					"title": "output",
					"terminals": [
						{
							"name": "input", //rest set by BaclavaContainer
						}
					]
				}
			},			
			{
				"name": "URL Link Output",
				"category": "Output",
				"description": "Output as a clickable link.",
				"container": {
					"width": 350,
					"xtype": "WireIt.URILinkContainer",
					"title": "output",
					"terminals": [
						{"name": "input"} //rest set by URILinkContainer
					]
				}
			},			
			{
				"name": "List Output",
				"category": "Output",
				"description": "Single port workflow output. As a flattened list of Strings",
				"container": {
					"xtype": "WireIt.FormContainer",
					"title": "Output test",
					"fields": [
						{"type": "text", "inputParams": {"label": "List values", "name": "input", "wirable": false}}
					],
					"terminals": [
						{
							"name": "input",
							"offsetPosition": {"left": -14, "top": 75},
							"alwaysSrc":false,
							"ddConfig":{
								"type": "inputList",
								"allowedTypes": ["outputDepthOne", "outputList"]
							}
						}
					]
				}
			},
			
			//Pass through modules that act as both input and putput
			{
				"name": "PassThrough",
				"category": "Pass Through",
				"description": "Field that can be placed between the output of one workflow and the Input of another one. Shows the value being passed as a String",
				"container": {
					"xtype": "WireIt.FormContainer",
					// inputEx options :
					"collapsible": true,
					"legend": "here comes the passthrough...",
					"fields": [
						{"type": "uneditable", "inputParams": {"label": "PassThrough", "name": "both", "required": false } },
					],
					"terminals": [
						{
							"name": "input",
							"offsetPosition": {"left": -14, "top": 33 },
							"ddConfig": {
								"type": "inputString",
								"allowedTypes": ["outputString"]
							},
							"nMaxWires": 1
						},
						{
							"name": "output",
							"offsetPosition": {"right": -14, "top": 33},
							"alwaysSrc":true,
							 wireConfig: { drawingMethod: "arrows"},
							"ddConfig": {
								"type": "outputString",
								"allowedTypes": ["inputString", "inputDepthZero"],
							}
						}
					]
				}
			},
			{
				"name": "URL Pass Through",
				"description": "Field that can be placed between the Baclava output of one workflow and the Baclava Input of another one. Provides a clickable URI to the file being passed",
				"category": "Pass Through",
				"container": {
					"width": 350,
					"xtype": "WireIt.URILinkContainer",
					"terminals": [
						{ "name": "input"}, //rest set by URILinkContainer
						{ "name": "output"} //rest set by URILinkContainer
					]
				}
			},			
			{
				"name": "Baclava Pass Through",
				"description": "Field that can be placed between the Baclava output of one workflow and the Baclava Input of another one. Provides a clickable URI to the file being passed",
				"category": "Pass Through",
				"container": {
					"width": 350,
					"xtype": "WireIt.BaclavaContainer",
					"terminals": [
						{ "name": "input" }, //rest set by BaclavaContainer
						{ "name": "output" }, //rest set by BaclavaContainer
					]
				}
			},			
			{
				"name": "comment",
				"container": {
					"xtype": "WireIt.FormContainer",
					"title": "My Comment",
					"fields": [
						{"type": "text", "inputParams": {"label": "", "name": "comment", "wirable": false }}
					]
				},
				"value": {
					"input": {
						"type":"url","inputParams":{}
					}
				}
			},
		]
	},

	/**
	 * Support function for finding a the configuaration for a module based on its name
	 * @method moduleByName
	 * @static
	 */
	moduleByName: function(name) {
		var language = this.language;
		var modules = language.modules
		for(var i = 0 ; i < modules.length ; i++) {
			if (modules[i].name == name) {
				return modules[i];
			}
		}
		return null;
	},
		
	/**
	 * Creates a title for a taverna module given the name of the module.
	 * Adds a taverna logo with a link (if showWorkflow)
	 * Adds a link to a help page if one was set.
	 * @method titleByName
	 * @static
	 */
	titleByName: function(name) {
		module = this.moduleByName(name);
		var tavernaLink = ""
		if (module.tavernaInfo.showWorkflow){	
			var tavernaTitle = module.tavernaInfo.wfToolTip || "Click here to see workflow script";
			var tavernaLink	= '<a href="' + module.container.wfURI +'" target="_blank"><IMG SRC="taverna/taverna.jpg" title="' + tavernaTitle + '"></a> '
		}
		var helpLink = "";
		if (module.tavernaInfo.helpPage) {
			var helpTitle = module.tavernaInfo.helpToolTip || "Click here to more information";
			helpLink = ' <a href="' + module.tavernaInfo.helpPage +'" target="_blank"><IMG SRC="images/icons/help.png" title="' + helpTitle + '"></a>'
		}
		return (tavernaLink + module.name + helpLink);
	},
	
	/**
	 * Provides a Taverna icon for those taverna modules that do not allow showWorkflow
	 * This ensures there will always be exactly one Taverna icons, either as a link to the workflow or just plain.
	 * @method iconByName
	 * @static
	 */
	iconByName: function(name) {
		module = this.moduleByName(name);
		if (module.tavernaInfo.showWorkflow){	
			return null
		} else {
			return module.container.icon || "taverna/taverna.jpg";
		}
	},

	/**
	 * Extended init function that also does the taverna specific stuff.
	 * @method init
	 * @static
	 */
	init: function() {

		try {
			//Choose the type of adapter that will connect to the server.
			this.language.adapter = WireIt.WiringEditor.adapters.AjaxAdapter;
		
			//bring in the specific exstensions for the project
			//Pipes are saved and loaded by languages so taking the name from the exstension keeps them apart.
			this.language.languageName = languageExstension.language.languageName
			//Add in the speific Workflows and inputs from the exstension
			for(var i = 0 ; i < languageExstension.language.modules.length ; i++) {
				this.language.modules.push(languageExstension.language.modules[i]);
			}
			
			//The default is that the module name becomes the title of the windows.
				//These methods allows this to be changed to a more informative title and add an icon.
			for(var i = 0 ; i < this.language.modules.length ; i++) {
				module = this.language.modules[i];
				if (module.container.xtype == "WireIt.TavernaWFContainer"){
					module.title = this.titleByName(module.name);
					module.container.icon = this.iconByName(module.name);
				}
			}

			this.editor = new tavernaLanguage.WiringEditor(this.language);

			//Open the minimap
			this.editor.accordionView.openPanel(2);

			// Open the infos panel
			//this.editor.accordionView.openPanel(3);
			

			//Add an extra window to the right hand side to give information about the run
			var runStatusFields = [
				{"type": "uneditable", inputParams: {"name": "status", label: "Status", value: "Not yet run", rows: 4} },
				{"type": "text", inputParams: {"name": "details", label: "Description", cols: 50, rows: 4} }
			];
			this.editor.runStatusForm = new inputEx.Group({
				parentEl: YAHOO.util.Dom.get('runStatus'),
				fields: runStatusFields
			});
			
			var testProp = {};
			//testProp.status = "Not yet run."
			testProp.details = "Please setup the pipe and press run."
			this.editor.runStatusForm.setValue(testProp , false); // the false tells inputEx to NOT fire the updatedEvt
			//Open the minimap
			this.editor.accordionView.openPanel(1);


		} catch(ex) {
			console.log("Error while initialising: ", ex);
		}
	},

	/**
	 * Execute the module in the "ExecutionFrame" virtual machine
	 * @method run
	 * @static
	 */
	run: function() {
		var ef = new ExecutionFrame( this.editor);
		ef.run();
	}

};


