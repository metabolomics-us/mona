/**
 * Created by wohlgemuth on 10/16/14.
 */
(function() {
    'use strict';

    app.directive('displaySpectraPanel', function() {
        return {
            require: "ngModel",
            restrict: "A",
            replace: true,
            scope: {
                spectrum: '=spectrum'
            },
            templateUrl: '/views/spectra/display/panel.html',
            controller: ['$scope', '$location', 'SpectrumCache', function($scope, $location, SpectrumCache) {

                var truncateDecimal = function(s, length) {
                    var regex = new RegExp("\\s*(\\d+\\.\\d{" + length + "})\\d*\\s*");
                    var m = s.match(regex);
                    return (m !== null) ? s.replace(m[0].trim(), m[1]) : s;
                }

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
            }]
        };
    });
})();

