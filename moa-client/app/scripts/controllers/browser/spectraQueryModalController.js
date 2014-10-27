/**
 * Created by wohlgemuth on 7/11/14.
 */
moaControllers.QuerySpectrumModalController = function ($scope, $modalInstance, SpectraQueryBuilderService, $log, $http, REST_BACKEND_SERVER, AppCache) {
    /* Metadata */
    $scope.metadataCategories = [];
    $scope.metadata = {};
    $scope.metadataValues = {};

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
        return $http.post(REST_BACKEND_SERVER + '/rest/meta/data/search?max=100', {
            query: {
                name: name,
                value: {like: '%' + value + '%'}
            }
        }).then(function (res) {
            var values = {};

            angular.forEach(res.data, function(value) {
                values[value.value] = true;
            });

            return Object.keys(values).slice(0,10);
        });
    };

    /**
     * initialization and population of default values
     */
    (function list() {
        $scope.tags = AppCache.getTags();
        $scope.metadataCategories = AppCache.getMetadataCategories();
        $scope.metadata = AppCache.getMetadata();
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