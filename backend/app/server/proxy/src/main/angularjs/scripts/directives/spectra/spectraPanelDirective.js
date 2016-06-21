/**
 * Created by wohlgemuth on 10/16/14.
 */
(function() {
    'use strict';

    displaySpectraPanelController.$inject = ['$scope', '$location', 'SpectrumCache', '$log'];
    angular.module('moaClientApp')
        .directive('displaySpectraPanel', displaySpectraPanel);

    function displaySpectraPanel() {
        var directive = {
            require: "ngModel",
            restrict: "A",
            templateUrl: '/views/spectra/display/panel.html',
            replace: true,
            scope: {
                spectrum: '=spectrum'
            },
            controller: displaySpectraPanelController
        };

        return directive;
    }

    /* @ngInject */
    function displaySpectraPanelController($scope, $location, SpectrumCache, $log) {

        var truncateDecimal = function(s, length) {
            return (typeof(s) === 'number') ?  s.toFixed(4) :  s;
            //TODO: using toFixed() for truncate of number, depreciate once verified
            /*var regex = new RegExp("\\s*(\\d+\\.\\d{" + length + "})\\d*\\s*");
            var m = s.match(regex);
            return (m !== null) ? s.replace(m[0].trim(), m[1]) : s;*/
        };

        angular.forEach($scope.spectrum.metaData, function(meta, index) {
            if (meta.category !== 'annotation' && meta.deleted !== 'true'
              && meta.hidden !== 'true' && meta.computed !== 'true') {
                meta.value = truncateDecimal(meta.value, 4);
            }
        });

        /**
         * displays the spectrum for the given index
         * @param id
         * @param index
         */
        $scope.viewSpectrum = function(id) {
            SpectrumCache.setBrowserSpectra($scope.spectrum);
            SpectrumCache.setSpectrum($scope.spectrum);
            $location.path('/spectra/display/' + $scope.spectrum.id);
        };
    }
})();

