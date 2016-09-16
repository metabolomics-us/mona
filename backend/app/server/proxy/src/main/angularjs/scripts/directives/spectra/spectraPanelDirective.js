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
            return (typeof(s) === 'number') ?  s.toFixed(length) :  s;
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
        $scope.viewSpectrum = function() {
            // SpectrumCache.setBrowserSpectra($scope.spectrum);
            SpectrumCache.setSpectrum($scope.spectrum);

            return '/spectra/display/' + $scope.spectrum.id;
        };
    }
})();

