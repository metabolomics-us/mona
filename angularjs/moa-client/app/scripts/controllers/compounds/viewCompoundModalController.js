/**
 * Created by Gert on 5/28/2014.
 */
'use strict';

moaControllers.ViewCompoundModalController = function ($scope, Submitter, $modalInstance, compound) {
    $scope.compound = compound;

    $scope.cancelDialog = function() {
        $modalInstance.dismiss('cancel');
        console.log($scope.compound)
    };
//
//	/**
//	 * contains our results
//	 * @type {{}}
//	 */
//	$scope.newSubmitter = newSubmitter;
//
//	/**
//	 * cancels any dialog in this controller
//	 */
//	$scope.cancelDialog = function () {
//		$modalInstance.dismiss('cancel');
//	};
//
//	/**
//	 * takes care of updates
//	 */
//	$scope.updateSubmitter = function () {
//
//		var submitter = createSubmitterFromScope();
//		//update the submitter
//		Submitter.update(submitter, function (data) {
//			$modalInstance.close(submitter);
//		}, function (error) {
//			handleDialogError(error);
//		});
//
//	};
//	/**
//	 * takes care of creates
//	 */
//	$scope.createNewSubmitter = function () {
//		var submitter = createSubmitterFromScope();
//		//no submitter id so create a new one
//		Submitter.save(submitter, function (savedSubmitter) {
//			$modalInstance.close(savedSubmitter);
//		}, function (error) {
//			handleDialogError(error);
//		});
//	};
//
//	/**
//	 * creates our submitter object
//	 */
//	function createSubmitterFromScope() {
//		//build our object
//		var submitter = new Submitter();
//		submitter.firstName = $scope.newSubmitter.firstName;
//		submitter.lastName = $scope.newSubmitter.lastName;
//		submitter.emailAddress = $scope.newSubmitter.emailAddress;
//		submitter.password = $scope.newSubmitter.password;
//
//		if ($scope.newSubmitter.id) {
//			submitter.id = $scope.newSubmitter.id;
//		}
//		return submitter;
//	}

};
