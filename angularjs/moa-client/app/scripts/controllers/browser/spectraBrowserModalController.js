/**
 * displays our spectra
 * @param $scope
 * @param $modalInstance
 * @param spectrum
 * @param massSpec
 * @constructor
 */
moaControllers.ViewSpectrumModalController = function ($scope, $modalInstance, spectrum,massSpec) {
    $scope.spectrum = spectrum;
    $scope.massSpec = massSpec;

    $scope.cancelDialog = function() {
        $modalInstance.dismiss('cancel');
    };
};