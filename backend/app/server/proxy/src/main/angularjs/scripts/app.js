/***************************************
 * Our main module, and configs. Read the README.md for best practices
 * before making any changes to moaClientApp
 * *************************************/

(function() {
    'use strict';

    angular.module('moaClientApp', [
        'ngRoute',
        'ngResource',
        'ngCookies',
        'ngAnimate',
        'ui.bootstrap',
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
                    var progress = parseInt(((service.completedSpectraCount / service.uploadedSpectraCount) * 100), 10);
                    return 'MoNA is ' + progress + '% done with processing and uploading spectra.';
                }
            };
        }]);
})();
