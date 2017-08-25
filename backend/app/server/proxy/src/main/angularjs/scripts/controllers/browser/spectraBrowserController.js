/**
 * Created by sajjan on 6/11/14.
 *
 * This controller is handling the browsing of spectra
 */

(function() {
    'use strict';
    SpectraBrowserController.$inject = ['$scope', 'Spectrum', 'SpectraQueryBuilderService', '$location', 'SpectrumCache', '$timeout', '$log', 'toaster', 'Analytics'];
    angular.module('moaClientApp')
      .controller('SpectraBrowserController', SpectraBrowserController);

    /* @ngInject */
    function SpectraBrowserController($scope, Spectrum, SpectraQueryBuilderService, $location, SpectrumCache, $timeout, $log, toaster, Analytics) {

        $scope.table = false;

        /**
         * contains all local objects and is our model
         * @type {Array}
         */
        $scope.spectra = [];

        /**
         * loads more spectra into the given view
         */
        $scope.pagination = {
            currentPage: 1,
            itemsPerPage: 10,
            maxSize: 10,
            totalSize: -1,
            loading: true
        };

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
            $scope.pagination.currentPage = 1;
            $scope.calculateResultCount();
            $scope.loadSpectra();
        };

        /**
         * Submits similarity query to the server
         */
        $scope.submitSimilarityQuery = function() {
            $scope.startTime = Date.now();

            Spectrum.searchSimilarSpectra(
                SpectraQueryBuilderService.getSimilarityQuery(),
                function(data) {
                    searchSuccess(data);
                    $scope.pagination.itemsPerPage = data.length;
                    $scope.pagination.totalSize = data.length;
                },
                searchError
            );
        };

        /**
         * Calculates the number of results for the given query
         */
        $scope.calculateResultCount = function() {
            Spectrum.searchSpectraCount({
                endpoint: 'count',
                query: $scope.query,
                text: $scope.textQuery
            }, function (data) {
                $scope.pagination.totalSize = data.count;
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


        /**
         * Execute query
         */
        $scope.loadPage = function() {
            $location.search('page', $scope.pagination.currentPage);
        };

        $scope.loadSpectra = function() {
            //search utilizing our compiled query so that it can be easily refined over time
            $scope.pagination.loading = true;
            $scope.spectra = [];

            $(window).scrollTop(0);

            // Note the start time for timing the spectrum search
            $scope.startTime = Date.now();

            var currentPage = $scope.pagination.currentPage - 1;

            $log.debug('Submitted query (page '+ currentPage +'): '+ $scope.query);

            // Log query with google analytics
            Analytics.trackEvent('query', 'execute', $scope.query, currentPage);

            if ($scope.query === undefined && $scope.textQuery === undefined) {
                Spectrum.searchSpectra({size: $scope.pagination.itemsPerPage, page: currentPage}, searchSuccess, searchError);
            } else {
                Spectrum.searchSpectra({endpoint: 'search', query: $scope.query, text: $scope.textQuery, page: currentPage, size: $scope.pagination.itemsPerPage}, searchSuccess, searchError);
            }
        };

        function searchSuccess(data) {
            $scope.duration = (Date.now() - $scope.startTime) / 1000;

            if (data.length > 0) {
                // Add data to spectra object
                $scope.spectra = $scope.addAccurateMass(data);
            }

            hideSplash();
            $scope.pagination.loading = false;
        }

        function searchError(error) {
            hideSplash();
            $scope.pagination.loading = false;

            toaster.pop({
                type: 'error',
                title: 'Unable to complete request',
                body: 'Please try again later.'
            });
        }


        /**
         * Query dropdown and editor
         */
        $scope.editQuery = false;

        $scope.updateQuery = function(query) {
            $location.search('query', query);
        };


        (function() {
            $scope.spectra = [];

            // Display splash overlay
            $scope.searchSplash = true;

            // Handle similarity search
            if ($location.path() === '/spectra/similaritySearch') {
                $log.debug('Executing similarity search...');
                $scope.pagination.loading = true;

                if (SpectraQueryBuilderService.hasSimilarityQuery()) {
                    $scope.submitSimilarityQuery();
                } else {
                    $location.path('/spectra/search').search({type: 'similarity'});
                }
            }

            // Handle all other queries
            else {
                $log.debug('Executing spectrum query...');

                // Handle InChIKey queries
                if ($location.search().hasOwnProperty('inchikey')) {
                    $log.info('Accepting InChIKey query from URL: ' + $location.search().inchikey);

                    if (/^[A-Z]{14}-[A-Z]{10}-[A-Z]$/.test($location.search().inchikey)) {
                        SpectraQueryBuilderService.addCompoundMetaDataToQuery('InChIKey', $location.search().inchikey);
                    } else {
                        SpectraQueryBuilderService.addCompoundMetaDataToQuery('InChIKey', $location.search().inchikey, true);
                    }

                    SpectraQueryBuilderService.executeQuery();
                }

                // Handle SPLASH queries
                if ($location.search().hasOwnProperty('splash')) {
                    $log.info('Accepting SPLASH query from URL: ' + $location.search().splash);

                    SpectraQueryBuilderService.addSplashToQuery($location.search().splash);
                    SpectraQueryBuilderService.executeQuery();
                }
                
                // Handle general queries
                if ($location.search().hasOwnProperty('query') || $location.search().hasOwnProperty('text')) {
                    $log.info('Accepting RSQL query from URL: "' + $location.search().query + '", and text search: "'+ $location.search().text + '"');

                    $scope.query = $location.search().query;
                    $scope.textQuery = $location.search().text;
                }

                // Handle page number
                if ($location.search().hasOwnProperty('page')) {
                    var page = parseInt($location.search().page);

                    if (!Number.isNaN(page)) {
                        $log.debug('Setting current page to '+ $location.search().page);
                        $scope.pagination.currentPage = page;
                    }
                }

                $scope.submitQuery();
            }
        })();
    }
})();
