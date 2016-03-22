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
        'ngSanitize',
        'ui.bootstrap',
        'angular.filter',
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
        'pascalprecht.translate',
        'viewhead'
    ])

        /**
         * Global $http error handling
         * usage: in app.config
         */
        /* @ngInject */
        .config(function($provide,$httpProvider) {
          $provide.factory('httpInterceptor', function ($q,$location) {
              return {
                  response: function (response) {
                      return response || $q.when(response);
                  },
                  responseError: function (rejection) {
                      $location.path('/');
                      return $q.reject(rejection);
                  }
              };
          });
          $httpProvider.interceptors.push('httpInterceptor');
        })

        /**
         * Set translator language for dialog service
         */
        /* @ngInject */
        .config(function($translateProvider) {
            $translateProvider.preferredLanguage('en-US');
            $translateProvider.useSanitizeValueStrategy('sanitize');
        })

        /**
         * App name
         */
        /* @ngInject */
        .run(function($rootScope) {
            $rootScope.APP_NAME = 'MassBank of North America';
            $rootScope.APP_NAME_ABBR = 'MoNA';
            $rootScope.APP_VERSION = 'alpha-2';
        })

        /**
         * Prompt user before leaving the page if spectra are being uploaded.
         * Uses $injector to bypass timeout error when testing with protractor.
         */
        /* @ngInject */
        .run(function($window, $injector) {
            $window.onbeforeunload = function(e) {
                var service = $injector.get('UploadLibraryService');

                if (service.isUploading()) {
                    var progress = parseInt(((service.completedSpectraCount / service.uploadedSpectraCount) * 100), 10);
                    return 'MoNA is ' + progress + '% done with processing and uploading spectra.';
                }
            };
        });
})();
