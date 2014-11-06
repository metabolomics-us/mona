/**
 * Created by sajjan on 11/6/14.
 */
'use strict';

moaControllers.SpectraDatabaseIndexController = function($scope, $http, $location, AppCache, SpectraQueryBuilderService, REST_BACKEND_SERVER) {
    /**
     * Metadata that we wish to display
     * @type {string[]}
     */
    $scope.fields = [
        'instrument type',
        'ms type',
        'ion mode'
    ];

    /**
     * Loaded unique metadata values
     * @type {{}}
     */
    $scope.fieldData = {};


    /**
     * Query all metadata values for a given metadata name
     * @param name
     * @param callback
     */
    var queryMetadataValues = function (name, callback) {
        $http.post(REST_BACKEND_SERVER + '/rest/meta/data/search?max=100', {
            query: {
                name: name,
                value: {like: '%'},
                property: 'stringValue'
            }
        }).then(function(data) {
            callback(name, data.data);
        });
    };


    /**
     * Submit query from clicked metadata link
     */
    $scope.submitQuery = function(name, value) {
        var query = {};
        query[name] = value;
        console.log(query)

        SpectraQueryBuilderService.compileQuery(query);
        $location.path("/spectra/browse/");
    };


    /**
     * initialization and population of metadata values
     */
    (function list() {
        for(var i = 0; i < $scope.fields.length; i++) {
            queryMetadataValues($scope.fields[i], function(name, data) {
                $scope.fieldData[name] = data;
            });
        }
    })();
};

app.filter('titlecase', function() {
    return function(s) {
        s = ( s === undefined || s === null ) ? '' : s;
        return s.toString().toLowerCase().replace( /\b([a-z])/g, function(ch) {
            return ch.toUpperCase();
        });
    };
});