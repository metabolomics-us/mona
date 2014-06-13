'use strict';

var app = angular
    .module('moaClientApp', [
        'ngRoute',
        'ngResource',
        'ui.bootstrap',
        'ngTagsInput'
    ]);


app.config(function ($routeProvider) {
    $routeProvider
        .when('/', {
            templateUrl: 'views/main.html',
            controller: 'MainCtrl'
        }).when('/compounds', {
            templateUrl: 'views/compounds/list.html',
            controller: 'CompoundsController'
        }
    ).
        when('/submitters', {
            templateUrl: 'views/submitters/list.html',
            controller: 'SubmitterController'
        }
    ).
        when('/upload', {
            templateUrl: 'views/upload/single.html',
            controller: 'SpectraController'
        }
    )
        .otherwise({
            redirectTo: '/'
        });
});

/**
 * location of our backend server
 */
//app.constant('REST_BACKEND_SERVER', 'http://localhost:8080');
app.constant('REST_BACKEND_SERVER', 'http://trashcan.fiehnlab.ucdavis.edu:9090')

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
