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
        $scope.api = {};
        $scope.activeTab = [false, false, false];

        $scope.selectTab = function(idx) {
            // Set tab
            if (angular.isUndefined($scope.tabIndex) && $location.search().hasOwnProperty('tab')) {
                $scope.tabIndex = parseInt($location.search().tab);
            } else {
                $scope.tabIndex = idx;
                $location.search('tab', idx);
            }

            for (var i = 0; i < $scope.activeTab.length; i++) {
                $scope.activeTab[i] = (i == $scope.tabIndex);
            }

            // Refresh the charts
            $timeout(function() {
                window.dispatchEvent(new Event('resize'));

                for (var k in $scope.api) {
                    if ($scope.api.hasOwnProperty(k)) {
                        $scope.api[k].refresh();
                    }
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
                    mode: 'size',
                    dispatch: {
                        chartClick: function(e) {
                            var data = e.pos.target.__data__;
                            $scope.currentPage = 1;
                            $scope.activeTableData = data.children;
                            $scope.$apply();
                        }
                    }
                }
            }
        };


        /**
         * Query all metadata values for a given metadata name
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
                        var transformedData = response.data.map(function(x) {
                            return [x.name, x.spectrumCount, x.compoundCount];
                        });
                        
                        $scope.compoundClassData = {
                            'spectrum': [buildHierarchy(transformedData, 1)],
                            'compound': [buildHierarchy(transformedData, 2)]
                        };

                        $scope.changeSunburstDataMode('spectrum');
                    },
                    function(response) {}
                );
        };

        $scope.tableDataPage = 1;
        $scope.tableSort = '-spectra';

        $scope.changeSunburstDataMode = function(sunburstDataMode) {
            $scope.sunburstDataMode = sunburstDataMode;
            $scope.activeCompoundClassData = $scope.compoundClassData[$scope.sunburstDataMode];
            $scope.activeTableData = $scope.compoundClassData[$scope.sunburstDataMode][0].children;
            $scope.tableDataPage = 1;

            $timeout(function () {
                $scope.api.compoundClassChart.refresh();
            }, 50);
        };

        $scope.tableDataSort = function(key) {
            if ($scope.tableSort.substring(1) == key) {
                $scope.tableSort = ($scope.tableSort.charAt(0) == '+' ? '-' : '+') + key;
            } else {
                $scope.tableSort = '-'+ key;
            }
        };

        $scope.tableDataClick = function(node) {
            $scope.activeCompoundClassData = [node];
            $scope.activeTableData = node.children;

            $timeout(function () {
                $scope.api.compoundClassChart.refresh();
            }, 50);
        };

        $scope.tableDataExecuteQuery = function(node) {
            SpectraQueryBuilderService.prepareQuery();
            SpectraQueryBuilderService.addGeneralClassificationToQuery(node.name);
            SpectraQueryBuilderService.executeQuery();
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


        var buildHierarchy = function(csv, index) {
            var root = {
                "name": "Chemical Compounds",
                "children": []
            };

            for (var i = 0; i < csv.length; i++) {
                var sequence = csv[i][0];
                var size = csv[i][index];

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
                                "size": parseInt(csv[i][index]),
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
            $scope.selectTab(0);

            getGlobalStatistics();
            getCompoundClassStatistics();
            getMetadataValues();
        })();
    }
})();

