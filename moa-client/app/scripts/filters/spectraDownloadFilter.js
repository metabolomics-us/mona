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

/**
 * as mol file
 */
app.filter('compoundDownloadAsMolFile', function( REST_BACKEND_SERVER){
  return function(input) {
    return REST_BACKEND_SERVER + '/rest/compounds/' + input + "?format=mol";
  };
});

/**
 * as mol file
 */
app.filter('compoundDownloadAsSDFFile', function( REST_BACKEND_SERVER){
  return function(input) {
    return REST_BACKEND_SERVER + '/rest/compounds/' + input + "?format=sdf";
  };
});

/**
 * as mona record file
 */
app.filter('compoundDownloadFile', function( REST_BACKEND_SERVER){
  return function(input) {
    return REST_BACKEND_SERVER + '/rest/compounds/' + input + "?format=mona";
  };
});


