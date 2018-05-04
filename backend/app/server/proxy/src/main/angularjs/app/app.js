'use strict';

// CSS
import 'bootstrap/dist/css/bootstrap.min.css';

// Dependencies
import angular from 'angular';
import uirouter from '@uirouter/angularjs';
import ngResource from 'angular-resource';
import ngCookies from 'angular-cookies';
import ngAnimate from 'angular-animate';
import uiBootstrap from 'angular-ui-bootstrap'
import ngAnalytics from 'angular-google-analytics'

// App
import globals from './app.globals';
import analytics from './app.config.analytics';
import routing from './app.config.routing';

import AuthenticationController from './core/authentication.controller';
import AuthenticationService from './core/authentication.service';
import Cookieervice from './core/cookie.service';

export default angular.module('MoNA', [
  uirouter,
  ngResource,
  ngCookies,
  ngAnimate,
  uiBootstrap,
  ngAnalytics
//        'angular.filter',
//        'dialogs.main',
//        'ngTagsInput',
//        'wohlgemuth.msp.parser',
//        'wohlgemuth.mgf.parser',
//        'wohlgemuth.massbank.parser',
//        'wohlgemuth.cts',
//        'angularFileUpload',
//        'angularMasspecPlotter',
//        'toaster',
//        'nvd3',
])
  .config(routing)
  .config(analytics)
  .run(globals)

  .controller('AuthenticationController', AuthenticationController);

  /**
   * Global $http error handling
   * usage: in app.config
   */
  /* @ngInject */
//  .config(['$provide', '$httpProvider', function($provide, $httpProvider) {
//    $provide.factory('httpInterceptor', ['$q', '$location', '$rootScope', function($q, $location, $rootScope) {
//      $rootScope.httpError = [];
//
//      return {
//        requestError: function(rejection) {
//          return $q.reject(rejection);
//        },
//        response: function(response) {
//          return response || $q.when(response);
//        },
//        responseError: function(rejection) {
//          $rootScope.httpError.push(rejection);
//          //$location.path('/'); enable this if we want to redirect users to index.html on error
//          return $q.reject(rejection);
//        }
//      };
//    }]);
//
//    $httpProvider.defaults.useXDomain = true;
//    delete $httpProvider.defaults.headers.common['X-Requested-With'];
//    $httpProvider.interceptors.push('httpInterceptor');
//  }])

  /**
   * Prompt user before leaving the page if spectra are being uploaded.
   * Uses $injector to bypass timeout error when testing with protractor.
   */
  /* @ngInject */
//  .run(['$window', '$injector', function($window, $injector) {
//    $window.onbeforeunload = function(e) {
//      var service = $injector.get('UploadLibraryService');
//
//      if (service.isUploading()) {
//        var progress = parseInt(((service.completedSpectraCount / service.uploadedSpectraCount) * 100), 10);
//        return 'MoNA is ' + progress + '% done with processing and uploading spectra.';
//      }
//    };
//  }])