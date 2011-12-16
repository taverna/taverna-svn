var guide1Language = {
 
// Set a unique name for the language
languageName: "guide1Language",

// inputEx fields for pipes properties
propertiesFields: [
	// default fields (the "name" field is required by the WiringEditor):
	{"type": "string", inputParams: {"name": "name", label: "Title", typeInvite: "Enter a title" } },
	{"type": "text", inputParams: {"name": "description", label: "Description", cols: 30} },
 
	// Additional fields
	{"type": "boolean", inputParams: {"name": "isTest", value: true, label: "Test"}},
	{"type": "select", inputParams: {"name": "category", label: "Category", selectValues: ["Demo", "Test", "Other"]} }
],

modules: [
// List of module type definitions
{
	"name": "moduleName",
	"container": {
		// which container class to use
		"xtype":"WireIt.InOutContainer",
		// Depends of the container
		"inputs": ["text1", "text2", "option1"],
		"outputs": ["result", "error"]
	}
},
{
	"name": "demoModule",
	"container": {
	"xtype":"WireIt.Container",
	 
	"icon": "../../res/icons/application_edit.png",
	"terminals": [
		{"name": "_INPUT1", "direction": [-1,0], "offsetPosition": {"left": -3, "top": 2 }},
		{"name": "_INPUT2", "direction": [-1,0], "offsetPosition": {"left": -3, "top": 37 }},
		{"name": "_OUTPUT", "direction": [1,0], "offsetPosition": {"left": 103, "top": 20 }}
	]
	}
},
{
	"name": "MyModule",
	"container": {
		"xtype": "WireIt.FormContainer",
		// inputEx options :
		"title": "WireIt.FormContainer demo",
		"collapsible": true,
		"fields": [
			{"type": "select", "inputParams": {"label": "Title", "name": "title", "selectValues": ["Mr","Mrs","Mme"] } },
			{"inputParams": {"label": "Firstname", "name": "firstname", "required": true } },
			{"inputParams": {"label": "Lastname", "name": "lastname", "value":"Dupont"} },
			{"type":"email", "inputParams": {"label": "Email", "name": "email", "required": true, "wirable": true}},
			{"type":"boolean", "inputParams": {"label": "Happy to be there ?", "name": "happy"}},
			{"type":"url", "inputParams": {"label": "Website", "name":"website", "size": 25}}
		],
		"legend": "Tell us about yourself..."
 	}
},
{
	"name": "AND gate",
	"container": {
		"xtype":"WireIt.ImageContainer",
		"image": "../logicGates/images/gate_and.png",
		"terminals": [
			{"name": "_INPUT1", "direction": [-1,0], "offsetPosition": {"left": -3, "top": 2 }},
			{"name": "_INPUT2", "direction": [-1,0], "offsetPosition": {"left": -3, "top": 37 }},
			{"name": "_OUTPUT", "direction": [1,0], "offsetPosition": 
				{"left": 80, "top": 20 }}
		]
	}
},

{
	"name": "comment",
	"container": {
		"xtype": "WireIt.FormContainer",
		"icon": "../../res/icons/comment.png",
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
{
	"name": "MyComment",
	"container": {
		"xtype": "WireIt.FormContainer",
		"icon": "../../res/icons/comment.png",
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
};
