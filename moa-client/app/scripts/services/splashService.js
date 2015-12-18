/**
 * Created by sajjan on 8/14/15.
 */

(function() {
    'use strict';
    angular.module('moaClientApp')
      .factory('SplashService', SplashService);

    /* @ngInject */
    function SplashService($resource, $http) {
        $http.defaults.useXDomain = true;
        delete $http.defaults.headers.common['X-Requested-With'];

        return $resource(
          'http://cream.fiehnlab.ucdavis.edu:9292/splash.fiehnlab.ucdavis.edu/splash/it', {},
          {
              'splashIt': {
                  method: 'POST',
                  headers: {
                      'Content-Type': 'application/json'
                  },
                  isArray: false,
                  transformResponse: function(data, headersGetter, status) {
                      return {splash: data};
                  }
              }
          }
        );
    }
})();