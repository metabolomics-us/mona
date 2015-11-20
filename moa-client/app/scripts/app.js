'use strict';

var app = angular
    .module('moaClientApp', [
        'ngRoute',
        'ngResource',
        'ngCookies',
        'ngAnimate',
        'ngSanitize',
        'ngAnimate',
        'ui.bootstrap',
        'dialogs.main',
        'dialogs.default-translations',
        'ngTagsInput',
        'wohlgemuth.msp.parser',
        'wohlgemuth.mgf.parser',
        'wohlgemuth.massbank.parser',
        'wohlgemuth.cts',
        'angularFileUpload',
        'angularMasspecPlotter',
        'infinite-scroll',
        'mgcrea.bootstrap.affix',
        'pascalprecht.translate'
    ]);

/**
 * Number of spectra/compounds to view
 *
 * 7 fills out a screen nicely and has a decent response time at the same time
 */
app.constant('MAX_SPECTRA', 7);
app.constant('MAX_COMPOUNDS', 20);
app.constant('MAX_OBJECTS', 20);


/**
 * Toggle for whether commonly used data (tags, metadata, etc) should be
 * internally cached
 */
app.constant('INTERNAL_CACHING', true);

/**
 * App name
 */
app.run(function($rootScope) {
    $rootScope.APP_NAME = 'MassBank of North America';
    $rootScope.APP_NAME_ABBR = 'MoNA';
    $rootScope.APP_VERSION = 'alpha-2';
});


/**
 * HTTP configuration
 */
app.config(function ($httpProvider) {
    // Enable cross domain access
    $httpProvider.defaults.useXDomain = true;
    delete $httpProvider.defaults.headers.common['X-Requested-With'];

    // Interceptor to handle 500 errors
    //$httpProvider.interceptors.push('httpResponseInterceptor');
});

/**
 * Set translator language for dialog service
 */
app.config(function ($translateProvider) {
    $translateProvider.preferredLanguage('en-US');
    $translateProvider.useSanitizeValueStrategy('sanitize');
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
