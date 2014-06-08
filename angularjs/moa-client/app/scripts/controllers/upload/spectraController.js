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
			alert(spectra);
		});
	};

	$scope.availableNames = [{name:'test'},{name: 'test-2'}];

	$scope.biologicalName = 1;

	$scope.counter = 1;
	$scope.$watch('biologicalName', function() {
		$scope.availableNames.push({name:'another one ' + $scope.counter});
		$scope.counter++;
	});

};

