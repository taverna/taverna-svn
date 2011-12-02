/**
 * testLanguage. See developers.html for documentation.
 * To create a project specific language this file should be replaced.
 * Remember to add the new file to taverna.html (or its replacement)
 */
var languageExstension = { //Do not replace this name unless you replace it in tavernaLanguage.js as well.

	language: {
	
		//Using a different language name for each project keeps the save pipes apart, 
		//   even if multiple projects are run from the same server.
		languageName: "tavernaTestLanguage",


		//the modules here will be added to the ones declared in tavernaLanguage.js 
		modules: [
			{
				"name": "Echo",
				"category": "Taverna Workflow",
				"description": "Echoes input to output",
				"tavernaInfo" : {
					"wfURI":"Workflows/Echo.t2flow",
					"inputs": [{"name":"Bar", "depth":0}],
					"outputs": [{"name":"Foo", "depth":0}],
					"showWorkflow": true,
					"helpPage": "Workflows/Echo.html",
					"links" : [
						{"uri": "Workflows/Echo.html","text": "Workflow Description"},
						{"uri": "Workflows/Echo.t2flow","text": "Workflow Definition"},
					],
				},
				"container" : {
					"xtype":"WireIt.TavernaWFContainer",
				},
			},
			{
				"name": 'HelloWorld',
				"description": 'The classical no input, just output "HelloWorld" demonstration Workflow',
				"category": "Taverna Workflow",
				"tavernaInfo" : {
					"wfURI":"Workflows/HelloWorld.t2flow",
					"inputs": [],
					"outputs": [{"name":"Foo", "depth":0}],
					"showWorkflow": true,
					"helpPage": "Workflows/Echo.html",
					"links" : [
						{"uri": "Workflows/HelloWorld.html","text": "Workflow Description"},
						{"uri": "Workflows/HelloWorld.t2flow","text": "Workflow Definition"},
					]
				},
				"container": {
					"xtype":"WireIt.TavernaWFContainer",
				},
			},
			{
				"name": 'Triple Echo',
				"description": "Test workflow which simply passes the three inputs to the output with the same name. No processing is carried out",
				"category": "Taverna Workflow",
				"tavernaInfo" : {
					"wfURI":"Workflows/ThreeStrings.t2flow",
					"inputs": [
						{"name":"in_Left", "depth":0},
						{"name":"in_Middle", "depth":0},
						{"name":"in_Right", "depth":0}],
					"outputs": [
						{"name":"out_Left", "depth":0},
						{"name":"out_Middle", "depth":0},
						{"name":"out_Right", "depth":0}],
					"showWorkflow": "true",
					"helpPage": "Workflows/ThreeStrings.html",
					"links" : [
						{"uri": "Workflows/ThreeStrings.html","text": "Workflow Description"},
						{"uri": "Workflows/ThreeStrings.t2flow","text": "Workflow Definition"},
					]
				},
				"container": {
					//"icon":"taverna/taverna.jpg",
					"xtype":"WireIt.TavernaWFContainer",
				},
			},
			{
				"name": "Mixed Concatenation",
				"category": "Taverna Workflow",
				"description": "Concatenates a mixture of single Strings and Lists of Strings",
				"tavernaInfo" : {
					"wfURI":"Workflows/MixedWorkflow.t2flow",
					"inputs": [
						{"name":"LeftList", "depth":1, "description":"Cool this works"},
						{"name":"LeftNoList", "depth":0},
						{"name":"RightList", "depth":1},
						{"name":"RightNoList", "depth":0}],
					"outputs": [{"name":"Result", "depth":1}],
					"helpPage": "Workflows/MixedWorkflow.html",
					"showWorkflow": false,
					"links" : [
						{"uri": "Workflows/MixedWorkflow.html","text": "Workflow Description"},
						{"uri": "Workflows/MixedWorkflow.t2flow","text": "Workflow Definition"},
					]
				},
				"container": {
					"xtype":"WireIt.TavernaWFContainer",
				},
			},
			{
				"name": "Triple Echo Baclava Input",
				"category": "Input",
				"description": "Workflow input for the Triple Echo workflow.",
				"container": {
					"width": 350,
					"xtype": "WireIt.BaclavaContainer",
					"title": "input",
					"uri" : "Inputs/BaclavaTripleEchoInput.xml",
					"terminals": [{"name": "output"} //rest set by BaclavaContainer
					]
				}
			},			
		]
	},

};


