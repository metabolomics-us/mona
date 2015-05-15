/**
 * Created by wohlgemuth on 6/28/14.
 */

/**
 * this defines all the different urls in our view
 */
app.config(function ($routeProvider) {
    $routeProvider
        //defining the root url
        .when('/', {
            templateUrl: 'views/main.html',
            controller: 'MainCtrl'
        })

        //browsing compounds
        .when('/compounds', {
            templateUrl: 'views/compounds/browse/compounds.html',
            controller: 'CompoundBrowserController'
        })

        //uploading a mass spec
        .when('/upload', {
            templateUrl: 'views/spectra/cleanSpectraData.html',
            controller: 'CleanSpectraDataController'
        })
        .when('/uploadstatus', {
            templateUrl: 'views/spectra/upload/upload.html',
            controller: 'SpectraUploadController'
        })

        //displaying a single compound
        .when('/spectra/browse', {
            templateUrl: 'views/spectra/browse/spectra.html',
            controller: 'SpectraBrowserController'
        })

        //view individual spectrum
        .when('/spectra/display/:id', {
            templateUrl: 'views/spectra/display/viewSpectrum.html',
            controller: 'ViewSpectrumController',
            resolve: moaControllers.ViewSpectrumController.loadSpectrum
        })

        //database index
        .when('/spectra/dbindex', {
            templateUrl: 'views/spectra/dbindex/dbindex.html',
            controller: 'SpectraDatabaseIndexController'
        })

        //statistics page
        .when('/statistics', {
            templateUrl: 'views/statistics/statistics.html',
            controller: 'SpectraDatabaseIndexController'
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
            templateUrl: 'views/documentation/terms.html'
        })

        //any other page is redirected to the root page
        .otherwise({
            redirectTo: '/'
        });
});
