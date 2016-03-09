/**
 * Created by sajjan on 11/13/15.
 */

(function() {
    'use strict';
    angular.module('moaClientApp')
        .controller('QueryTreeController', queryTreeController);

    /* @ngInject */
    function queryTreeController($scope, Spectrum, $location, $filter, $log, SpectraQueryBuilderService, REST_BACKEND_SERVER) {
        $scope.executeQuery = function(node) {
            SpectraQueryBuilderService.setQuery(JSON.parse(node.query));
            $location.path('/spectra/browse');
        };

        $scope.downloadJSON = function(node) {
            window.location.href = REST_BACKEND_SERVER +'/rest/spectra/search/download/'+ node.jsonExport.id;
        };

        $scope.downloadMSP = function(node) {
            window.location.href = REST_BACKEND_SERVER +'/rest/spectra/search/download/'+ node.mspExport.id;
        };

        (function() {
            $scope.queries = {};
            $scope.queryTree = [];

            // Get predefined queries and build query tree
            Spectrum.getPredefinedQueries(
                function(data) {
                    // Entry for libraries
                    data.unshift({
                        label: "Libraries",
                        query: null,
                        jsonExport: null,
                        mspExport: null
                    });

                    // Set up all nodes
                    for (var i = 0; i < data.length; i++) {
                        $scope.queries[data[i].label] = data[i];

                        var label = data[i].label.split(' - ');
                        data[i].depth = label.length;
                        data[i].id = i;
                        data[i].children = [];
                    }

                    // Identify node parents
                    for (var i = 0; i < data.length; i++) {
                        var label = data[i].label.split(' - ');
                        data[i].singleLabel = label.pop();
                        var parentLabel = label.join(" - ");

                        if (data[i].depth === 1) {
                            data[i].parent = -1;
                            $scope.queryTree.push(data[i]);
                        } else {
                            for (var j = 0; j < data.length; j++) {
                                if (data[j].label === parentLabel) {
                                    data[i].parent = j;
                                    data[j].children.push(data[i]);
                                    break;
                                }
                            }
                        }
                    }
                },
                function(error) {
                    $log.error('query tree failed: ' + error);
                }
            );
        })();
    }
})();
