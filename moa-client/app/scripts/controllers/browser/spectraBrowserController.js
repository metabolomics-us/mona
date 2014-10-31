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
 * @param TaggingService
 * @param $modal
 * @param $routeParams
 * @param SpectraQueryBuilderService
 * @param MetadataService
 * @param $log
 * @param $location
 * @constructor
 */
moaControllers.SpectraBrowserController = function ($scope, Spectrum, Compound, $modal, $routeParams, SpectraQueryBuilderService, MetadataService, $log, $location, AppCache, SpectrumCache, $rootScope) {
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
     * status of our accordion
     * @type {{isBiologicalCompoundOpen: boolean, isChemicalCompoundOpen: boolean, isDerivatizedCompoundOpen: boolean}}
     */
    $scope.accordionStatus = {
        isBiologicalCompoundOpen: true,
        isChemicalCompoundOpen: false,
        isDerivatizedCompoundOpen: false,
        isSpectraOpen: true,
        isIonTableOpen: false

    };

    /**
     * refine the current query by submitting an updates query
     */
    $scope.refineQuery = function () {
        var query = {};
        var tags = [];

        /**
         *  Add name to query if given in route
         */
        if ($routeParams.name && $routeParams.name != '') {
            query.nameFilter = $routeParams.name;
        }

        /*
         * Add inchikey to query if given in route
         */
        if ($routeParams.inchikey && $routeParams.inchikey != '') {
            query.inchiFilter = $routeParams.inchikey;
        }

        for (var i = 0; i < $scope.tagsSelection.length; i++) {
            tags.push($scope.tagsSelection[i].text);
        }

        // Reset spectra and perform the query
        $scope.submitQuery(SpectraQueryBuilderService.updateQuery(query, {}, tags, $rootScope.getSpectraQuery()));
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
            $scope.submitQuery(query);
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

        var query = $rootScope.getSpectraQuery();

        //assign the offset
        query.offset = $scope.spectra.length;

        $rootScope.setSpectraQuery(query);
    };

    /**
     * submits our build query to the backend
     */
    $scope.submitQuery = function (query) {
        $rootScope.setSpectraQuery(query);
        $scope.dataAvailable = true;

        // Reset spectra
        $scope.spectraLoadLength = -1;
        $scope.spectra = [];

        $scope.loadMoreSpectra();
    };


    /**
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

            Spectrum.searchSpectra($rootScope.getSpectraQuery(), function (data) {
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

        AppCache.getTags(function (data) {
            $scope.tags = data;
        });

        if (SpectrumCache.hasBrowserSpectra()) {
            $scope.spectra = SpectrumCache.getBrowserSpectra();
            SpectrumCache.removeBrowserSpectra();
        } else {
            $scope.spectra = [];
        }

        //submit our initial query
        $scope.refineQuery();
    })();
};