'use strict';

var app = angular
    .module('moaClientApp', [
        'ngRoute',
        'ngResource',
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
app.constant('REST_BACKEND_SERVER', 'http://localhost:8080');
//app.constant('REST_BACKEND_SERVER', 'http://cream.fiehnlab.ucdavis.edu:8080');


/**
 * Number of spectra/compounds to view
 */
app.constant('MAX_OBJECTS', 20);

app.run(function ($rootScope, SpectraQueryBuilderService) {

    /**
     * set's a new spectra query
     * @param query
     */
    $rootScope.setSpectraQuery = function (query) {
        $rootScope.spectraQuery = query;
    };

    /**
     * returns our query
     * @returns {*|$rootScope.spectraQuery}
     */
    $rootScope.getSpectraQuery = function () {
        return $rootScope.spectraQuery;
    };

    /**
     * resets our query
     */
    $rootScope.resetSpectraQuery = function () {
        $rootScope.spectraQuery = SpectraQueryBuilderService.prepareQuery();
    };

    //contains our build and modified spectra query
    $rootScope.spectraQuery = SpectraQueryBuilderService.prepareQuery();

});

app.constant('INTERNAL_CACHING', true);


/**
 * enable cross domain stuff
 */
app.config(function ($httpProvider) {
    $httpProvider.defaults.useXDomain = true;
    delete $httpProvider.defaults.headers.common['X-Requested-With'];
});

/**
 * holder for all our controllers
 * @type {{}}
 */
var moaControllers = {};

/**
 * lets assign them
 */
app.controller(moaControllers);
