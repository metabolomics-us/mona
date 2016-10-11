/**
 * Created by sajjan on 11/13/15.
 */

(function() {
    'use strict';
    queryTreeController.$inject = ['$scope', 'Spectrum', '$location', '$filter', '$log', 'SpectraQueryBuilderService', 'REST_BACKEND_SERVER'];
    angular.module('moaClientApp')
        .controller('QueryTreeController', queryTreeController);

    /* @ngInject */
    function queryTreeController($scope, Spectrum, $location, $filter, $log, SpectraQueryBuilderService, REST_BACKEND_SERVER) {
        $scope.executeQuery = function(node) {
            return '/spectra/browse?query='+ node.query;
        };

        $scope.downloadJSON = function(node) {
            return REST_BACKEND_SERVER +'/rest/downloads/retrieve/'+ node.jsonExport.id;
        };

        $scope.downloadMSP = function(node) {
            return REST_BACKEND_SERVER +'/rest/downloads/retrieve/'+ node.mspExport.id;
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
