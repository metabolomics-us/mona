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

        // create flash message on http error requests
        if ($rootScope.httpError.length > 0) {
            console.log($rootScope.httpError);
            (function() {
                while ($rootScope.httpError.length !== 0) {
                    var curError = $rootScope.httpError.pop();

                    if (typeof curError !== 'undefined') {
                        var method = curError.config.method;
                        var url = curError.config.url;
                        var status = curError.status;

                        var message = '<strong>Unable to</strong> ' + method + ' from ' + url + ' Status: ' + status;
                        var id = Flash.create('danger', message, 10000, {class: 'custom-class', id: 'custom-id'}, true);
                        /* First argument (string) is the type of the flash alert.
                         * Second argument (string) is the message displays in the flash alert (HTML is ok).
                         * Third argument (number, optional) is the duration of showing the flash. 0 to not automatically hide flash (user needs to click the cross on top-right corner).
                         * Fourth argument (object, optional) is the custom class and id to be added for the flash message created.
                         * Fifth argument (boolean, optional) is the visibility of close button for this flash.
                         * Returns the unique id of flash message that can be used to call Flash.dismiss(id); to dismiss the flash message.
                         */
                    }
                }
            })();
        }
    }
})();

