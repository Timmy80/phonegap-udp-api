var exec = require('cordova/exec');
var platform = require('cordova/platform');

// Start of cut and paste area (to put back in Git repo version of this file)
module.exports = {
    
    // Order of parameters on all calls:
    // - callback function
    // - error function
    // - native class name
    // - native method name
    // - arguments for method
    
initialize: function(port) {
    cordova.exec(
                 // To access the success and error callbacks for initialization, these two functions should be in your project:
                 // UDPTransmitterInitializationSuccess(success)
                 // UDPTransmitterInitializationError(error)
                 function(success){UDPTransmitterInitializationSuccess(success);},
                 function(error){UDPTransmitterInitializationError(error);},
                 "UDPTransmit",
                 "initialize",
                 [port]);
    return true;
},
    
sendMessage: function(host, port, message) {
    cordova.exec(
                 // To access the success and error callbacks for packet transmission, these two functions should be in your project:
                 // UDPTransmissionSuccess(success)
                 // UDPTransmissionError(error)
                 function(success){UDPTransmissionSuccess(success);},
                 function(error){UDPTransmissionError(error);},
                 "UDPTransmit",
                 "sendMessage",
                 [host, port, message]);
    return true;
},
    
onReceive: function(callback, errorCallback) {
    cordova.exec(
                 callback,		// reception callback
                 errorCallback,	// error callback
                 "UDPTransmit",	// module name
                 "onReceive",	// method to call
                 []);			// no argument
    return true;
}
    
};
// End of cut and paste area
