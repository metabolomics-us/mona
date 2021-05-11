/***************************************
 * Our main module, and configs. Read the README.md for best practices
 * before making any changes to moaClientApp
 * *************************************/
import * as angular from 'angular';
import "bootstrap/dist/css/bootstrap.css";
import "angular-dialog-service/dist/dialogs.min.css"
import "components-font-awesome/css/font-awesome.css";
import "ng-tags-input/build/ng-tags-input.bootstrap.css";
import "ng-tags-input/build/ng-tags-input.bootstrap.min.css";
import "nvd3/build/nv.d3.css";

import "../lib/chemdoodle-test/ChemDoodleWeb.css"
import "../styles/main.scss";
import "../styles/autocomplete.scss";
import "angular-ui-bootstrap/dist/ui-bootstrap-csp.css";

import "angular-ui-bootstrap"
import "bootstrap-multiselect/dist/js/bootstrap-multiselect";
import 'angular-route';
import 'angular-resource';
import 'angular-cookies';
import 'angular-animate';
import 'angular-filter';
import 'angular-dialog-service/dist/dialogs';
import 'ng-tags-input';
import 'angular-msp-parser';
import 'angular-mgf-parser';
import 'angular-massbank-parser';
import 'angular-cts-service';
import 'angular-file-upload';
import 'angular-masspec-plotter';
import 'angularjs-toaster';
import 'nvd3';
import 'angular-google-analytics';

angular
    .module('moaClientApp', [
        'ngRoute',
        'ui.bootstrap',
        'ngResource',
        'ngCookies',
        'ngAnimate',
        'angular.filter',
        'dialogs.main',
        'ngTagsInput',
        'wohlgemuth.msp.parser',
        'wohlgemuth.mgf.parser',
        'wohlgemuth.massbank.parser',
        'wohlgemuth.cts',
        'angularFileUpload',
        'angularMasspecPlotter',
        'toaster',
        'nvd3',
        'angular-google-analytics'
    ])

        /**
         * Global $http error handling
         * usage: in app.config
         */
        /* @ngInject */
        .constant('REST_BACKEND_SERVER', '')
        .config(['$provide', '$httpProvider', function($provide, $httpProvider) {
            $provide.factory('httpInterceptor', ['$q', '$location', '$rootScope', function($q, $location, $rootScope) {
                $rootScope.httpError = [];
                return {
                    requestError: function(rejection) {
                        return $q.reject(rejection);
                    },
                    response: function(response) {
                        return response || $q.when(response);
                    },
                    responseError: function(rejection) {
                        $rootScope.httpError.push(rejection);
                        //$location.path('/'); enable this if we want to redirect users to index.html on error
                        return $q.reject(rejection);
                    }
                };
            }]);

            $httpProvider.defaults.useXDomain = true;
            delete $httpProvider.defaults.headers.common['X-Requested-With'];
            $httpProvider.interceptors.push('httpInterceptor');
        }])

        /**
         * Configure Google Analytics
         */
        .config(['AnalyticsProvider', function(AnalyticsProvider) {
            AnalyticsProvider
                .setAccount('UA-87692241-2')
                .trackPages(true)
                .ignoreFirstPageLoad(true);
        }])
        .run(['Analytics', function(Analytics) {}])

        /**
         * App name
         */
        /* @ngInject */
        .run(['$rootScope', function($rootScope) {
            $rootScope.APP_NAME = 'MassBank of North America';
            $rootScope.APP_NAME_ABBR = 'MoNA';
            $rootScope.APP_VERSION = 'v1.0';
        }])

        /**
         * Prompt user before leaving the page if spectra are being uploaded.
         * Uses $injector to bypass timeout error when testing with protractor.
         */
        /* @ngInject */
        .run(['$window', '$injector', function($window, $injector) {
            $window.onbeforeunload = function(e) {
                var service = $injector.get('UploadLibraryService');

                if (service.isUploading()) {
                    var progress = (service.completedSpectraCount / service.uploadedSpectraCount) * 100;
                    return 'MoNA is ' + progress + '% done with processing and uploading spectra.';
                }
            };
        }]);
