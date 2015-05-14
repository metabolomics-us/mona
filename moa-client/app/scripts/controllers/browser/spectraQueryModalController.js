/**
 * Created by wohlgemuth on 7/11/14.
 */
moaControllers.QuerySpectrumModalController = function ($scope, $modalInstance, SpectraQueryBuilderService) {
    /**
     * Store accordion status
     * @type {{name: boolean}}
     */
    $scope.queryAccordion = {name: true};

    /**
     * Tags selected in query window
     * @type {{}}
     */
    $scope.selectedTags = {};

    /**
     * Store all metadata query data
     * @type {{name: string, value: string}[]}
     */
    $scope.metadataQuery = [];


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
    $scope.submitQuery = function () {

        //compile initial query
        SpectraQueryBuilderService.compileQuery($scope.query);

        //refine by metadata
        for (var i = 0; i < $scope.metadataQuery.length; i++) {
            SpectraQueryBuilderService.addMetaDataToQuery($scope.metadataQuery[i]);
        }

        //add tags to query
        for (var key in $scope.selectedTags) {
            if ($scope.selectedTags.hasOwnProperty(key) ) {
                    SpectraQueryBuilderService.addTagToQuery(key,false,$scope.selectedTags[key]);
            }
        }

        //submit the final query
        $modalInstance.close(SpectraQueryBuilderService.getQuery());
    };

};


/**
 * TODO
 * FIX MULTIPLE META FIELDS ON SERVER SIDE
 */
app.filter('unique', function () {
    return function (input, key) {
        var unique = {};
        var uniqueList = [];
        if (input != null) {
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
