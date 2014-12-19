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
moaControllers.SpectraUploadWizardController = function ($scope, $q, $modalInstance, $http, $window, $filter, AppCache, AuthentificationService, UploadLibraryService, $log) {
    //
    // Define wizard steps
    //

    /**
     * definition of all our steps
     * @type {string[]}
     */
    $scope.steps = ['spectra', 'loading', 'compound', 'metadata', 'tags', 'comments', 'summary'];

    /**
     * our current step where we are at
     * @type {number}
     */
    $scope.step = 0;

    /**
     *
     * @type {string}
     */
    $scope.error = [];


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

        if($scope.getCurrentStep() === 'loading') {
            $scope.step--;
        }
    };

    /**
     * checks if the current step is complete of the wizard
     * @param uploadWizard
     * @returns {boolean}
     */
    $scope.isStepComplete = function (form) {
        if ($scope.getCurrentStep() === 'spectra') {
            return ($scope.files.length > 0);
        }

        if ($scope.getCurrentStep() === 'loading') {
            return false;
        }

        if ($scope.getCurrentStep() === 'compound') {
            return true;
        }

        if ($scope.getCurrentStep() === 'metadata') {
            return true;
        }

        if ($scope.getCurrentStep() === 'tags') {
            return true;
        }

        if ($scope.getCurrentStep() === 'comments') {
            return true;
        }

        if ($scope.getCurrentStep() === 'summary') {
            return true;
        }

        // We can only return when our wizard is valid
        return false;
    };

    /**
     * next step
     * @param dismiss
     */
    $scope.handleNext = function (dismiss) {
        // Reset error message
        $scope.error = [];

        // Scroll to the top of the window, useful for screens like metadata
        $window.scrollTo(0, 0);


        if ($scope.isLastStep()) {
            submitSpectra();
        } else if ($scope.getCurrentStep() === 'spectra') {
            $scope.step += 1;
            loadSpectra();
        } else {
            $scope.step += 1;
        }
    };

    /**
     *
     */
    var loadSpectra = function() {
        $scope.loadingStatus = 'Loading... 0%';

        // If there are multiple files, continue to batch uploader
        if($scope.files.length > 1) {
            $scope.step += 2;
            $scope.batchUpload = true;
        }

        // Otherwise, parse the file and determine the number of spectra in the file
        else {
            UploadLibraryService.loadSpectraFile($scope.files[0], function(data, origin) {
                // Count the number of spectra in the file
                var count = UploadLibraryService.countData(data, $scope.files[0].name);

                if(count == 1) {
                    $scope.loadingStatus = 'Processing...';

                    UploadLibraryService.processData(data, function(spectrum) {
                        console.log(spectrum)
                        $scope.loadingStatus = 'Completed';

                        $scope.spectrum = spectrum;
                        $scope.spectrum.meta.push({name: '', value: ''});

                        $scope.step += 1;
                        $scope.batchUpload = false;
                    }, origin);
                } else {
                    $scope.step += 2;
                    $scope.batchUpload = true;
                }
            }, function(progress) {
                $scope.loadingStatus = 'Loading...'+ progress +'%';
            });
        }
    };

    /**
     *
     */
    var submitSpectra = function() {
        if($scope.batchUpload) {
            UploadLibraryService.uploadSpectra($scope.files, function (spectrum) {
                $log.info('Final spectrum');
                $log.info(spectrum);
                spectrum.$batchSave();
            }, $scope.spectrum);
        } else {
            UploadLibraryService.uploadSpectrum($scope.spectrum, function (spectrum) {
                $log.info('Final spectrum');
                $log.info(spectrum);
                spectrum.$save();
            });
        }

        $modalInstance.close(true);
    };



    /**
     * assign our submitter
     */
    AuthentificationService.getCurrentUser().then(function (data) {
        $scope.submitter = data;
    });


    /**
     *
     * @param files
     */
    $scope.loadSpectraFiles = function(files) {
        $scope.errors = [];
        $scope.files = [];

        // Valid file properties
        for(var i = 0; i < files.length; i++) {
            var extension = files[i].name.split('.').pop().toLowerCase();

            if(files[i].size > 26214400*4) {
                $scope.errors.push(files[i].name +' exceeds the 100 Mb upload limit and will be excluded');
            }

            else if(extension != 'msp' && extension != 'txt' && extension != 'mgf') {
                $scope.errors.push(files[i].name +' is not an accepted file type and will be excluded');
            }

            else {
                $scope.files.push(files[i]);
            }
        }

        // Set selected filenames
        if($scope.files.length > 0) {
            $scope.filenames = $scope.files[0].name + ($scope.files.length > 1 ? ' + '+ ($scope.files.length - 1) + ' file(s)' : '');
        } else {
            $scope.filenames = '';
        }
    };


    /**
     * provides us with an overview of all our tags
     * @param query
     * @returns {*}
     * Performs initialization and acquisition of data used by the wizard
     */
    $scope.loadTags = function (query) {
        var deferred = $q.defer();

        // First filters by the query and then removes any tags already selected
        deferred.resolve($filter('filter')($scope.tags, query));

        return deferred.promise;
    };


    /**
     * Performs initialization and acquisition of data used by the wizard
     */
    (function() {
        // Define new spectrum
        $scope.spectrum = {
            meta: []
        };

        // Set file lists
        $scope.files = [];
        $scope.filenames = '';

        // Get tags
        AppCache.getTags(function(data) {
            $scope.tags = data;
        });
    })();
};