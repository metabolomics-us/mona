/**
 * Created by wohlgemuth on 2/12/15.
 *
 * where can we find our rest connections
 * DEV address: http://cream.fiehnlab.ucdavis.edu:8080
 * alternative production: http://cream.fiehnlab.ucdavis.edu:9292/mona.fiehnlab.ucdavis.edu
 * http://0.0.0.0:9292/mona.fiehnlab.ucdavis.edu
 */

(function() {
    'use strict';
    angular.module('moaClientApp')
        .constant('REST_BACKEND_SERVER', 'http://0.0.0.0:9292/cream.fiehnlab.ucdavis.edu:8080');
})();