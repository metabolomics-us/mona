/**
 * Created by wohlgemuth on 7/11/14.
 */
moaControllers.QuerySpectrumModalController = function ($scope, $modalInstance,SpectraQueryBuilderService, MetadataService, $log) {


    /* Metadata */
    $scope.metadataCategories = [];
    $scope.metadata = {};
    $scope.metadataValues = {};


    /**
     * contains our build query object
     * @type {{}}
     */
    $scope.query = {};

    $scope.cancelDialog = function () {
        $modalInstance.dismiss('cancel');
    };


    /**
     * load categories
     */
    $scope.loadCategories = function () {
        $scope.metadataCategories = MetadataService.categories(
            metadataQuery,
            function (error) {
                $log.error('metadata categories failed: ' + error);
            }
        );
    };
    /**
     * initialization and population of default values
     */
    (function list() {
        $scope.loadCategories();
    })();


    /**
     * builds our metadata values for the given query
     * @param data
     */
    var metadataValuesQuery = function (data) {
        data.forEach(function (element, index, array) {
            if (element.type === "string") {
                var values = {};

                MetadataService.dataValues(
                    {id: element.id},
                    function (data) {
                        data.forEach(function (element, index, array) {
                            values[element.value] = true;
                        });

                        $scope.metadataValues[element.name] = [];
                        Object.keys(values).forEach(function (key, index, array) {
                            $scope.metadataValues[element.name].push({value: key});
                        });
                    },
                    function (error) {
                        $log.error('metadata values failed: ' + error);
                    }
                );
            }
        });
    };

    var metadataQuery = function (data) {
        // Query each metdata category and store the data
        data.forEach(function (element, index, array) {
            $scope.metadata[element.name] = MetadataService.categoryData(
                {id: element.id},
                metadataValuesQuery,
                function (error) {
                    $log.error('metadata category data failed: ' + error);
                }
            );
        });
    };

};