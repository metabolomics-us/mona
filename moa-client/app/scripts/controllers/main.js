(function() {
    'use strict';
    angular.module('moaClientApp')
      .controller('MainController', MainController);

    /* @ngInject */
    function MainController($scope, $rootScope, Flash) {

        $scope.slides = [
            {image: 'images/spectrum-1.png', id: '252', name: 'Cyclopamine'},
            {image: 'images/spectrum-2.png', id: '931', name: 'Ro-42130'}
        ];

        // console.log the error messages
        if ($rootScope.httpError.length > 0) {

            (function() {
                while ($rootScope.httpError.length !== 0) {
                    var curError = $rootScope.httpError.pop();

                    if (typeof curError !== 'undefined') {
                        var method = curError.config.method;
                        var url = curError.config.url;
                        var status = curError.status;

                        var message = 'Unable to ' + method + ' from ' + url + ' Status: ' + status;

                        console.log(message);
                    }
                }
            })();
        }
    }
})();

