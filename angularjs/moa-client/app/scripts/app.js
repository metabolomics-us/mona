'use strict';

var app = angular
    .module('moaClientApp', [
		'ngRoute',
		'ngResource',
		'ui.bootstrap'
	]);


app.config(function ($routeProvider) {
        $routeProvider
            .when('/', {
                templateUrl: 'views/main.html',
                controller: 'MainCtrl'
            }).when('/submitters', {
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

app.constant('REST_BACKEND_SERVER','http://192.168.1.75:8080');

/**
 * holder for all our controllers
 * @type {{}}
 */
var moaControllers = {};

/**
 * lets assign them
 */
app.controller(moaControllers);