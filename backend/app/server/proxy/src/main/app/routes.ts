/**
 * Created by wohlgemuth on 6/28/14.
 * Defines all the different urls in our view
 */

import * as angular from 'angular';

    configure.$inject = ['$routeProvider', '$locationProvider'];
    angular.module('moaClientApp')
        .config(configure);

    /* @ngInject */
    function configure($routeProvider, $locationProvider) {
        // use HTML5 Mode
        $locationProvider.html5Mode(true);

        $routeProvider
            //defining the root url
            .when('/', {
                template: "<main></main>"
            })

            //uploading a mass spec
            .when('/upload', {
                template: "<upload-page></upload-page>"
            })

            .when('/upload/basic', {
                template: "<basic-uploader></basic-uploader>"
            })

            .when('/upload/advanced', {
                template: "<advanced-uploader></advanced-uploader>"
            })

            .when('/upload/status', {
                template: "<spectra-upload></spectra-upload>"
            })

            //spectrum browser
            .when('/spectra/browse', {
                template: "<spectra-browser></spectra-browser>"
            })

            .when('/spectra/similaritySearch', {
                template: "<spectra-browser></spectra-browser>"
            })

            //view individual spectrum
            .when('/spectra/display/:id', {
                template: "<spectrum-viewer></spectrum-viewer>",
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

            //database index and statistics
            .when('/spectra/statistics', {
                template: "<spectra-database-index></spectra-database-index>",
                reloadOnSearch: false
            })

            .when('/spectra/querytree', {
                redirectTo: '/downloads'
            })

            .when('/downloads', {
                template: "<query-tree></query-tree>"
            })

            .when('/spectra/search', {
                templateUrl: './views/spectra/query/search.html'
            })

            //user profile page
            .when('/profile', {
                template: "<submitter-profile></submitter-profile>"
            })

            //displaying all submitters
            .when('/admin/submitters', {
                template: "<submitter></submitter>"
            })

            .when('/documentation/license', {
                templateUrl: './views/documentation/license.html'
            })

            .when('/documentation/query', {
                templateUrl: './views/documentation/query.html'
            })

            .when('/documentation/terms', {
                template: "<documentation-term></documentation-term>"
            })

            .when('/500', {
                template: "<documentation-term></documentation-term>"
            })

            //any other page is redirected to the root page
            .otherwise({
                redirectTo: '/'
            });
    }
