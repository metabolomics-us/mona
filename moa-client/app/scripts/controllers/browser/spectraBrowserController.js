/**
 * Created by sajjan on 6/11/14.
 */

'use strict';


/**
 * this controller is handling the browsing of compounds in the moa-client application
 *
 * @param $scope
 * @param Spectrum
 * @param Compound
 * @param $modal
 * @param $routeParams
 * @param SpectraQueryBuilderService
 * @param MetadataService
 * @param $log
 * @param $location
 * @param AppCache
 * @param SpectrumCache
 * @param QueryCache
 * @constructor
 */
moaControllers.SpectraBrowserController = function ($scope, Spectrum, Compound, $modal, $routeParams, SpectraQueryBuilderService, MetadataService, $log, $location, AppCache, SpectrumCache, QueryCache) {
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

    /**
     * load more spectra
     */
    $scope.spectraLoadLength = -1;

    /**
     * loads more spectra into the view using our query object
     */
    $scope.loadingMore = false;

    /**
     * Tells whether we are have loaded all available data
     */
    $scope.dataAvailable = true;


    /**
     * refine the current query by submitting an updates query
     */
    $scope.refineQuery = function () {
        var query = {};
        var tags = [];

        // Add name and inchikey to query if provided
        if ($scope.nameFilter != '') {
            query.nameFilter = $scope.nameFilter;
        }

        if ($scope.inchiFilter != '') {
            query.inchiFilter = $scope.inchiFilter;
        }

        // Add selected tags to query
        for (var i = 0; i < $scope.tagsSelection.length; i++) {
            tags.push($scope.tagsSelection[i].text);
        }

        // Reset spectra and perform the query
        SpectraQueryBuilderService.updateQuery(query, {}, tags);

        $scope.submitQuery();
    };

    /**
     * reset the current query
     */
    $scope.resetQuery = function() {
        // Reset query object
        SpectraQueryBuilderService.prepareQuery();

        // Reset query refining
        $scope.nameFilter = '';
        $scope.inchiFilter = '';
        $scope.tagsSelection = [];

        // Submit blank query
        $scope.submitQuery();
    };

    /**
     * opens our modal dialog to query spectra against the system
     */
    $scope.querySpectraDialog = function () {
        var modalInstance = $modal.open({
            templateUrl: '/views/spectra/query/query.html',
            controller: moaControllers.QuerySpectrumModalController,
            size: 'lg',
            backdrop: 'true',
            resolve: {
                tags: function () {
                    return $scope.tags;
                }
            }
        });

        modalInstance.result.then(function (query) {
            $scope.submitQuery();
        });
    };

    /**
     * displays the spectrum for the given index
     * @param index
     */
    $scope.viewSpectrum = function (id, index) {
        // Store spectra in cache
        SpectrumCache.setBrowserSpectra($scope.spectra);
        SpectrumCache.setSpectrum($scope.spectra[index]);

        $location.path('/spectra/display/' + id);
    };

    /**
     * show the currently selected sprectra
     * @param inchikey
     */
    $scope.viewAssociatedSpectra = function (inchikey) {
        $location.path("/spectra/browse/" + inchikey);
    };

    /**
     * calculates our offsets for us
     */
    $scope.calculateOffsets = function () {
        $scope.spectraLoadLength = $scope.spectra.length;

        var query = QueryCache.getSpectraQuery();

        //assign the offset
        query.offset = $scope.spectra.length;

        QueryCache.setSpectraQuery(query);
    };

    /**
     * submits our build query to the backend
     */
    $scope.submitQuery = function () {
        $scope.dataAvailable = true;

        // Reset spectra
        $scope.spectraLoadLength = -1;
        $scope.spectra = [];

        // Add query parameters to query refining


        $scope.loadMoreSpectra();
    };


    /**
     * TODO remove
     * Get natural mass as accurate mass of spectrum
     */
    $scope.addAccurateMass = function (spectra) {
        for (var i = 0; i < spectra.length; i++) {
            var mass = '';

            for (var j = 0; j < spectra[i].biologicalCompound.metaData.length; j++) {
                if (spectra[i].biologicalCompound.metaData[j].name === 'total exact mass') {
                    mass = parseFloat(spectra[i].biologicalCompound.metaData[j].value).toFixed(3);
                }
            }

            spectra[i].accurateMass = mass;
        }

        return spectra;
    };

    /**
     * loads more spectra into the given view
     */
    $scope.loadMoreSpectra = function () {
        if (SpectrumCache.hasBrowserSpectra()) {
            $scope.spectra = SpectrumCache.getBrowserSpectra();
            SpectrumCache.removeBrowserSpectra();
        }

        else if ($scope.spectraLoadLength != $scope.spectra.length && $scope.dataAvailable) {
            //search utilizing our compiled query so that it can be easily refined over time
            $scope.loadingMore = true;
            $scope.calculateOffsets();

            Spectrum.searchSpectra(QueryCache.getSpectraQuery(), function (data) {
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
     * our list view and default view
     */
    (function list() {
        // Get tags from cache
        AppCache.getTags(function (data) {
            $scope.tags = data;
        });

        // Load spectra if available
        if (SpectrumCache.hasBrowserSpectra()) {
            $scope.spectra = SpectrumCache.getBrowserSpectra();
            SpectrumCache.removeBrowserSpectra();
        } else {
            $scope.spectra = [];
        }

        // Add name and inchikey to query if given in route
        if ($routeParams.name && $routeParams.name != '') {
            $scope.nameFilter = $routeParams.name;
        }

        if ($routeParams.inchikey && $routeParams.inchikey != '') {
            $scope.inchiFilter = $routeParams.inchikey;
        }

        // Submit our initial query
        $scope.refineQuery();
    })();
};