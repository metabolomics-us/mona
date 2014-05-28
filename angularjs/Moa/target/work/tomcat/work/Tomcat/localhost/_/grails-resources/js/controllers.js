/**
 * these are all our controllers
 */

var controllers = {};

/**
 * handles all interactions with submitters
 * @param $scope
 * @param Submitter
 * @constructor
 */
controllers.SubmitterController = function ($scope, Submitter, $modal) {

    /**
     * contains all local objects
     * @type {Array}
     */
    $scope.submitters = [];

    /**
     * list all our submitters in the system
     */
    $scope.listSubmitter = list();

    /**
     * deletes our submitter from the system
     * @param submitterId
     */
    $scope.remove = function (index) {
        var submitterToRemove = $scope.submitters[index];

        Submitter.delete({id: submitterToRemove.id}, function (data) {

                //remove it from the scope and update our table
                $scope.submitters.splice(index, 1);
            },
            function (errors) {
                alert('oh noes an error...');
            }
        );
    };

    /**
     * displays our dialog to create a new submitter
     */
    $scope.displayCreateDialog = function () {

        var modalInstance = $modal.open({
            templateUrl: '/partial/submitters/dialog/createDialog.html',
            controller: controllers.SubmitterModalController,
            size: 'lg',
            resolve: {
                //just an empty object
                newSubmitter: function () {
                    return {};
                }
            }
        });

        //retrieve the result from the dialog and save it
        modalInstance.result.then(function (submitter) {
            //push our object to the scope now so that our table can show it
            $scope.submitters.push(submitter);
        })
    };

    /**
     * displays the edit dialog for the select submitter
     * @param index
     */
    $scope.displayEditDialog = function (index) {
        var modalInstance = $modal.open({
            templateUrl: '/partial/submitters/dialog/editDialog.html',
            controller: controllers.SubmitterModalController,
            size: 'lg',
            resolve: {
                //populate the dialog with the given submitter at this index
                newSubmitter: function () {
                    return $scope.submitters[index];
                }
            }
        });

        //retrieve the result from the dialog and save it
        modalInstance.result.then(function (submitter) {
            //will be handled automatically by angular js
        });
    };

    /**
     * helper function
     */
    function list() {
        $scope.submitters = Submitter.query(function (data) {
        }, function (error) {
            alert('failed: ' + error);
        })
    }

};

/**
 *
 * general controller for modal dialog, which are used to create submitters
 * @param $scope
 * @param Submitter
 * @param $modalInstance
 * @param newSubmitter
 * @constructor
 */
controllers.SubmitterModalController = function ($scope, Submitter, $modalInstance, newSubmitter) {

    /**
     * contains our results
     * @type {{}}
     */
    $scope.newSubmitter = newSubmitter;

    /**
     * cancels any dialog in this controller
     */
    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };

    /**
     * takes care of updates
     */
    $scope.update = function () {

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
    $scope.create = function () {

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

/**
 * handles all interactions with spectra
 * @constructor
 */
controllers.SpectraController = function ($scope, $modal) {

    /**
     * initializes our spectra upload dialog
     */
    $scope.uploadSpectraDialog = function () {
        var modalInstance = $modal.open({
            templateUrl: '/partial/upload/dialog/wizard.html',
            controller: controllers.SpectraWizardController,
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

/**
 * wizard to upload spectra
 * @param $scope
 * @constructor
 */
controllers.SpectraWizardController = function ($scope, $modalInstance, $window,MolConverter,$http) {

    /**
     * definition of all our steps
     * @type {string[]}
     */
    $scope.steps = ['spectra', 'inchi', 'name', 'meta'];

    $scope.step = 0;

    $scope.spectra = {inchi:""};

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

    $scope.isStepComplete = function (uploadWizard) {
        //is the rawdata field valid
        if ($scope.getCurrentStep() === 'spectra' && uploadWizard.rawdata.$valid) {
            return true;
        }

        //the inchi key field is valid
        if ($scope.getCurrentStep() === 'inchi' && uploadWizard.inchi.$valid) {
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
            $modalInstance.close();
        } else {
            $scope.step += 1;
        }
    };
};


/**
 * handles all our navigations
 * @param $scope
 * @param $location
 * @constructor
 */
controllers.NavigationController = function ($scope, $location) {
    $scope.navClass = function (page) {
        var currentRoute = $location.path().substring(1) || 'home';
        return page === currentRoute ? 'active' : '';
    };
};

//register the actual controllers
massspecsOfAmerica.controller(controllers);
