/**
 * Created by Gert on 5/28/2014.
 */
'use strict';

moaControllers.SpectraController = function ($scope, $modal) {

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
		});
	}
};

