import * as angular from 'angular';

    MainController.$inject = ['$scope', '$rootScope', 'Spectrum', '$log'];
    angular.module('moaClientApp')
        .controller('MainController', MainController);

    /* @ngInject */
    function MainController($scope, $rootScope, Spectrum, $log) {


        $scope.showcaseSpectraIds = ['BSU00002', 'AU101801', 'UT001119'];
        $scope.showcaseSpectra = [];

        (function() {
            $scope.showcaseSpectraIds.forEach(function(id) {
                Spectrum.get(
                    {id: id},
                    function(data) {
                        $scope.showcaseSpectra.push(data);
                    },
                    function(error) {
                        $log.error("Failed to obtain spectrum "+ id)
                    }
                );
            });

            // console.log any Http error messages
            while ($rootScope.httpError.length !== 0) {
                var curError = $rootScope.httpError.pop();

                if (angular.isDefined(curError)) {
                    var method = curError.config.method;
                    var url = curError.config.url;
                    var status = curError.status;

                    var message = 'Unable to ' + method + ' from ' + url + ' Status: ' + status;

                    $log.error(message);
                }
            }
        })();
    }

