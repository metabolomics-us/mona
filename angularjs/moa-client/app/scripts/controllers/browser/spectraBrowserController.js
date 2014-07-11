/**
 * Created by sajjan on 6/11/14.
 */

'use strict';


/**
 * this controller is handling the browssing of compounds in the moa-client application
 *
 * @param $scope
 * @param Spectrum
 * @param Compound
 * @param TaggingService
 * @param $modal
 * @param $routeParams
 * @param SpectraQueryBuilderService
 * @param MetadataService
 * @param $log
 * @constructor
 */
moaControllers.SpectraBrowserController = function ($scope, Spectrum, Compound, TaggingService, $modal, $routeParams, SpectraQueryBuilderService, MetadataService, $log) {
    /**
     * contains all local objects and is our model
     * @type {Array}
     */
    $scope.spectra = [];

    /**
     * available tags
     * @type {Array}
     */
    $scope.tags = [];

    /**
     * available selection
     * @type {Array}
     */
    $scope.tagsSelection = [];


    // Name filter
    $scope.nameFilters = {};

    /**
     *  Add name to filter if given in route
     */
    if ($routeParams.name) {
        $scope.nameFilters[$routeParams.name] = true;
    }

    /**
     * adds the name filer
     */
    $scope.addNameFilter = function () {
        if ($scope.spectraQuery.newNameFilter.$valid) {
            $scope.nameFilters[$scope.newNameFilter] = true;
            $scope.newNameFilter = '';
        }
    };

    /**
     * removes the name filter
     * @param name
     */
    $scope.removeNameFilter = function (name) {
        delete $scope.nameFilters[name];
    };

    /**
     * all our inchi filters
     * @type {{}}
     */
    $scope.inchiFilters = {};

    /*
     * Add inchikey to filter if given in route
     */
    if ($routeParams.inchikey)
        $scope.inchiFilters[$routeParams.inchikey] = true;

    /**
     * applies the inchi key filter to our model
     */
    $scope.addInchiFilter = function () {
        if ($scope.spectraQuery.newInchiFilter.$valid) {
            $scope.inchiFilters[$scope.newInchiFilter] = true;
            $scope.newInchiFilter = '';
        }
    };

    /**
     * removes the inchi key filter from our model
     * @param inchikey
     */
    $scope.removeInchiFilter = function (inchikey) {
        delete $scope.inchiFilters[inchikey];
    };


    // Partial inchi filter
    $scope.partialInchiFilters = {};

    /**
     * adds the partial inchi key filter
     */
    $scope.addPartialInchiFilter = function () {
        if ($scope.spectraQuery.newInchiPartialFilter.$valid) {
            $scope.partialInchiFilters[$scope.newInchiPartialFilter] = true;
            $scope.newInchiPartialFilter = '';
        }
    };

    /**
     * removes the partial inchi key filter
     * @param inchikey
     */
    $scope.removePartialInchiFilter = function (inchikey) {
        delete $scope.partialInchiFilters[inchikey];
    };


    /**
     * displays the spectrum for the given index
     * @param index
     */
    $scope.viewSpectrum = function (index) {
        var modalInstance = $modal.open({
            templateUrl: '/views/spectra/display/viewSpectrum.html',
            controller: moaControllers.ViewSpectrumModalController,
            size: 'lg',
            backdrop: 'true',
            resolve: {
                spectrum: function () {
                    return $scope.spectra[index];
                }
            }
        });
    };

    /**
     * load more spectra
     */
    $scope.spectraLoadLength = -1;

    /**
     * loads more spectra into the view
     */
    $scope.loadMoreSpectra = function () {
        if ($scope.spectraLoadLength != $scope.spectra.length) {
            $scope.spectraLoadLength = $scope.spectra.length;

            Spectrum.query(
                {offset: '&offset=' + $scope.spectra.length},
                function (data) {
                    $scope.spectra.push.apply($scope.spectra, data);
                },
                function (error) {
                    alert('failed: ' + error);
                }
            );
        }
    };


    /* Metadata */
    $scope.metadataCategories = [];
    $scope.metadata = {};
    $scope.metadataValues = {};

    /**
     * builds our metadata values for the given query
     * @param data
     */
    var metadataValuesQuery = function (data) {
        data.forEach(function (element, index, array) {
            if (element.type === "string") {
                var values = {};

                MetadataService.dataValues(
                    {id: element.id},
                    function (data) {
                        data.forEach(function (element, index, array) {
                            values[element.value] = true;
                        });

                        $scope.metadataValues[element.name] = [];
                        Object.keys(values).forEach(function (key, index, array) {
                            $scope.metadataValues[element.name].push({value: key});
                        });
                    },
                    function (error) {
                        $log.error('metadata values failed: ' + error);
                    }
                );
            }
        });
    };

    var metadataQuery = function (data) {
        // Query each metdata category and store the data
        data.forEach(function (element, index, array) {
            $scope.metadata[element.name] = MetadataService.categoryData(
                {id: element.id},
                metadataValuesQuery,
                function (error) {
                    $log.error('metadata category data failed: ' + error);
                }
            );
        });
    };

    /**
     * contains our build query object
     * @type {{}}
     */
    $scope.query = {};

    /**
     * submits our build query to the backend
     */
    $scope.submitQuery = function () {

        $scope.compiledQuery = SpectraQueryBuilderService.compileQuery($scope.query, $scope.metadata);

        Spectrum.searchSpectra($scope.compiledQuery, function (data) {

            $log.info(data.length);
            $scope.result = data;
        });
    };


    /**
     * initialization and population of default values
     */
    (function list() {

        /**
         * intialize all the tags
         */
        $scope.tagsSelection = $scope.tags = TaggingService.query(function (data) {
        }, function (error) {
            alert('failed: ' + error);
        });

        /**
         * initialize our metadata categories
         */
        $scope.metadataCategories = MetadataService.categories(
            metadataQuery,
            function (error) {
                $log.error('metadata categories failed: ' + error);
            }
        );
    })();

};