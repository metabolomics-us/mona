'use strict';

var app = angular
    .module('moaClientApp', [
        'ngRoute',
        'ngResource',
        'ngCookies',
        'ui.bootstrap',
        'ngTagsInput',
        'wohlgemuth.msp.parser',
        'wohlgemuth.massbank.parser',
        'wohlgemuth.cts',
        'angularMasspecPlotter',
        'angularFileUpload',
        'infinite-scroll',
        'mgcrea.bootstrap.affix'
    ]);


/**
 * location of our backend server
 */

//app.constant('REST_BACKEND_SERVER', 'http://cream.fiehnlab.ucdavis.edu:9292/trashcan.fiehnlab.ucdavis.edu:8080');
//app.constant('REST_BACKEND_SERVER', 'http://127.0.0.1:8080');
//app.constant('REST_BACKEND_SERVER', 'http://cream.fiehnlab.ucdavis.edu:8080');


/**
 * Number of spectra/compounds to view
 */
app.constant('MAX_OBJECTS', 20);

/**
 * Toggle for whether commonly used data (tags, metadata, etc) should be
 * internally cached
 */
app.constant('INTERNAL_CACHING', true);


/**
 * enable cross domain stuff
 */
app.config(function ($httpProvider) {
    $httpProvider.defaults.useXDomain = true;
    delete $httpProvider.defaults.headers.common['X-Requested-With'];
});

/**
 * Prompt user before leaving the page if spectra are being uploaded.
 * Uses $injector to bypass timeout error when testing with protractor.
 */
app.run(function($window, $injector) {
    $window.onbeforeunload = function (e) {
        var service = $injector.get('UploadLibraryService');

        if(service.isUploading()) {
            var progress = parseInt(((service.completedSpectraCount / service.uploadedSpectraCount) * 100), 10);
            return 'MoNA is '+ progress +'% done with processing and uploading spectra.';
        }
    };
});

/**
 * holder for all our controllers
 * @type {{}}
 */
var moaControllers = {};

/**
 * let's assign them
 */
app.controller(moaControllers);