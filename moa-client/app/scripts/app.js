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
        'angularFileUpload',
        'angularMasspecPlotter',
        'infinite-scroll',
        'mgcrea.bootstrap.affix'
    ]);

/**
 * Number of spectra/compounds to view
 *
 * 7 fills out a screen nicely and has a decent response time at the same time
 */
app.constant('MAX_OBJECTS', 7);

/**
 * Toggle for whether commonly used data (tags, metadata, etc) should be
 * internally cached
 */
app.constant('INTERNAL_CACHING', true);

/**
 * App name
 */
app.run(function($rootScope) {
    $rootScope.APP_NAME = 'Massbank of North America';
    $rootScope.APP_NAME_ABBR = 'MoNA';
});


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
