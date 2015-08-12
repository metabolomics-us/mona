/**
 * Created by wohlgemuth on 10/16/14.
 */
app.directive('displaySpectraPanel', function () {
    return {
        require: "ngModel",
        restrict: "A",
        replace: true,
        scope: {
            spectrum: '=spectrum'
        },
        templateUrl: '/views/spectra/display/template/panel.html',
        controller: function ($scope, $location,SpectrumCache) {

            /**
             * displays the spectrum for the given index
             * @param id
             * @param index
             */
            $scope.viewSpectrum = function (id) {
                SpectrumCache.setBrowserSpectra($scope.spectrum);
                SpectrumCache.setSpectrum($scope.spectrum);

                $location.path('/spectra/display/' + $scope.spectrum.id);
            };

        }
    };
});
