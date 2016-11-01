/**
 * Created by wohlgemuth on 6/28/14.
 * Defines all the different urls in our view
 */

(function() {
    'use strict';

    configure.$inject = ['$routeProvider', '$locationProvider'];
    angular.module('moaClientApp')
        .config(configure);

    /* @ngInject */
    function configure($routeProvider,$locationProvider) {

        // use HTML5 Mode
        $locationProvider.html5Mode(true);

        $routeProvider
            //defining the root url
            .when('/', {
                templateUrl: 'views/main.html',
                controller: 'MainController'
            })

            //uploading a mass spec
            .when('/upload', {
                templateUrl: 'views/spectra/upload/upload.html'
            })

            .when('/upload/basic', {
                templateUrl: 'views/spectra/upload/basicUploader.html',
                controller: 'BasicUploaderController'
            })

            .when('/upload/advanced', {
                templateUrl: 'views/spectra/upload/advancedUploader.html',
                controller: 'AdvancedUploaderController'
            })

            .when('/upload/status', {
                templateUrl: 'views/spectra/upload/uploadStatus.html',
                controller: 'SpectraUploadController'
            })

            //spectrum browser
            .when('/spectra/browse', {
                templateUrl: 'views/spectra/browse/spectra.html',
                controller: 'SpectraBrowserController'
            })

            //view individual spectrum
            .when('/spectra/display/:id', {
                templateUrl: 'views/spectra/display/viewSpectrum.html',
                controller: 'ViewSpectrumController',
                resolve: {
                    delayedSpectrum: /* @ngInject */['Spectrum', '$route', 'SpectrumCache', function(Spectrum, $route, SpectrumCache) {
                        // If a spectrum is not cached or the id requested does not match the
                        // cached spectrum, request it from the REST api
                        if (!SpectrumCache.hasSpectrum() || SpectrumCache.getSpectrum().id !== $route.current.params.id) {
                            return Spectrum.get(
                                {id: $route.current.params.id},
                                function(data) {
                                },
                                function(error) {
                                    alert('failed to obtain spectrum: ' + error);
                                }
                            ).$promise;
                        }

                        else {
                            var spectrum = SpectrumCache.getSpectrum();
                            SpectrumCache.removeSpectrum();
                            return spectrum;
                        }
                    }]
                }
            })

            .when('/spectra/splash/:splash', {
                redirectTo: '/spectra/browse'
            })

            .when('/spectra/inchikey/:inchikey', {
                redirectTo: '/spectra/browse'
            })

            //database index
            .when('/spectra/dbindex', {
                templateUrl: 'views/spectra/dbindex/dbindex.html',
                controller: 'SpectraDatabaseIndexController'
            })

            .when('/spectra/querytree', {
                redirectTo: '/downloads'
            })

            .when('/downloads', {
                templateUrl: 'views/spectra/dbindex/queryTree.html',
                controller: 'QueryTreeController'
            })

            .when('/spectra/search', {
                templateUrl: 'views/spectra/query/search.html',
                controller: 'KeywordSearchController'
            })

            //query by similarity
            .when('/spectra/similarity', {
                templateUrl: 'views/spectra/query/similarityQuery.html',
                controller: 'SpectraSimilarityQueryController'
            })

            //statistics page
            .when('/statistics', {
                templateUrl: 'views/statistics/statistics.html',
                controller: 'SpectraDatabaseIndexController'
            })

            .when('/statistics/import', {
                templateUrl: 'views/statistics/times.html',
                controller: 'StatisticsController',
                resolve: {
                    statistics: /* @ngInject */['StatisticsService', function(StatisticsService) {
                        return [
                            StatisticsService.executionTime({time: "day", method: "import", max: 100}),
                            StatisticsService.executionTime({time: "hour", method: "import", max: 100})
                        ];
                    }]
                }
            })

            .when('/statistics/validation', {
                templateUrl: 'views/statistics/times.html',
                controller: 'StatisticsController',

                resolve: {
                    statistics: /* @ngInject */['StatisticsService', function(StatisticsService) {
                        return [
                            StatisticsService.executionTime({time: "day", method: "validation", max: 100}),
                            StatisticsService.executionTime({time: "hour", method: "validation", max: 100})
                        ]
                    }]
                }
            })

            .when('/statistics/query', {
                templateUrl: 'views/statistics/times.html',
                controller: 'StatisticsController',

                resolve: {
                    statistics: /* @ngInject */['StatisticsService', function(StatisticsService) {
                        return [
                            StatisticsService.executionTime({time: "day", method: "search", max: 100}),
                            StatisticsService.executionTime({time: "hour", method: "search", max: 100})
                        ]
                    }]
                }
            })

            //user profile page
            .when('/profile', {
                templateUrl: 'views/submitters/profile.html',
                controller: 'SubmitterProfileController'
            })

            //displaying all submitters
            .when('/admin/submitters', {
                templateUrl: 'views/submitters/list.html',
                controller: 'SubmitterController'
            })

            .when('/documentation/license', {
                templateUrl: 'views/documentation/license.html'
            })

            .when('/news', {
                templateUrl: 'views/news/news.html'
            })

            .when('/documentation/query', {
                templateUrl: 'views/documentation/query.html'
            })

            .when('/documentation/terms', {
                templateUrl: 'views/documentation/terms.html',
                controller: 'DocumentationTermController'
            })

            .when('/500', {
                templateUrl: 'views/500.html',
                controller: 'DocumentationTermController'
            })

            //any other page is redirected to the root page
            .otherwise({
                redirectTo: '/'
            });
    }
})();
