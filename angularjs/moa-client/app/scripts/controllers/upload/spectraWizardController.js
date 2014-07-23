/**
 * Created by Gert on 5/28/2014.
 */
'use strict';

/**
 * provides us with a spectra wizard controller to populate our database
 * @param $scope
 * @param $modalInstance
 * @param $window
 * @param $http
 * @param CTSService
 * @param TaggingService
 * @param AuthentificationService
 * @param newSpectrum
 * @constructor
 */
moaControllers.SpectraWizardController = function ($scope, $modalInstance, $window, $http, TaggingService, AuthentificationService,gwCtsService, newSpectrum,$log) {

    /**
     * definition of all our steps
     * @type {string[]}
     */
    $scope.steps = ['spectra', 'bioLogicalInchi', 'chemicalInChI', 'meta', 'tags', 'comment', 'summary'];

    /**
     * contains all possible chemical names
     * @type {Array}
     */
    $scope.possibleChemicalNames = [];

    /**
     * all possible names for a biological inchi
     * @type {Array}
     */
    $scope.possibleBiologicalNames = [];
    /**
     * our current step where we are at
     * @type {number}
     */
    $scope.step = 1;

    /**
     * this object contains all our generated data
     * @type {{}}
     */
    $scope.spectra = newSpectrum;

    /**
     * assign our submitter
     */
    AuthentificationService.getCurrentUser().then(function (data) {
        $scope.spectra.submitter = data;
    });

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
    $scope.isStepComplete = function (form) {

        //is the spectrum field valid
        if ($scope.getCurrentStep() === 'spectra') {
            if (form.spectrum.$valid) {
                if( !form.spectrum.$pristine){
                    return true;
                }
                //check our object if something is assigned
                else if(angular.isDefined($scope.spectra.spectrum)){
                    return true;
                }
            }
        }

        //the biological inchi key field is valid
        if ($scope.getCurrentStep() === 'bioLogicalInchi'){
            if( form.biologicalInchi.$valid && !form.biologicalInchi.$pristine) {
                return true;
            }
            else if(angular.isDefined($scope.spectra.biologicalCompound.names)){
                return true;
            }
        }

        //the chemical inchi page is complete
        if ($scope.getCurrentStep() === 'chemicalInChI') {
            if( form.chemicalInChI.$valid && !form.chemicalInChI.$pristine) {
                return true;
            }
            else if(angular.isDefined($scope.spectra.chemicalCompound.names)){
                return true;
            }
        }

        if ($scope.getCurrentStep() === 'meta') {
            return true;
        }

        if ($scope.getCurrentStep() === 'summary') {
            return true;
        }

        if ($scope.getCurrentStep() === 'tags') {
            return true;
        }

        if ($scope.getCurrentStep() === 'comment') {
            return true;
        }


        //we can only return when our wizard is valid
        return false
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


    /**
     * popluate the biological inchi names field
     */
    $scope.$watch('spectra.biologicalCompound.inchiKey', function () {

        //get all names for the inchi key

        //only if it's a valid inchi key we will query the server for valid names
        //if (key.match(/^([A-Z]{14}-[A-Z]{10}-[A-Z,0-9])+$/)) {
        if (angular.isDefined($scope.spectra.biologicalCompound.inchiKey)) {

            gwCtsService.getNamesForInChIKey($scope.spectra.biologicalCompound.inchiKey,function (result) {
                //$log.debug('recevied names: ' + result);

                $scope.possibleBiologicalNames = result;

            });
        }

        //}

    });

    /**
     * populate the chemical inchi name field
     */
    $scope.$watch('spectra.chemicalCompound.inchiKey', function () {

        //get all names for the inchi key

        //only if it's a valid inchi key we will query the server for valid names
        //if (key.match(/^([A-Z]{14}-[A-Z]{10}-[A-Z,0-9])+$/)) {

        if (angular.isDefined($scope.spectra.chemicalCompound.inchiKey)) {
            gwCtsService.getNamesForInChIKey($scope.spectra.chemicalCompound.inchiKey,function (result) {
                $log.debug('recevied names: ' + result);
                $scope.possibleChemicalNames = result;

            });

        }
        //}

    });

    /**
     * provides us with an overview of all our tags
     * @param query
     * @returns {*}
     */
    $scope.loadTags = function (query) {
        return TaggingService.query(function (data) {
        }, function (error) {
            alert('failed: ' + error);
        }).$promise
    };

};
