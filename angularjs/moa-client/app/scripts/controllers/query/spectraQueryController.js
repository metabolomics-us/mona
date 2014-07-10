/**
 * Created by Sajjan on 7/5/2014.
 */
'use strict';

moaControllers.SpectraQueryController = function ($scope, $modal, MetadataService, $log) {
    // Query values
    $scope.query = {};



    $scope.submitQuery = function() {
        // Build individual criteria
        var compound = {};
        var metaData = [];


        // Get all metadata in a single dictionary
        var meta = {};
        Object.keys($scope.metadata).forEach(function(element, index, array) {
            for(var i = 0; i < $scope.metadata[element].length; i++)
            meta[$scope.metadata[element][i].name] = $scope.metadata[element][i];
        });


        Object.keys($scope.query).forEach(function(element, index, array) {
            if(element === "nameFilter" && $scope.query[element])
                compound.name = {like: $scope.query[element]};

            else if(element === "inchiFilter" && $scope.query[element]) {
                if(/^([A-Z]{14}-[A-Z]{10}-[A-Z,0-9])+$/.test($scope.query[element]))
                    compound.inchiKey = {eq: $scope.query[element]};
                else
                    compound.inchiKey = {like: '%'+ $scope.query[element] +'%'};
            }

            // Ignore tolerance values
            else if(element.indexOf("_tolerance", element.length - 10) !== -1)
                return;

            else {
                if($scope.query[element]) {
                    if (meta[element].type === "double") {
                        if((element +"_tolerance") in $scope.query && $scope.query[element +"_tolerance"]) {
                            var min = parseFloat($scope.query[element]) - parseFloat($scope.query[element +"_tolerance"]);
                            var max = parseFloat($scope.query[element]) + parseFloat($scope.query[element +"_tolerance"]);
                            metaData.push({name: element, value: {between: [min, max]}});
                        } else
                            metaData.push({name: element, value: {eq: parseFloat($scope.query[element])}});
                    } else
                        metaData.push({name: element, value: {eq: $scope.query[element]}});
                }
            }
        });

        // Build query
        var query = {};

        if(compound)
            query.compound = compound;
        if(metaData)
            query.metadata = metaData;

        $scope.compiledQuery = query;

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

