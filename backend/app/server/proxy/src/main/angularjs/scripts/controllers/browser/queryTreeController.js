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
        $scope.showEmptyDownloads = false;

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
                    // Header entry for libraries, which is displayed by default if any libraries are being displayed
                    data.unshift({
                        label: "Libraries",
                        query: null,
                        jsonExport: null,
                        mspExport: null,
                        display: data.some(function(x) { return x.label.indexOf('Libraries') > -1 && x.queryCount > 0; })
                    });

                    // Set up all nodes
                    for (var i = 0; i < data.length; i++) {
                        $scope.queries[data[i].label] = data[i];

                        var label = data[i].label.split(' - ');
                        data[i].downloadLabel = data[i].label.replace(/ /g, '_').replace(/\//g, '-');
                        data[i].depth = label.length;
                        data[i].id = i;
                        data[i].children = [];

                        // Hide downloads with 0 records
                        if (i > 0) {
                            data[i].display = (data[i].queryCount > 0);
                        }
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
            $scope.static = {};

            DownloadService.getStaticDownloads(
                function(data) {
                    console.log(data);

                    data.forEach(function(x) {
                        if (angular.isDefined(x.category)) {
                            var categoryName = x.category[0].toUpperCase() + x.category.substr(1);

                            if (!$scope.static.hasOwnProperty(categoryName)) {
                                $scope.static[categoryName] = [];
                            }

                            x.path = REST_BACKEND_SERVER +'/rest/downloads/static/'+ x.category +'/'+ x.fileName;
                            $scope.static[categoryName].push(x);
                        } else {
                            if (!$scope.static.hasOwnProperty(categoryName)) {
                                $scope.static['General'] = [];
                            }

                            x.path = REST_BACKEND_SERVER +'/rest/downloads/static/'+ x.fileName;
                            $scope.static['General'].push(x);
                        }
                    });

                    console.log($scope.static)
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
