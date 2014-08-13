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

        //displaying all submitters
        .when('/submitters', {
            templateUrl: 'views/submitters/list.html',
            controller: 'SubmitterController'
        })

        //uploading a massspec
        .when('/spectra/upload/single', {
            templateUrl: 'views/spectra/upload/single.html',
            controller: 'SpectraController'
        })
        .when('/spectra/upload/library', {
            templateUrl: 'views/spectra/upload/library.html',
            controller: 'SpectraController'
        })
        .when('/spectra/upload/moa', {
            templateUrl: 'views/spectra/upload/moa.html',
            controller: 'SpectraController'
        })

        //displaying a single compound
        .when('/spectra/browse/:inchikey?', {
            templateUrl: 'views/spectra/browse/spectra.html',
            controller: 'SpectraBrowserController'
        })

        //view individual spectrum
        .when('/spectra/display/:id', {
            templateUrl: 'views/spectra/display/viewSpectrum.html',
            controller: 'ViewSpectrumController',
            resolve: moaControllers.ViewSpectrumController.loadSpectrum
        })

        //query spectra
        .when('/spectra/query', {
            templateUrl: 'views/spectra/query/query.html',
            controller: 'SpectraBrowserController'
        })

        //any other page is redirected to the root page
        .otherwise({
            redirectTo: '/'
        });
});
