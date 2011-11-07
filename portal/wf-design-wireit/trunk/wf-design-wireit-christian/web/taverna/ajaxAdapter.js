/**
 * Ajax Adapter. Expect JSON response for all queries.
 * @class WireIt.WiringEditor.adapters.AjaxAdapter
 * @static 
 */
WireIt.WiringEditor.adapters.AjaxAdapter = {
	
	/**
	 * init the adapter 
	 * @method init
	 * @static
	 */
	init: function() {
		YAHOO.util.Connect.setDefaultPostHeader('application/json');
	},
	
	/**
	 * called when saved
	 * @method saveWiring
	 * @static
	 */
	saveWiring: function(value, callbacks) {
		this._sendRequest('../SaveWireit', 'POST', value, callbacks);
	},
	
	/**
	 * called when running
	 * @method runWiring
	 * @static
	 */
	runWiring: function(value, callbacks) {
		this._sendRequest('../RunWireit', 'POST', value, callbacks);
	},
	
	/**
	 * called when deleted
	 * @method deleteWiring
	 * @static
	 */
	deleteWiring: function(value, callbacks) {
		this.url = "../DeleteWireit?name=" + value.name + "&language=" + value.language;
		this._sendRequest(this.url, 'GET', value, callbacks);
	},
	
	/**
	 * called to load the wirings
	 * @method listWirings
	 * @static
	 */
	listWirings: function(value, callbacks) {
		this.url = "../ListWireit?language=" + value.language;
		this._sendRequest(this.url, 'GET', value, callbacks);
	},
	
	/**
	 * send a request in JSON
	 * @method _sendRequest
	 * @static
	 */
	_sendRequest: function(url, method, value, callbacks) {
	
		var params = [];
		for(var key in value) {
			if(value.hasOwnProperty(key)) {
				params.push(window.encodeURIComponent(key)+"="+window.encodeURIComponent(value[key]));
			}
		}
		var postData = params.join('&');
		
		YAHOO.util.Connect.asyncRequest(method, url, {
			success: function(o) {
				var s = o.responseText,
					 r = YAHOO.lang.JSON.parse(s);
			 	callbacks.success.call(callbacks.scope, r);
			},
			failure: function(o) {
				var error = o.status + " " + o.statusText;
				callbacks.failure.call(callbacks.scope, error);
			}
		},postData);
	}
	
};
