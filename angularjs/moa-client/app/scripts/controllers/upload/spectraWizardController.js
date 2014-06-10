/**
 * Created by Gert on 5/28/2014.
 */
'use strict';

moaControllers.SpectraWizardController = function ($scope, $modalInstance, $window, $http, CTSService, TaggingService,AuthentificationService) {


    /**
     * definition of all our steps
     * @type {string[]}
     */
    $scope.steps = ['spectra', 'bioLogicalInchi', 'chemicalInChI','meta', 'tags', 'comment', 'summary'];

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
    $scope.step = 4;

    /**
     * this object contains all our generated data
     * @type {{}}
     */
    $scope.spectra = {biologicalCompound: {}, chemicalCompound: {}, tags: [], metadata: []};

    /**
     * assign the currently logged in user as submitter
     */
    AuthentificationService.getCurrentUser().then(function(data){
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
    $scope.isStepComplete = function (uploadWizard) {

        //is the rawdata field valid
        if ($scope.getCurrentStep() === 'spectra' && uploadWizard.rawdata.$valid) {
            return true;
        }

        //the biological inchi key field is valid
        if ($scope.getCurrentStep() === 'bioLogicalInchi' && uploadWizard.biologicalInchi.$valid /*&& uploadWizard.biologicalInChIName.$valid*/) {

            return true;
        }

        //the chemical inchi page is complete
        if ($scope.getCurrentStep() === 'chemicalInChI' && uploadWizard.chemicalInChI.$valid /*&& uploadWizard.chemicalInChIName.$valid*/) {
            return true;
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


    /**
     * popluate the biological inchi names field
     */
    $scope.$watch('spectra.biologicalCompound.inchi', function () {

        //get all names for the inchi key

        //only if it's a valid inchi key we will query the server for valid names
        //if (key.match(/^([A-Z]{14}-[A-Z]{10}-[A-Z,0-9])+$/)) {
        if (angular.isDefined($scope.spectra.biologicalCompound.inchi)) {

            CTSService.getNamesForInChIKey($scope.spectra.biologicalCompound.inchi).then(function (result) {
                $scope.possibleBiologicalNames = result;

            });
        }

        //}

    });

    /**
     * populate the chemical inchi name field
     */
    $scope.$watch('spectra.chemicalCompound.inchi', function () {

        //get all names for the inchi key

        //only if it's a valid inchi key we will query the server for valid names
        //if (key.match(/^([A-Z]{14}-[A-Z]{10}-[A-Z,0-9])+$/)) {

        if (angular.isDefined($scope.spectra.chemicalCompound.inchi)) {
            CTSService.getNamesForInChIKey($scope.spectra.chemicalCompound.inchi).then(function (result) {
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
    $scope.loadTags = function(query) {
        return TaggingService.query(function (data) {
        }, function (error) {
            alert('failed: ' + error);
        }).$promise
    };
};
