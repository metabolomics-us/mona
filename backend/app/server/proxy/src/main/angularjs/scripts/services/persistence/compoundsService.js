/**
 * Created by wohlgemuth on 6/12/14.
 */

(function() {
    'use strict';
    compound.$inject = ['$resource', 'REST_BACKEND_SERVER', 'MAX_COMPOUNDS'];
    angular.module('moaClientApp')
      .factory('Compound', compound);

    /* @ngInject */
    function compound($resource, REST_BACKEND_SERVER, MAX_COMPOUNDS) {
        return $resource(REST_BACKEND_SERVER + '/rest/compounds/:id?max=' + MAX_COMPOUNDS,
            {id: "@id", offset: "@offset"},
            {
                'update': {
                    method: 'PUT'
                }
            }
        );
    }
})();
