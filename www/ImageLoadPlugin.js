var exec = require('cordova/exec');

exports.getImagesData = function (arg0, success, error) {
    exec(success, error, 'ImageLoadPlugin', 'getImagesData', [arg0]);
};

exports.getBucketName = function(arg0, success, error) {
    exec(success, error, 'ImageLoadPlugin', 'getBucketName', [arg0])
};

exports.getImage = function(arg0, success, error) {
    exec(success, error, 'ImageLoadPlugin', 'getImage', [arg0])
};