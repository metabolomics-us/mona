'use strict';

/**
 * Configure global variables
 */
export default function globals($rootScope) {
  $rootScope.APP_NAME = 'MassBank of North America';
  $rootScope.APP_NAME_ABBR = 'MoNA';
  $rootScope.APP_VERSION = 'v1.0';
}

globals.$inject = ['$rootScope'];