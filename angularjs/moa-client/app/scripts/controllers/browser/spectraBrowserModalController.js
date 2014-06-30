moaControllers.ViewSpectrumModalController = function ($scope, $modalInstance, spectrum) {
    $scope.spectrum = spectrum;

    $scope.cancelDialog = function() {
        $modalInstance.dismiss('cancel');
    };
};