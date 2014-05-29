'use strict';

var app = angular
    .module('moaClientApp', [
        'ngCookies',
        'ngResource',
        'ngSanitize',
        'ngRoute',
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
