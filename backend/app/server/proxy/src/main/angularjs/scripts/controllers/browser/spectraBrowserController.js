/**
 * Created by sajjan on 6/11/14.
 *
 * This controller is handling the browsing of spectra
 */

(function() {
    'use strict';
    SpectraBrowserController.$inject = ['$scope', 'Spectrum', 'SpectraQueryBuilderService', '$location', 'SpectrumCache', '$timeout', '$log', 'MAX_SPECTRA', 'toaster'];
    angular.module('moaClientApp')
      .controller('SpectraBrowserController', SpectraBrowserController);

    /* @ngInject */
    function SpectraBrowserController($scope, Spectrum, SpectraQueryBuilderService, $location, SpectrumCache, $timeout, $log, MAX_SPECTRA, toaster) {

        $scope.table = false;

        /**
         * contains all local objects and is our model
         * @type {Array}
         */
        $scope.spectra = [];

        /**
         * loads more spectra into the given view
         */
        var page = 0;

        /**
         * Executed query
         * @type {string}
         */
        $scope.query = '';

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
         * Handle search splash
         */
        $scope.searchSplash = false;

        var hideSplash = function() {
            $timeout(function () {
                $scope.searchSplash = false;
            }, 50)
        };


        /**
         * Start a new search
         */
        $scope.searchSpectra = function() {
            $location.path('spectra/search').search({});
        };

        /**
         * Reset the current query
         */
        $scope.resetQuery = function() {
            SpectraQueryBuilderService.prepareQuery();
            SpectraQueryBuilderService.executeQuery();
        };

        /**
         * Submits our query to the server
         */
        $scope.submitQuery = function() {
            $scope.dataAvailable = true;

            // Reset spectra
            $scope.spectraLoadLength = -1;
            $scope.spectra = [];

            $scope.calculateResultCount();

            // Load our data
            $scope.loadMoreSpectra();
        };

        /**
         * Calculates the number of results for the given query
         */
        $scope.calculateResultCount = function() {
            $scope.queryResultCount = "Loading...";

            Spectrum.searchSpectraCount({
                endpoint: 'count',
                query: $scope.query
            }, function (data) {
                $scope.queryResultCount = data.count;
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

            return '/spectra/display/' + id;
        };


        /**
         * Get total exact mass as accurate mass of spectrum
         */
        $scope.addAccurateMass = function(spectra) {
            for (var i = 0; i < spectra.length; i++) {
                var mass = '';
                var spectrum = spectra[i];

                if (angular.isDefined(spectrum.compound)) {
                    for (var j = 0, m = spectrum.compound.length; j < m; j++) {
                        var compound = spectrum.compound[j];

                        for(var k = 0, n = compound.metaData.length; k < n; k++) {
                            var meta = compound.metaData[k];
                            if(meta.name === 'total exact mass') {
                                mass = parseFloat(meta.value).toFixed(3);
                                break;
                            }
                        }

                        if(compound.kind === 'biological') {
                            break;
                        }
                    }
                }
                spectrum.accurateMass = mass;
            }

            return spectra;
        };

        $scope.loadMoreSpectra = function() {
            if (!$scope.loadingMore && $scope.spectraLoadLength !== $scope.spectra.length && $scope.dataAvailable) {
                //search utilizing our compiled query so that it can be easily refined over time
                $scope.loadingMore = true;
                $scope.spectraLoadLength = $scope.spectra.length;

                // Note the start time for timing the spectrum search
                $scope.startTime = Date.now();

                $log.debug('Submitted query: '+ $scope.query);

                if ($scope.query === '') {
                    Spectrum.searchSpectra({size: MAX_SPECTRA, page: page}, searchSuccess, searchError);
                } else {
                    Spectrum.searchSpectra({endpoint: 'search', query: $scope.query, page: page, size: MAX_SPECTRA}, searchSuccess, searchError);
                }
            }
        };

        function searchSuccess(data) {
            $scope.duration = (Date.now() - $scope.startTime) / 1000;

            if (data.length === 0) {
                $scope.dataAvailable = false;
            } else {
                // Add data to spectra object
                $scope.spectra.push.apply($scope.spectra, $scope.addAccurateMass(data));
            }

            hideSplash();
            $scope.loadingMore = false;
            page += 1;
        }

        function searchError(error) {
            hideSplash();
            $scope.loadingMore = false;
            
            toaster.pop({
                type: 'error',
                title: 'Unable to complete request',
                body: 'Please try again later.'
            });
        }

        $scope.$on('$viewContentLoaded', function() {
            $timeout(function() {
                $(window).scrollTop($scope.spectraScrollStartLocation);
            }, 1);
        });


        (function() {
            $scope.spectraScrollStartLocation = 0;
            $scope.spectra = [];

            // Display splash overlay
            $scope.searchSplash = true;

            // Handle InChIKey queries
            if($location.search().hasOwnProperty('inchikey')) {
                $log.info('Accepting InChIKey query from URL: '+ $location.search().inchikey);

                if (/^[A-Z]{14}-[A-Z]{10}-[A-Z]$/.test($location.search().inchikey)) {
                    SpectraQueryBuilderService.addCompoundMetaDataToQuery('InChIKey', $location.search().inchikey);
                } else {
                    SpectraQueryBuilderService.addCompoundMetaDataToQuery('InChIKey', $location.search().inchikey, true);
                }

                SpectraQueryBuilderService.executeQuery();
            }

            // Handle SPLASH queries
            if($location.search().hasOwnProperty('splash')) {
                $log.info('Accepting SPLASH query from URL: '+ $location.search().splash);

                SpectraQueryBuilderService.addSplashToQuery($location.search().splash);
                SpectraQueryBuilderService.executeQuery();
            }

            // Handle general queries
            if($location.search().hasOwnProperty('query')) {
                $log.info('Accepting query from URL: '+ $location.search().query);
                $scope.query = $location.search().query;
            }

            $scope.submitQuery();
        })();
    }
})();
