/**
 * Created by wohlgemuth on 7/11/14.
 */
moaControllers.QuerySpectrumModalController = function ($scope, $modalInstance, SpectraQueryBuilderService, $log, $http, REST_BACKEND_SERVER, AppCache) {
    /**
     *
     */
    $scope.metadata = {};

    $scope.tags = [];
    $scope.tagsSelection = [];


    /**
     * contains our build query object
     * @type {{}}
     */
    $scope.query = {};

    $scope.cancelDialog = function () {
        $modalInstance.dismiss('cancel');
    };

    /**
     * closes the dialog and finishes and builds the query
     */
    $scope.submitQuery = function(){
        var result = SpectraQueryBuilderService.compileQuery($scope.query, $scope.metadata, $scope.tagsSelection);
        $modalInstance.close(result);
    };

    /**
     * perform metadata query
     */
    $scope.queryMetadataValues = function (name, value) {
        return $http.post(REST_BACKEND_SERVER + '/rest/meta/data/search?max=10', {
            query: {
                name: name,
                value: {ilike: '%' + value + '%'},
                property: 'stringValue'
            }
        }).$promise;
    };


    /**
     * initialization and population of default values
     */
    (function list() {
        $scope.metadata = {};

        AppCache.getTags(function(data) {
            $scope.tags = data;
        });

        AppCache.getMetadata(function(data) {
            for(var i = 0; i < data.length; i++) {
                if(data[i].category.visible) {
                    var name = data[i].category.name;

                    if (!$scope.metadata.hasOwnProperty(name)) {
                        $scope.metadata[name] = [];
                    }

                    $scope.metadata[name].push(data[i]);
                }
            }
        });
    })();

};


/**
 * TODO
 * FIX MULTIPLE META FIELDS ON SERVER SIDE
 */
app.filter('unique', function() {
    return function(input, key) {
        var unique = {};
        var uniqueList = [];
        if(input != null) {
            for (var i = 0; i < input.length; i++) {
                if (typeof unique[input[i][key]] == "undefined") {
                    unique[input[i][key]] = "";
                    uniqueList.push(input[i]);
                }
            }
        }
        return uniqueList;
    };
});