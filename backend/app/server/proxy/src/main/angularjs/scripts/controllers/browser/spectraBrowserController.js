/**
 * Created by sajjan on 6/11/14.
 *
 * This controller is handling the browsing of spectra
 */

(function() {
    'use strict';
    SpectraBrowserController.$inject = ['$scope', 'Spectrum', 'SpectraQueryBuilderService', '$location', 'SpectrumCache', 'MetadataService', '$timeout', '$log', 'toaster', 'Analytics'];
    angular.module('moaClientApp')
      .controller('SpectraBrowserController', SpectraBrowserController);

    /* @ngInject */
    function SpectraBrowserController($scope, Spectrum, SpectraQueryBuilderService, $location, SpectrumCache, MetadataService, $timeout, $log, toaster, Analytics) {

        /**
         * contains all local objects and is our model
         * @type {Array}
         */
        $scope.spectra = [];

        /**
         * loads more spectra into the given view
         */
        $scope.pagination = {
            loading: true,

            // Pagination parameters
            currentPage: 1,
            itemsPerPage: 10,
            maxSize: 10,
            totalSize: -1,

            // Page size parameters
            itemsPerPageSelection: '10',
            itemsPerPageOptions: [10, 25, 50],

            // Table view parameters
            table: false,
            tableColumnOptions: ["ID", "Name", "Structure", "Mass Spectrum", "Accurate Mass"],
            tableColumnSelected: ["ID", "Name", "Structure", "Mass Spectrum", "Accurate Mass"]
        };
        

        /**
         * Handle search splash
         */
        $scope.searchSplash = true;

        var hideSplash = function() {
            $timeout(function () {
                $scope.searchSplash = false;
            }, 50)
        };


        /**
         * Query dropdown and editor
         */
        $scope.editQuery = false;

        $scope.updateQuery = function(query) {
            $location.search('query', query);
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
        var addMetadataMap = function(spectra) {
            for (var i = 0; i < spectra.length; i++) {
                var metaDataMap = {};

                if (angular.isDefined(spectra[i].compound)) {
                    for (var j = 0; j < spectra[i].compound.length; j++) {
                        spectra[i].compound[j].metaData.forEach(function(metaData) {
                            if(metaData.name === 'total exact mass') {
                                metaDataMap[metaData.name] = parseFloat(metaData.value).toFixed(4);
                            } else {
                                metaDataMap[metaData.name] = metaData.value;
                            }
                        });

                        if(spectra[i].compound.kind === 'biological')
                            break;
                    }
                }

                spectra[i].metaData.forEach(function(metaData) {
                    metaDataMap[metaData.name] = metaData.value;
                });

                spectra[i].metaDataMap = metaDataMap;
            }

            return spectra
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
                $scope.spectra = addMetadataMap(data);
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


        (function() {
            // Watch itemsPerPage pagination option
            $scope.$watch('pagination.itemsPerPageSelection', function () {
                var size = parseInt($scope.pagination.itemsPerPageSelection);

                if (!Number.isNaN(size)) {
                    $log.info('Updating search to use page size to '+ size);
                    $location.search('size', size);
                }
            });

            // Get unique metadata values for dropdown
            MetadataService.metaDataNames({},
                function (data) {
                    data.sort(function(a, b) {
                        return parseInt(b.count) - parseInt(a.count);
                    }).filter(function(x) {
                        return x.name !== 'Last Auto-Curation';
                    }).map(function(x) {
                        $scope.pagination.tableColumnOptions.push(x.name);
                    })
                }
            );

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

                // Handle page size
                if ($location.search().hasOwnProperty('size')) {
                    var size = parseInt($location.search().size);

                    if (!Number.isNaN(page)) {
                        $log.debug('Setting current page size to '+ $location.search().size);
                        $scope.pagination.itemsPerPage = size;
                        $scope.pagination.itemsPerPageSelection = size.toString();

                        if ($scope.pagination.itemsPerPageOptions.indexOf(size) == -1) {
                            $scope.pagination.itemsPerPageOptions.push(size);
                            $scope.pagination.itemsPerPageOptions.sort(function(a, b) {
                                return parseInt(a) - parseInt(b);
                            });
                        }
                    }
                }

                $scope.submitQuery();
            }
        })();
    }
})();
