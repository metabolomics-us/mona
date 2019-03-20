(function() {
    'use strict';

    feedbackService.$inject = ['$resource', 'REST_BACKEND_SERVER', '$http'];
    angular.module('moaClientApp')
      .factory('Feedback', feedbackService);

    /* @ngInject */
    function feedbackService($resource, REST_BACKEND_SERVER, $http) {

        $http.defaults.useXDomain = true;

        return $resource(
            REST_BACKEND_SERVER + '/rest/feedback/:id',
            {id: "@id"},
            {
                'update': {
                    method: 'PUT'

                }
            }
        );
    }
})();
