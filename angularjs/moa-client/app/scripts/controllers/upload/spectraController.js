/**
 * Created by Gert on 5/28/2014.
 */
'use strict';

moaControllers.SpectraController = function ($scope, $modal, CTSService) {

    /**
     * initializes our spectra upload dialog
     */
    $scope.uploadSpectraDialog = function () {
        var modalInstance = $modal.open({
            templateUrl: '/views/upload/dialog/wizard.html',
            controller: moaControllers.SpectraWizardController,
            size: 'lg',
            backdrop: 'static',
            resolve: {

            }
        });

        //retrieve the result from the dialog and save it
        modalInstance.result.then(function (spectra) {
            alert(spectra);
        });
    };

    $scope.availableNames = [
        {name: 'test'},
        {name: 'test-2'}
    ];

    $scope.biologicalName = 1;

    $scope.$watch('biologicalName', function () {

        //get all names for the inchi key
        var key = "QNAYBMKLOCPYGJ-UWTATZPHSA-N";

        //only if it's a valid inchi key we will query the server for valid names
        //if (key.match(/^([A-Z]{14}-[A-Z]{10}-[A-Z,0-9])+$/)) {
            CTSService.getNamesForInChIKey(key).then(function (result) {
                $scope.availableNames = result;

            });
        //}

    });

};

