/**
 * Created by sajjan on 11/6/14.
 */

(function() {
    'use strict';
    SpectraDatabaseIndexController.$inject = ['$scope', '$http', '$location', '$window', '$timeout', 'SpectraQueryBuilderService', 'REST_BACKEND_SERVER'];
    angular.module('moaClientApp')
        .controller('SpectraDatabaseIndexController', SpectraDatabaseIndexController);

    /* @ngInject */
    function SpectraDatabaseIndexController($scope, $http, $location, $window, $timeout, SpectraQueryBuilderService, REST_BACKEND_SERVER) {
        $scope.tabIndex = 0;
        $scope.api = {};

        $scope.selectTab = function(idx) {
            $scope.tabIndex = idx;

            // Refresh the charts
            $timeout(function() {
                window.dispatchEvent(new Event('resize'));

                for (var k in $scope.api) {
                    $scope.api[k].refresh();
                }
            }, 50);
        };


        // Metadata chart properties
        $scope.metadataFields = [
            //{name: 'instrument type', title: 'Instrument Type'},
            {name: 'ms level', title: 'MS Level'},
            {name: 'ionization mode', title: 'Ionization Mode'},
            {name: 'precursor type', title: 'Precursor Type'}
        ];

        $scope.selectedMetadataField = $scope.metadataFields[0];

        $scope.selectMetadataField = function(option) {
            $scope.selectedMetadataField = option;
        };

        $scope.metadataChartOptions = {
            chart: {
                type: 'pieChart',
                height: 600,
                x: function (d) {
                    return d.key;
                },
                y: function (d) {
                    return d.y;
                },
                pie: {
                    dispatch: {
                        elementClick: function (e) {
                            console.log($scope.selectedMetadataField)
                            console.log(e);
                            $scope.executeQuery($scope.selectedMetadataField.name, e.data.key);
                            $scope.$apply();
                        }
                    }
                },
                showLabels: true,
                labelsOutside: true,
                duration: 500,
                labelThreshold: 0.01,
                color: function (d, i) {
                    var colors = d3.scale.category10().range();
                    return colors[i % (colors.length - 1)];
                },
                legend: {
                    margin: {
                        top: 5,
                        right: 35,
                        bottom: 5,
                        left: 0
                    }
                }
            }
        };

        $scope.sunburstOptions = {
            chart: {
                type: "sunburstChart",
                height: 600,
                duration: 500,
                sunburst: {
                    mode: 'size'
                }
            }
        };


        /**
         * Query all metadata values for a given metadata name
         * @param id
         */
        var getMetadataValues = function() {
            $scope.metadataFields.forEach(function(field) {
                $http.get(REST_BACKEND_SERVER + '/rest/metaData/values?name='+ field.name)
                    .then(
                        function(response) {
                            field.data = [];

                            // Transform data for D3 plot
                            response.data.values.forEach(function(x) {
                                field.data.push({
                                    key: x.value,
                                    y: x.count
                                })
                            });

                            field.data.sort(function(a, b) { return b.y - a.y; });
                            field.data = field.data.slice(0, 10);
                        },
                        function(response) {}
                    );
            });
        };

        /**
         * Query for total statistics
         */
        var getGlobalStatistics = function() {
            return $http.get(REST_BACKEND_SERVER + '/rest/statistics/global')
                .then(
                    function(response) {
                        $scope.globalData = response.data;
                    },
                    function(response) {}
                );
        };

        /**
         * Query for compound class statistics
         */
        var getCompoundClassStatistics = function() {
            return $http.get(REST_BACKEND_SERVER + '/rest/statistics/compoundClasses')
                .then(
                    function(response) {
                        $scope.compoundClassData = [buildHierarchy(response.data.map(function(x) {
                            return [x.name, x.spectrumCount, x.compoundCount];
                        }))];
                    },
                    function(response) {}
                );
        };


        /**
         * Submit query from clicked metadata link
         * @param name
         * @param value
         */
        $scope.executeQuery = function(name, value) {
            SpectraQueryBuilderService.prepareQuery();
            SpectraQueryBuilderService.addMetaDataToQuery(name, value);
            SpectraQueryBuilderService.executeQuery();
        };


        var buildHierarchy = function(csv) {
            var root = {
                "name": "Chemical Compounds",
                "children": []
            };

            for (var i = 0; i < csv.length; i++) {
                var sequence = csv[i][0];
                var size = csv[i][1];

                if (isNaN(size)) { // e.g. if this is a header row
                    continue;
                }

                var parts = sequence.split("|");
                var currentNode = root;

                for (var j = 0; j < parts.length; j++) {
                    var children = currentNode["children"];
                    var nodeName = parts[j];
                    var childNode;

                    var foundChild = false;

                    for (var k = 0; k < children.length; k++) {
                        if (children[k]["name"] == nodeName) {
                            childNode = children[k];
                            foundChild = true;
                            break;
                        }
                    }

                    if (j + 1 < parts.length) {
                        // If we don't already have a child node for this branch, create it.
                        if (!foundChild) {
                            childNode = {
                                "name": nodeName,
                                "children": []
                            };

                            children.push(childNode);
                        }

                        currentNode = childNode;
                    } else {
                        // Reached the end of the sequence; create a leaf node.
                        if(!foundChild) {
                            childNode = {
                                "name": nodeName,
                                "size": parseInt(csv[i][1]),
                                "spectra": parseInt(csv[i][1]),
                                "compounds": parseInt(csv[i][2]),
                                "children": []
                            };

                            children.push(childNode);
                        } else {
                            childNode.spectra = parseInt(csv[i][1]);
                            childNode.compounds = parseInt(csv[i][2]);
                        }
                    }
                }
            }

            // Add counts to root node
            root.size = root.spectra = root.compounds = 0;

            root.children.forEach(function(x) {
                root.size += x.size;
                root.spectra += x.spectra;
                root.compounds += x.compounds;
            });

            return root;
        };



        (function() {
            getGlobalStatistics();
            getCompoundClassStatistics();
            getMetadataValues();
        })();
    }
})();

