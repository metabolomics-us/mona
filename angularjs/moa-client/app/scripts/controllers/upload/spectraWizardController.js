/**
 * Created by Gert on 5/28/2014.
 */
'use strict';

moaControllers.SpectraWizardController = function ($scope, $modalInstance, $window,MolConverter,$http) {

	/**
	 * definition of all our steps
	 * @type {string[]}
	 */
	$scope.steps = ['spectra', 'bioLogicalInchi','chemicalInChI', 'meta','summary'];

	/**
	 * our current step where we are at
	 * @type {number}
	 */
	$scope.step = 0;

	/**
	 * this object contains all our generated data
	 * @type {{}}
	 */
	$scope.spectra = {};

	/**
	 * is this our current step
	 * @param step
	 * @returns {boolean}
	 */
	$scope.isCurrentStep = function (step) {
		return $scope.step === step;
	};

	/**
	 * set the current step
	 * @param step
	 */
	$scope.setCurrentStep = function (step) {
		$scope.step = step;
	};

	/**
	 * the current step of the wizard
	 * @returns {*}
	 */
	$scope.getCurrentStep = function () {
		return $scope.steps[$scope.step];
	};

	/**
	 * are we on the first step
	 * @returns {boolean}
	 */
	$scope.isFirstStep = function () {
		return $scope.step === 0;
	};

	/**
	 * are we on the last step
	 * @returns {boolean}
	 */
	$scope.isLastStep = function () {
		return $scope.step === ($scope.steps.length - 1);
	};

	/**
	 * returns the label of the current step
	 * @returns {string}
	 */
	$scope.getNextLabel = function () {
		return ($scope.isLastStep()) ? 'Submit' : 'Next';
	};

	/**
	 * previous step
	 */
	$scope.handlePrevious = function () {
		$scope.step -= ($scope.isFirstStep()) ? 0 : 1;
	};

	/**
	 * checks if the current step is complete of the wizard
	 * @param uploadWizard
	 * @returns {boolean}
	 */
	$scope.isStepComplete = function (uploadWizard) {

		//is the rawdata field valid
		if ($scope.getCurrentStep() === 'spectra' && uploadWizard.rawdata.$valid && !uploadWizard.rawdata.$pristine) {
			return true;
		}

		//the biological inchi key field is valid
		if ($scope.getCurrentStep() === 'bioLogicalInchi' && uploadWizard.biologicalInchi.$valid && uploadWizard.biologicalInChIName.$valid) {

			return true;
		}

		//the chemical inchi page is complete
		if ($scope.getCurrentStep() === 'chemicalInChI' && uploadWizard.chemicalInChI.$valid && uploadWizard.chemicalInChIName.$valid) {
			return true;
		}

		if($scope.getCurrentStep() === 'meta'){
			return true;
		}

		if($scope.getCurrentStep() === 'summary'){
			return true;
		}

		//nope we are done
		return false;
	};
	/**
	 * next step
	 * @param dismiss
	 */
	$scope.handleNext = function (dismiss) {
		if ($scope.isLastStep()) {
			$modalInstance.close($scope.spectra);
		} else {
			$scope.step += 1;
		}
	};
};
