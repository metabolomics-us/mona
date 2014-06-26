/**
 * Created by Gert on 5/28/2014.
 */
'use strict';

moaControllers.ViewSpectrumModalController = function ($scope, $modalInstance, compound) {
    $scope.spectrum = spectrum;

    $scope.cancelDialog = function() {
        $modalInstance.dismiss('cancel');
    };
};
