/**
 * Created by wohlgemuth on 7/22/14.
 */
'use strict';

/**
 * simple event to rest the scroll bar to the top of the page on route changes
 */
app.run(["$rootScope", "$route", "$window", '$location', function ($rootScope, $route, $window, $location) {

    $rootScope.$on('$routeChangeStart', function (evt, absNewUrl, absOldUrl) {
        $window.scrollTo(0, 0);    //scroll to top of page after each route change
    });

    $rootScope.$on('$locationChangeSuccess', function() {
        $rootScope.actualLocation = $location.path();
    });
}]);
