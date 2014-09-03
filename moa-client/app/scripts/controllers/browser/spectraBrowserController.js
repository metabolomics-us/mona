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
moaControllers.SpectraBrowserController = function ($scope, Spectrum, Compound, TaggingService, $modal, $routeParams, SpectraQueryBuilderService, MetadataService, $log, $location, SpectrumCache) {
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
    $scope.viewSpectrum = function (id, index) {
        // Store spectra in cache
        SpectrumCache.put('spectra', $scope.spectra);
        SpectrumCache.put('viewSpectrum', $scope.spectra[index]);

        console.log(SpectrumCache.get('viewSpectrum'));
        $location.path('/spectra/display/'+ id);
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
            $scope.spectra.push.apply($scope.spectra, $scope.addAccurateMass(data));
        });
    };


    /**
     * Get natural mass as accurate mass of spectrum
     */
    $scope.addAccurateMass = function(spectra) {
        for(var i = 0; i < spectra.length; i++) {
            var mass = '';

            for(var j = 0; j < spectra[i].biologicalCompound.metaData.length; j++) {
                if(spectra[i].biologicalCompound.metaData[j].name === 'natural mass') {
                    mass = parseFloat(spectra[i].biologicalCompound.metaData[j].value).toFixed(3);
                }
            }

            spectra[i].accurateMass = mass;
        }

        return spectra;
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
     * Tells whether we are have loaded all available data
     */
    $scope.dataAvailable = true;

    /**
     * loads more spectra into the given view
     */
    $scope.loadMoreSpectra = function () {
        if(SpectrumCache.get('spectra') != null) {
            $scope.spectra = SpectrumCache.get('spectra');
            SpectrumCache.put('spectra', null)
        } else if ($scope.spectraLoadLength != $scope.spectra.length && $scope.dataAvailable) {
            //search utilizing our compiled query so that it can be easily refined over time
            $scope.loadingMore = true;
            $scope.calculateOffsets();

            Spectrum.searchSpectra($scope.compiledQuery, function (data) {
                if (data.length == 0) {
                    $scope.dataAvailable = false;
                } else {
                    // Add data to spectra object
                    $scope.spectra.push.apply($scope.spectra, $scope.addAccurateMass(data));
                }

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
        if(SpectrumCache.get('spectra') != null) {
            $scope.spectra = SpectrumCache.get('spectra');
            SpectrumCache.put('spectra', null)
        } else {
            $scope.spectra = [];
        }

        $scope.loadTags();
    })();

};