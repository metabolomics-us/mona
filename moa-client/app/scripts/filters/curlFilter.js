/**
 * Created by wohlgemuth on 11/6/14.
 */

app.filter('curl', function (REST_BACKEND_SERVER) {

    return function(input) {
        return 'curl -H "Content-Type: application/json" -d \'' + angular.toJson(input,false) + '\' ' +  REST_BACKEND_SERVER + '/rest/spectra/search';
    };

});
