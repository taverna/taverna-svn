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
				"category": "Taverna components",
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
					]
				},
				"container" : {
					"xtype":"WireIt.TavernaWFContainer"
				}
			},
			{
				"name": 'HelloWorld',
				"description": 'The classical no input workflow, just outputs "Hello World" demonstration Workflow',
				"category": "Taverna components",
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
					"xtype":"WireIt.TavernaWFContainer"
				}
			},
			{
				"name": 'Triple Echo',
				"description": "Test workflow which simply passes the three inputs to the output with the same name. No processing is carried out",
				"category": "Taverna components",
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
					//"icon":"taverna/images/taverna.png",
					"xtype":"WireIt.TavernaWFContainer"
				}
			},
//			{
//				"name": "Mixed Concatenation",
//				"category": "Taverna components",
//				"description": "Concatenates a mixture of single strings and lists of strings",
//				"tavernaInfo" : {
//					"wfURI":"Workflows/MixedWorkflow.t2flow",
//					"inputs": [
//						{"name":"LeftList", "depth":1, "description":"Cool this works"},
//						{"name":"LeftNoList", "depth":0},
//						{"name":"RightList", "depth":1},
//						{"name":"RightNoList", "depth":0}],
//					"outputs": [{"name":"Result", "depth":1}],
//					"helpPage": "Workflows/MixedWorkflow.html",
//					"showWorkflow": false,
//					"links" : [
//						{"uri": "Workflows/MixedWorkflow.html","text": "Workflow Description"},
//						{"uri": "Workflows/MixedWorkflow.t2flow","text": "Workflow Definition"},
//					]
//				},
//				"container": {
//					"xtype":"WireIt.TavernaWFContainer"
//				}
//			},
                        {
				"name": "PRM",
				"category": "Taverna components",
				"description": "Population Reconstruction Model",
				"tavernaInfo" : {
					"wfURI":"Workflows/PRM.t2flow",
					"inputs": [{"name":"DataURL", "depth":0}, {"name":"UserID", "depth":0}],
					"outputs": [{"name":"PopulationFile_PRM", "depth":0}],
					"showWorkflow": true,
					"helpPage": "Workflows/PRM.html",
					"links" : [
						{"uri": "Workflows/PRM.html","text": "Workflow Description"},
						{"uri": "Workflows/PRM.t2flow","text": "Workflow Definition"},
					]
				},
				"container" : {
					"xtype":"WireIt.TavernaWFContainer"
				}
			},
                        {
				"name": "DSM",
				"category": "Taverna components",
				"description": "Dynamic Simulation Model",
				"tavernaInfo" : {
					"wfURI":"Workflows/DynamicSimulationModel.t2flow",
					"inputs": [{"name":"SimulatedYears", "depth":0}, {"name":"PopulationFile_PRM", "depth":0}, {"name":"UserID", "depth":0}],
					"outputs": [{"name":"PopulationFile_DSM", "depth":0}],
					"showWorkflow": true,
					"helpPage": "Workflows/DynamicSimulationModel.html",
					"links" : [
						{"uri": "Workflows/DynamicSimulationModel.html","text": "Workflow Description"},
						{"uri": "Workflows/DynamicSimulationModel.t2flow","text": "Workflow Definition"},
					]
				},
				"container" : {
					"xtype":"WireIt.TavernaWFContainer"
				}
			},
                        {
				"name": "BHPS Linker",
				"category": "Taverna components",
				"description": "British Household Panel Survey Linker",
				"tavernaInfo" : {
					"wfURI":"Workflows/BHPSLinker.t2flow",
					"inputs": [{"name":"PopulationFile_DSM", "depth":0}, {"name":"ColumnToAggregate", "depth":0}, {"name":"UserID", "depth":0}],
					"outputs": [{"name":"DataXML", "depth":0}],
					"showWorkflow": true,
					"helpPage": "Workflows/BHPSLinker.html",
					"links" : [
						{"uri": "Workflows/BHPSLinker.html","text": "Workflow Description"},
						{"uri": "Workflows/BHPSLinker.t2flow","text": "Workflow Definition"},
					]
				},
				"container" : {
					"xtype":"WireIt.TavernaWFContainer"
				}
			},
                        {
				"name": "MapTube Mapper",
				"category": "Taverna components",
				"description": "MapTube Mapper",
				"tavernaInfo" : {
					"wfURI":"Workflows/MapTube.t2flow",
					"inputs": [{"name":"ColumnToMap", "depth":0}, {"name":"DataXML", "depth":0}],
					"outputs": [{"name":"MapURL", "depth":0}],
					"showWorkflow": true,
					"helpPage": "Workflows/MapTube.html",
					"links" : [
						{"uri": "Workflows/MapTube.html","text": "Workflow Description"},
						{"uri": "Workflows/MapTube.t2flow","text": "Workflow Definition"},
					]
				},
				"container" : {
					"xtype":"WireIt.TavernaWFContainer"
				}
			},                        
                        {
				"name": "Triple Echo Baclava Input",
				"category": "Inputs",
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
	}

};


