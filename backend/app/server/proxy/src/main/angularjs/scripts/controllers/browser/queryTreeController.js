/**
 * Created by sajjan on 11/13/15.
 */

(function() {
    'use strict';
    queryTreeController.$inject = ['$scope', 'DownloadService', '$log', 'REST_BACKEND_SERVER'];
    angular.module('moaClientApp')
        .controller('QueryTreeController', queryTreeController);

    /* @ngInject */
    function queryTreeController($scope, DownloadService, $log, REST_BACKEND_SERVER) {
        $scope.executeQuery = function(node) {
            return '/spectra/browse?query='+ node.query;
        };

        $scope.downloadJSON = function(node) {
            return REST_BACKEND_SERVER +'/rest/downloads/retrieve/'+ node.jsonExport.id;
        };

        $scope.downloadMSP = function(node) {
            return REST_BACKEND_SERVER +'/rest/downloads/retrieve/'+ node.mspExport.id;
        };

        /**
         * Get predefined queries and build query tree
         */
        var getPredefinedQueries = function() {
            DownloadService.getPredefinedQueries(
                function(data) {
                    // Filter out downloads with 0 records
                    data = data.filter(function(x) { return x.queryCount > 0; });

                    // Header entry for libraries if any exist
                    if (data.some(function(x) { return x.label.indexOf('Libraries') > -1; })) {
                        data.unshift({
                            label: "Libraries",
                            query: null,
                            jsonExport: null,
                            mspExport: null
                        });
                    }

                    // Set up all nodes
                    for (var i = 0; i < data.length; i++) {
                        $scope.queries[data[i].label] = data[i];

                        var label = data[i].label.split(' - ');
                        data[i].downloadLabel = data[i].label.replace(/ /g, '_').replace(/\//g, '-');
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

                    // Sort query tree
                    $scope.queryTree.sort(function(a, b) {
                        if (a.label === "All Spectra") {
                            return -1;
                        } else if (b.label == "All Spectra") {
                            return 1;
                        } else if (a.label === "Libraries") {
                            return (b == "All Spectra" ? 1 : -1)
                        } else if (b.label === "Libraries") {
                            return (a == "All Spectra" ? 1 : -1)
                        } else {
                            return 0;
                        }
                    });
                },
                function(error) {
                    $log.error('query tree failed: ' + error);
                }
            );
        };

        var getStaticDownloads = function() {
            DownloadService.getStaticDownloads(
                function(data) {
                    $scope.static = {};

                    console.log(data);

                    data.forEach(function(x) {

                    });
                },
                function(error) {
                    $log.error('query tree failed: ' + error);
                }
            );
        };

        (function() {
            $scope.queries = {};
            $scope.queryTree = [];

            getPredefinedQueries();
            getStaticDownloads();
        })();
    }
})();
