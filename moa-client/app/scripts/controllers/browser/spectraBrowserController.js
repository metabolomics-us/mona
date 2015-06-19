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
 * @param SpectrumCache
 * @constructor
 */
moaControllers.SpectraBrowserController = function ($scope, Spectrum, Compound, $modal, $routeParams, SpectraQueryBuilderService, MetadataService, TaggingService, $log, $location, SpectrumCache, $rootScope, $timeout, $filter) {

    $scope.table = false;
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
     * how many results are loaded
     * @type {number}
     */
    $scope.queryResultCount = 0;

    /**
     * reset the current query
     */
    $scope.resetQuery = function () {
        SpectraQueryBuilderService.prepareQuery();

        // Reset query refining
        $scope.nameFilter = '';
        $scope.inchiFilter = '';
        $scope.tagsSelection = [];

        // Submit blank query
        $scope.submitQuery();
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
        var query = SpectraQueryBuilderService.getQuery();


        //TODO why are we doing this?????
        if (query.compound.hasOwnProperty('name')) {
            $scope.nameFilter = query.compound.name.ilike.replace(/%/g, '');
        }

        //TODO or this?
        if (query.compound.hasOwnProperty('inchiKey')) {
            $scope.inchiFilter = query.compound.inchiKey.hasOwnProperty('eq') ?
                query.compound.inchiKey.eq : query.compound.inchiKey.like;
        }

        //TODO or this?
        // Add tags from query to refine query tags
        $scope.tagsSelection = [];

        for (var i = 0; i < query.tags.length; i++) {
            for (var j = 0; j < $scope.tags.length; j++) {
                if (query.tags[i] == $scope.tags[j].text) {
                    $scope.tagsSelection.push($scope.tags[j]);
                    break;
                }
            }
        }

        $scope.calculateResultCount();
        //actually load our data
        $scope.loadMoreSpectra();
    };

    /**
     * fires an event for directives to show the current query
     */
    $scope.displayQuery = function(){
        $rootScope.$broadcast('spectra:query:show');
    };

    /**
     * calculates how my results this current query will return
     */
    $scope.calculateResultCount = function(){

        //reports the count for the complete query response
        $scope.queryResultCount = "loading...";
        Spectrum.searchSpectraCount(SpectraQueryBuilderService.getQuery(), function (data) {
            $scope.queryResultCount = data.count;
        });

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
            $scope.submitQuery();
        });
    };

    /**
     * displays the spectrum for the given index
     * @param id
     * @param index
     */
    $scope.viewSpectrum = function (id, index) {
        SpectrumCache.setBrowserSpectra($scope.spectra);

        $location.path('/spectra/display/' + id);
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
                    break;
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

        //inform other controllers that we are starting to load spectra
        $rootScope.$broadcast('spectra:starting:query');

        if (!$scope.loadingMore && $scope.spectraLoadLength != $scope.spectra.length && $scope.dataAvailable) {
            //search utilizing our compiled query so that it can be easily refined over time
            $scope.loadingMore = true;
            $scope.calculateOffsets();

            Spectrum.searchSpectra(SpectraQueryBuilderService.getQuery(), function (data) {
                if (data.length == 0) {
                    $scope.dataAvailable = false;
                } else {
                    // Add data to spectra object
                    $scope.spectra.push.apply($scope.spectra, $scope.addAccurateMass(data));
                }

                $scope.loadingMore = false;
            });
        }

        //inform other controllers that we finished loading spectra
        if ($scope.spectra) {
            $rootScope.$broadcast('spectra:loaded', $scope.spectra);
        }
    };

    /**
     * calculates our offsets for us
     */
    $scope.calculateOffsets = function () {
        $scope.spectraLoadLength = $scope.spectra.length;

        var query = SpectraQueryBuilderService.getQuery();

        //assign the offset
        query.offset = $scope.spectra.length;

    };


    /**
     * our list view and default view
     */
    (function list() {
        // Get tags from cache
        TaggingService.query(
            function (data) {
                $scope.tags = data;
            },
            function (error) {
                $log.error('failed: ' + error);
            }
        );

        if (SpectrumCache.hasBrowserSpectra()) {
            var scrollPos = SpectrumCache.getBrowserSpectraScrollLocation();

            $scope.spectra = SpectrumCache.getBrowserSpectra();
            SpectrumCache.removeBrowserSpectra();

            $timeout(function () {
                $(window).scrollTop(scrollPos ? scrollPos : 0);
            }, 0);
        } else {
            $scope.spectra = [];

            // Submit our initial query
            $scope.submitQuery();
        }
    })();
};