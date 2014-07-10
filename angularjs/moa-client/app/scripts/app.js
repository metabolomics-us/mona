'use strict';

var app = angular
    .module('moaClientApp', [
        'ngRoute',
        'ngResource',
        'ui.bootstrap',
        'ngTagsInput',
        'wohlgemuth.msp.parser',
        'wohlgemuth.cts',
        'angularFileUpload',
        'infinite-scroll'
    ]);


/**
 * location of our backend server
 */

app.constant('REST_BACKEND_SERVER', 'http://trashcan.fiehnlab.ucdavis.edu:8080');
//app.constant('REST_BACKEND_SERVER', 'http://localhost:8080');
//app.constant('REST_BACKEND_SERVER', 'http://cream.fiehnlab.ucdavis.edu:8080');

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
