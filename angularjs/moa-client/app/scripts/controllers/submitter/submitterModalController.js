/**
 * Created by Gert on 5/28/2014.
 */
'use strict';

moaControllers.SubmitterModalController = function ($scope, Submitter, $modalInstance, newSubmitter) {

	/**
	 * contains our results
	 * @type {{}}
	 */
	$scope.newSubmitter = newSubmitter;

	/**
	 * cancels any dialog in this controller
	 */
	$scope.cancelDialog = function () {
		$modalInstance.dismiss('cancel');
	};

	/**
	 * takes care of updates
	 */
	$scope.updateSubmitter = function () {

		var submitter = createSubmitterFromScope();
		//update the submitter
		Submitter.update(submitter, function (data) {
			$modalInstance.close(submitter);
		}, function (error) {
			handleDialogError(error);
		});

	};
	/**
	 * takes care of creates
	 */
	$scope.createNewSubmitter = function () {
		var submitter = createSubmitterFromScope();
		//no submitter id so create a new one
		Submitter.save(submitter, function (savedSubmitter) {
			$modalInstance.close(savedSubmitter);
		}, function (error) {
			handleDialogError(error);
		});
	};

	/**
	 * creates our submitter object
	 */
	function createSubmitterFromScope() {
		//build our object
		var submitter = new Submitter();
		submitter.firstName = $scope.newSubmitter.firstName;
		submitter.lastName = $scope.newSubmitter.lastName;
		submitter.emailAddress = $scope.newSubmitter.emailAddress;
		submitter.password = $scope.newSubmitter.password;

		if ($scope.newSubmitter.id) {
			submitter.id = $scope.newSubmitter.id;
		}
		return submitter;
	}

	/**
	 * handles our dialog errors
	 * @param error
	 */
	function handleDialogError(error) {
		var errorReport = [];

		if (error.data) {
			for (var i = 0; i < error.data.errors.length; i++) {
				var obj = error.data.errors[i];

				//remove the none needed object
				delete obj.object;
				errorReport.push(obj);

			}

			$scope.formErrors = errorReport;
		}
		else {
			$scope.formErrors = "we had an unexpected error, please check the JS console";
		}
	}
};
