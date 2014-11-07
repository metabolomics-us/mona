/**
 * Created by wohlgemuth on 11/7/14.
 */

/**
 * generates a curl link for us
 */
app.filter('spectraDownload', function (REST_BACKEND_SERVER) {

    return function(input) {
        return REST_BACKEND_SERVER + '/rest/spectra/' + input+ "?format=mona";
    };

});

/**
 * generates a curl link as msp file for us
 */
app.filter('spectraDownloadAsMsp', function (REST_BACKEND_SERVER) {

    return function(input) {
        return REST_BACKEND_SERVER + '/rest/spectra/' + input + "?format=msp";
    };

});



