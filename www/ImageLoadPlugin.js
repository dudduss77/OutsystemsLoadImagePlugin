var exec = require('cordova/exec');

// exports.coolMethod = function (arg0, success, error) {
//     exec(success, error, 'ImageLoadPlugin', 'coolMethod', [arg0]);
// };


function ImageLoadPlugin() {}

ImageLoadPlugin.prototype.coolMethod = function(successCallback, errorCallback) {
    var options = {};

    exec(successCallback, errorCallback, 'ImageLoadPlugin', 'coolMethod', [options]);
}

ImageLoadPlugin.install = function() {
    if(!window.plugins) {
        window.plugins = {}
    }

    window.plugins.imageloadPlugin = new ImageLoadPlugin();
    return window.plugins.imageloadPlugin;
}

cordova.addConstructor(ImageLoadPlugin.install);