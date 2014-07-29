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
 * @param $location
 * @constructor
 */
moaControllers.SpectraBrowserController = function ($scope, Spectrum, Compound, TaggingService, $modal, $routeParams, SpectraQueryBuilderService, MetadataService, $log, $location) {
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
     * opens our modal dialog to query spectra against the system
     */
    $scope.querySpectraDialog = function () {
        var modalInstance = $modal.open({
            templateUrl: '/views/spectra/query/query.html',
            controller: moaControllers.QuerySpectrumModalController,
            size: 'lg',
            backdrop: 'true'

        });

        modalInstance.result.then(function (query) {
            $scope.submitQuery(query);
        });
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
                /**
                 * assign the spectra object
                 * @returns {*}
                 */
                spectrum: function () {
                    return $scope.spectra[index];
                },
                /**
                 * build a simple representation of the mass spectra and available annotation
                 */
                massSpec: function () {
                    var spec = $scope.spectra[index];

                    var meta = [];

                    //assemble our annotation matrix
                    for (var i = 0; i < spec.metaData.length; i++) {

                        if (spec.metaData[i].category === 'annotation') {
                            meta.push(spec.metaData[i]);
                        }
                    }


                    var regex = /([0-9]*\.?[0-9]+)+:([0-9]*\.?[0-9]+)/g;

                    var match = regex.exec(spec.spectrum);

                    var result = [];

                    while (match != null) {

                        var annotation;

                        for (var i = 0; i < meta.length; i++) {

                            if (meta[i].value == match[1]) {
                                annotation = meta[i].name;
                            }
                        }

                        result.push({ion: match[1], intensity: match[2], annotation: annotation});
                        match = regex.exec(spec.spectrum);

                    }

                    return result;
                }
            }
        });
    };

    /**
     * load more spectra
     */
    $scope.spectraLoadLength = -1;

    /**
     * compiled query which is supposed to be executed or refiened
     * @type {{}}
     */
    $scope.compiledQuery = {};

    /**
     * calculates our offsets for us
     */
    $scope.calculateOffsets = function () {
        $scope.spectraLoadLength = $scope.spectra.length;

        //assign the offset
        $scope.compiledQuery.offset = $scope.spectra.length;

    };

    /**
     * submits our build query to the backend
     */
    $scope.submitQuery = function (query) {

        $scope.compiledQuery = query;
        $scope.calculateOffsets();

        Spectrum.searchSpectra($scope.compiledQuery, function (data) {
            $log.info(data.length);

            $scope.spectra.length = 0;
            $scope.spectra.push.apply($scope.spectra, data);
        });
    };


    /*
     * Add inchikey to query if given in route
     */
    if ($routeParams.inchikey)
        $scope.submitQuery(SpectraQueryBuilderService.compileQuery({inchiFilter: $routeParams.inchikey}, {}));


    /**
     * loads more spectra into the view using our query object
     */
    $scope.loadingMore = false;

    /**
     * loads more spectra into the given view
     */
    $scope.loadMoreSpectra = function () {
        if ($scope.spectraLoadLength != $scope.spectra.length) {
            $scope.loadingMore = true;
            $scope.calculateOffsets();

            //search utilizing our compiled query so that it can be easily refined over time
            Spectrum.searchSpectra($scope.compiledQuery, function (data) {
                $scope.spectra.push.apply($scope.spectra, data);
                $scope.loadingMore = false;
            });
        }
    };

    /**
     * loads all our tags into the associated variables
     */
    $scope.loadTags = function () {
        $scope.tags = TaggingService.query(function (data) {
        }, function (error) {
            alert('failed: ' + error);
        });
    };

    /**
     * initialization and population of default values
     */
    (function list() {
        $scope.spectra = [];
        $scope.loadTags();
    })();

};