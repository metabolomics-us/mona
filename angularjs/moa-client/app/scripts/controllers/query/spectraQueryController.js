/**
 * Created by Sajjan on 7/5/2014.
 */
'use strict';

moaControllers.SpectraQueryController = function ($scope, $modal, MetadataService, $log) {
    // Query values
    $scope.query = {};
    $scope.query.accurateMassTolerance = 0.5;


    $scope.submitQuery = function() {
        // Build individual criteria
        var criteria = [];

        Object.keys($scope.query).forEach(function(element, index, array) {
            if(element === "accurateMassTolerance")
                return;

            else if(element === "Mz_exact") {
                if($scope.query[element]) {
                    var min = parseFloat($scope.query[element]) - parseFloat($scope.query.Mz_exact);
                    var max = parseFloat($scope.query[element]) + parseFloat($scope.query.Mz_exact);
                    criteria.push({between: ["accurateMass", min, max]});
                }
            }

            else
                if($scope.query[element])
                    criteria.push({eq: [element, $scope.query[element]]});
        });


        // Build criteria query
        var criteriaQuery = criteria[0];
        /*
        for(var i = 1; i < criteria.length; i++) {
            var temp = criteriaQuery;
            criteriaQuery = criteria[i];
            criteriaQuery.and = temp;
        }
        */
        $scope.criteriaQuery = criteriaQuery;

        console.log($scope.metadataValues);

        // Do something with it and redirect
    };



    /* Metadata */
    $scope.metadataCategories = [];
    $scope.metadata = {};
    $scope.metadataValues = {};

    var metadataValuesQuery = function(data) {
        data.forEach(function(element, index, array) {
            if(element.type === "string") {
                var values = {};

                MetadataService.dataValues(
                    {id: element.id},
                    function (data) {
                        data.forEach(function(element, index, array) {
                            values[element.value] = true;
                        });

                        $scope.metadataValues[element.name] = [];
                        Object.keys(values).forEach(function(key, index, array) {
                            $scope.metadataValues[element.name].push({value: key});
                        });
                        console.log($scope.metadataValues[element.name]);
                    },
                    function (error) {
                        $log.error('metadata values failed: ' + error);
                    }
                );
            }
        });
    };

    var metadataQuery = function(data) {
        // Query each metdata category and store the data
        data.forEach(function(element, index, array) {
            $scope.metadata[element.name] = MetadataService.categoryData(
                {id: element.id},
                metadataValuesQuery,
                function(error) {
                    $log.error('metadata category data failed: ' + error);
                }
            );
        });
    };

    // Get metadata values
    (function list() {
        // Retrieve metadata categories
        $scope.metadataCategories = MetadataService.categories(
            metadataQuery,
            function (error) {
                $log.error('metadata categories failed: ' + error);
            }
        );
    })();
};

