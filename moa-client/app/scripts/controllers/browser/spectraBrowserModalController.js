/**
 * displays our spectra
 * @param $scope
 * @param $modalInstance
 * @param spectrum
 * @param massSpec
 * @constructor
 */
moaControllers.ViewSpectrumController = function ($scope, Spectrum, $routeParams, $log) {
    // Query for specific spectrum object
    $scope.spectrum = {
        spectrum: ''
    };
    $scope.massSpec = [];

    (function() {
        Spectrum.get(
            {id: $routeParams.id},

            function (data) {
                // Regular expression to extract ions
                var regex = /([0-9]*\.?[0-9]+)+:([0-9]*\.?[0-9]+)/g;

                $log.info(JSON.stringify(data, null, 2));

                // Parse spectrum string to generate ion list
                $scope.massSpec = [];
                var match;

                while ((match = regex.exec(data.spectrum)) != null) {
                    $scope.massSpec.push({ion: match[1], intensity: match[2]});
                }
            },

            function (error) {
                alert('failed to obtain spectrum: ' + error);
            }
        );
    })();
};