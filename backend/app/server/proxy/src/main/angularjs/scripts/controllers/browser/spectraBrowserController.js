/**
 * Created by sajjan on 6/11/14.
 * /**
 * this controller is handling the browsing of compounds in the moa-client application
 *
 * @param $scope
 * @param Spectrum
 * @param SpectraQueryBuilderService
 * @param $location
 * @param SpectrumCache
 * @param $rootScope
 * @param $timeout
 * @constructor
 */


(function() {
    'use strict';
    SpectraBrowserController.$inject = ['$scope', 'Spectrum', 'SpectraQueryBuilderService', '$location', 'SpectrumCache', '$rootScope', '$timeout', '$log', 'MAX_SPECTRA', 'toaster'];
    angular.module('moaClientApp')
      .controller('SpectraBrowserController', SpectraBrowserController);

    /* @ngInject */
    function SpectraBrowserController($scope, Spectrum, SpectraQueryBuilderService, $location,
                                      SpectrumCache, $rootScope, $timeout, $log, MAX_SPECTRA, toaster) {

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

        function hideSplash() {
            $timeout(function () {
                $scope.searchSplash = false;
            }, 100)
        }

        function showSplash() {
            $scope.searchSplash = true;
        }

        /**
         * Reset the current query
         */
        $scope.resetQuery = function() {
            SpectraQueryBuilderService.prepareQuery();
            SpectraQueryBuilderService.executeQuery();
        };

        /**
         * submits our build query to the backend
         */
        $scope.submitQuery = function() {

            $scope.dataAvailable = true;

            // Reset spectra
            $scope.spectraLoadLength = -1;
            $scope.spectra = [];

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

        $scope.initSearch = function() {
            $location.path('spectra/search').search({});
        };

        /**
         * calculates how my results this current query will return
         */
        $scope.calculateResultCount = function() {
            //reports the count for the complete query response

            $scope.queryResultCount = "Loading...";

            var queryString = SpectraQueryBuilderService.getRSQLQuery();

            if(queryString === '/rest/spectra') {
                Spectrum.searchSpectraCount({endpoint: 'count'}, function(data) {
                    $scope.queryResultCount = data.count;
                });
            }
            else {
                Spectrum.searchSpectraCount({
                    endpoint: 'count',
                    query: queryString
                }, function (data) {
                    $scope.queryResultCount = data.count;
                });
            }

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
         * Get natural mass as accurate mass of spectrum
         */
        $scope.addAccurateMass = function(spectra) {

            for (var i = 0, l = spectra.length; i < l; i++) {
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
         * loads more spectra into the given view
         */
        var page = 0;

        $scope.loadMoreSpectra = function() {
            if (!$scope.loadingMore && $scope.spectraLoadLength !== $scope.spectra.length && $scope.dataAvailable) {
                //search utilizing our compiled query so that it can be easily refined over time
                $scope.loadingMore = true;
                $scope.spectraLoadLength = $scope.spectra.length;

                var payload = SpectraQueryBuilderService.getRSQLQuery();
                // Note the start time for timing the spectrum search
                $scope.startTime = Date.now();

                $log.debug('Submitted query: '+ payload);

                if (payload === '') {
                    Spectrum.searchSpectra({size: MAX_SPECTRA, page: page}, searchSuccess, searchError);
                } else {
                    Spectrum.searchSpectra({endpoint: 'search', query: payload, page: page, size: MAX_SPECTRA}, searchSuccess, searchError);
                }
            }
            //inform other controllers that we finished loading spectra
            if ($scope.spectra) {
                $rootScope.$broadcast('spectra:loaded', $scope.spectra);
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

        function searchError(err) {
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


        /**
         * our list view and default view
         */
        (function() {
            $scope.spectraScrollStartLocation = 0;
            $scope.spectra = [];
            showSplash();

            // Handle queries passed in the URL
            if($location.search().hasOwnProperty('inchikey')) {
                $log.info('Accepting InChIKey query from URL: '+ $location.search().inchikey);

                if (/^[A-Z]{14}-[A-Z]{10}-[A-Z]$/.test($location.search().inchikey)) {
                    SpectraQueryBuilderService.setQueryString("compound.metaData=q='name==\"InChIKey\" and value==\""+ $location.search().inchikey +"\"'");
                } else {
                    SpectraQueryBuilderService.setQueryString("compound.metaData=q='name==\"InChIKey\" and value=match=\""+ $location.search().inchikey +"-.*\"'");
                }
            }

            if($location.search().hasOwnProperty('splash')) {
                $log.info('Accepting SPLASH query from URL: '+ $location.search().splash);

                SpectraQueryBuilderService.setQueryString("splash.splash=="+ $location.search().splash);
            }


            if($location.search().hasOwnProperty('query')) {
                $log.info('Accepting query from URL: '+ $location.search().query);
                
                SpectraQueryBuilderService.setQueryString($location.search().query);
            }

            // Submit our initial query
            $scope.submitQuery();
        })();
    }
})();
