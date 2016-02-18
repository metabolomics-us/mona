/**
 * Created by sajjan on 6/11/14.
 * /**
 * this controller is handling the browsing of compounds in the moa-client application
 *
 * @param $scope
 * @param Spectrum
 * @param Compound
 * @param $uibModal
 * @param $routeParams
 * @param SpectraQueryBuilderService
 * @param MetadataService
 * @param $log
 * @param $location
 * @param SpectrumCache
 * @constructor
 */


(function() {
    'use strict';
    angular.module('moaClientApp')
      .controller('SpectraBrowserController', SpectraBrowserController)

    /* @ngInject */
    function SpectraBrowserController($scope, Spectrum, Compound, $uibModal, $routeParams, SpectraQueryBuilderService,
                                      MetadataService, TaggingService, $log, $location, SpectrumCache, $rootScope, $timeout) {

        $scope.table = false;
        /**
         * contains all local objects and is our model
         * @type {Array}
         */
        $scope.spectra = [];

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
        $scope.resetQuery = function() {
            SpectraQueryBuilderService.prepareQuery();

            // Submit blank query
            $scope.submitQuery();
        };


        /**
         * submits our build query to the backend
         */
        $scope.submitQuery = function() {

            $scope.dataAvailable = true;

            // Reset spectra
            $scope.spectraLoadLength = -1;
            $scope.spectra = [];

            // Add query parameters to query refining
            var query = SpectraQueryBuilderService.getQuery();

            $scope.calculateResultCount();

            //actually load our data
            $scope.loadMoreSpectra();

        };

        /**
         * fires an event for directives to show the current query
         */
        $scope.displayQuery = function() {
            $rootScope.$broadcast('spectra:query:show');
        };

        /**
         * calculates how my results this current query will return
         */
        $scope.calculateResultCount = function() {
            //reports the count for the complete query response

            $scope.queryResultCount = "Loading...";

            Spectrum.searchSpectraCount(SpectraQueryBuilderService.getQuery(), function(data) {
                $scope.queryResultCount = data.count;
            });
        };

        /**
         * opens our modal dialog to query spectra against the system
         */
        $scope.querySpectraDialog = function() {
            var modalInstance = $uibModal.open({
                templateUrl: '/views/spectra/query/query.html',
                controller: 'QuerySpectrumModalController',
                size: 'lg',
                backdrop: 'true'
            });

            modalInstance.result.then(function(query) {
                $scope.submitQuery();
            });
        };

        /**
         * @Deprecated
         * displays the spectrum for the given index
         * @param id
         * @param index
         */
        $scope.viewSpectrum = function(id, index) {
            SpectrumCache.setBrowserSpectra($scope.spectra);
            SpectrumCache.setSpectrum($scope.spectra[index]);

            $location.path('/spectra/display/' + id);
        };


        /**
         * Get natural mass as accurate mass of spectrum
         */
        $scope.addAccurateMass = function(spectra) {
            for (var i = 0, l = spectra.length; i < l; i++) {
                var mass = '';

                if (angular.isDefined(spectra[i].biologicalCompound)) {
                    for (var j = 0; j < spectra[i].biologicalCompound.metaData.length; j++) {
                        if (spectra[i].biologicalCompound.metaData[j].name === 'total exact mass') {
                            mass = parseFloat(spectra[i].biologicalCompound.metaData[j].value).toFixed(3);
                            break;
                        }
                    }
                }

                spectra[i].accurateMass = mass;
            }

            return spectra;
        };

        /**
         * loads more spectra into the given view
         */
        $scope.loadMoreSpectra = function() {
            //inform other controllers that we are starting to load spectra
            $rootScope.$broadcast('spectra:starting:query');

            if (!$scope.loadingMore && $scope.spectraLoadLength !== $scope.spectra.length && $scope.dataAvailable) {
                //search utilizing our compiled query so that it can be easily refined over time
                $scope.loadingMore = true;
                $scope.spectraLoadLength = $scope.spectra.length;

                var payload = {
                    query: SpectraQueryBuilderService.getQuery(),
                    offset: $scope.spectra.length
                };

                Spectrum.searchSpectra(payload, function(data) {
                    // benchmark searchSpectra object
                    queryPerformance();

                    if (data.length === 0) {
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

        $scope.$on('$viewContentLoaded', function() {
            $timeout(function() {
                $(window).scrollTop($scope.spectraScrollStartLocation);
            }, 1);
        });


        var queryPerformance = function() {
            if(angular.isDefined(window.performance)) {
                var perfEntries = window.performance.getEntries();
                var last = perfEntries.length - 1;

                for (var i = last; i > -1; i--) {
                    var name = perfEntries[i].name;

                    if (name.indexOf('/rest/spectra/search?') > -1) {
                        $log.info("Name: " + perfEntries[i].name +
                            " Entry Type: " + perfEntries[i].entryType +
                            " Start Time: " + perfEntries[i].startTime +
                            " Duration: " + perfEntries[i].duration + "\n");

                        $scope.duration = perfEntries[i].duration / 1000;
                        break;
                    }
                }
            }
        };


        /**
         * our list view and default view
         */
        (function list() {
            $scope.spectraScrollStartLocation = 0;
            $scope.spectra = [];

            // Submit our initial query
            $scope.submitQuery();
        })();
    }
})();
