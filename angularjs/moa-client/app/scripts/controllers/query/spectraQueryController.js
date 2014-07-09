/**
 * Created by Sajjan on 7/5/2014.
 */
'use strict';

moaControllers.SpectraQueryController = function ($scope, $modal, MetadataService, $log) {
    // Query values
    $scope.query = {};
    $scope.query.accurateMassTolerance = 0.5;

    $scope.query.accurateMass =322;
    $scope.query.nameFilter = '1234'

    /* Get metadata categories and types */
    $scope.metadataCategories = [];
    $scope.metadata = {};


    $scope.submitQuery = function() {
        // Build individual criteria
        var criteria = [];

        Object.keys($scope.query).forEach(function(element, index, array) {
            if(element === "accurateMassTolerance")
                return;

            else if(element === "accurateMass") {
                if($scope.query.accurateMass) {
                    var min = $scope.query.accurateMass - $scope.query.accurateMassTolerance;
                    var max = $scope.query.accurateMass + $scope.query.accurateMassTolerance;
                    criteria.push({between: ["accurateMass", min, max]});
                }
            }

            else
                if($scope.query[element])
                    criteria.push({eq: [element, $scope.query[element]]});
        });


        // Build criteria query
        var criteriaQuery = criteria[0];

        for(var i = 1; i < criteria.length; i++) {
            var temp = criteriaQuery
            criteriaQuery = criteria[i];
            criteriaQuery.or = temp;
        }

        console.log(criteriaQuery);


        // Do something with it and redirect
    };


    // Get metadata values
    (function list() {
        $scope.metadataCategories = MetadataService.query(
            function (data) {
                // Query each metdata category and store the data
                data.forEach(function(element, index, array) {
                    $scope.metadata[element.name] = MetadataService.query(
                        {id: element.id, categoryController: "data"},
                        function(data) {},
                        function(error) { $log.error('failed: ' + error); }
                    );
                });
            },
            function (error) { $log.error('failed: ' + error); }
        );
    })();
};

