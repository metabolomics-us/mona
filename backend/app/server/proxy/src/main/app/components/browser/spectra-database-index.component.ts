/**
 * Created by sajjan on 11/6/14.
 */

import * as angular from 'angular';
import * as d3 from 'd3';

class SpectraDatabaseIndexController{
    private static $inject = ['$scope', '$http', '$location', '$window', '$timeout', 'SpectraQueryBuilderService', 'REST_BACKEND_SERVER'];
    private $scope;
    private $http;
    private $location;
    private $window;
    private $timeout;
    private SpectraQueryBuilderService;
    private REST_BACKEND_SERVER;
    private api;
    private activeTab;
    private metadataFields;
    private selectedMetadataField;
    private metadataChartOptions;
    private sunburstOptions;
    private getMetadataValues;
    private getGlobalStatistics;
    private globalData;
    private getCompoundClassStatistics;
    private currentPage;
    private activeTableData;
    private compoundClassData;
    private tableDataPage;
    private tableSort;
    private buildHierarchy;
    private tabIndex;
    private sunburstDataMode;
    private activeCompoundClassData;

    constructor($scope, $http, $location, $window, $timeout, SpectraQueryBuilderService, REST_BACKEND_SERVER) {
        this.$scope = $scope;
        this.$http = $http;
        this.$location = $location;
        this.$window = $window;
        this.$timeout = $timeout;
        this.SpectraQueryBuilderService = SpectraQueryBuilderService;
        this.REST_BACKEND_SERVER = REST_BACKEND_SERVER;
    }

    $onInit = () => {
        this.api = {};
        this.activeTab = [false, false, false];
        this.tableDataPage = 1;
        this.tableSort = '-spectra';

        // Metadata chart properties
        this.metadataFields = [
            //{name: 'instrument type', title: 'Instrument Type'},
            {name: 'ms level', title: 'MS Level'},
            {name: 'ionization mode', title: 'Ionization Mode'},
            {name: 'precursor type', title: 'Precursor Type'}
        ];

        this.selectedMetadataField = this.metadataFields[0];

        this.metadataChartOptions = {
            chart: {
                type: 'pieChart',
                height: 600,
                x:  (d) => {
                    return d.key;
                },
                y:  (d) =>{
                    return d.y;
                },
                pie: {
                    dispatch: {
                        elementClick: (e) => {
                            this.executeQuery(this.selectedMetadataField.name, e.data.key);
                            this.$scope.$apply();
                        }
                    }
                },
                showLabels: true,
                labelsOutside: true,
                duration: 500,
                labelThreshold: 0.01,
                color: (d, i) => {
                    let colors = d3.scale.category10().range();
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

        this.sunburstOptions = {
            chart: {
                type: "sunburstChart",
                height: 600,
                duration: 500,
                sunburst: {
                    mode: 'size',
                    dispatch: {
                        chartClick:(e) => {
                            let data = e.pos.target.__data__;
                            this.currentPage = 1;
                            this.activeTableData = data.children;
                            this.$scope.$apply();
                        }
                    }
                }
            }
        };

        /**
         * Query all metadata values for a given metadata name
         */
        this.getMetadataValues = () => {
            this.metadataFields.forEach((field) => {
                this.$http.get(this.REST_BACKEND_SERVER + '/rest/metaData/values?name='+ field.name)
                    .then(
                        (response) => {
                            field.data = [];

                            // Transform data for D3 plot
                            response.data.values.forEach((x) => {
                                field.data.push({
                                    key: x.value,
                                    y: x.count
                                })
                            });

                            field.data.sort((a, b) => { return b.y - a.y; });
                            field.data = field.data.slice(0, 10);
                        },
                        (response) => {}
                    );
            });
        };

        /**
         * Query for total statistics
         */
        this.getGlobalStatistics = () => {
            return this.$http.get(this.REST_BACKEND_SERVER + '/rest/statistics/global')
                .then(
                    (response) => {
                        this.globalData = response.data;
                    },
                    (response) => {
                    }
                );
        };

        /**
         * Query for compound class statistics
         */
        this.getCompoundClassStatistics = () => {
            return this.$http.get(this.REST_BACKEND_SERVER + '/rest/statistics/compoundClasses')
                .then(
                    (response) => {
                        let transformedData = response.data.map((x) => {
                            return [x.name, x.spectrumCount, x.compoundCount];
                        });

                        this.compoundClassData = {
                            'spectrum': [this.buildHierarchy(transformedData, 1)],
                            'compound': [this.buildHierarchy(transformedData, 2)]
                        };

                        this.changeSunburstDataMode('spectrum');
                    },
                    (response) => {}
                );
        };

        this.buildHierarchy = (csv, index) => {
            let root = {
                "name": "Chemical Compounds",
                "children": [],
                "size": 0,
                "spectra": 0,
                "compounds": 0
            };

            for (let i = 0; i < csv.length; i++) {
                let sequence = csv[i][0];
                let size = csv[i][index];

                if (isNaN(size)) { // e.g. if this is a header row
                    continue;
                }

                let parts = sequence.split("|");
                let currentNode = root;

                for (let j = 0; j < parts.length; j++) {
                    let children = currentNode["children"];
                    let nodeName = parts[j];
                    let childNode;

                    let foundChild = false;

                    for (let k = 0; k < children.length; k++) {
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

            root.children.forEach((x) => {
                root.size += x.size;
                root.spectra += x.spectra;
                root.compounds += x.compounds;
            });

            return root;
        };

        this.selectTab(0);

        this.getGlobalStatistics();
        this.getCompoundClassStatistics();
        this.getMetadataValues();
    }

    selectTab = (idx) => {
        // Set tab
        if (angular.isUndefined(this.tabIndex) && this.$location.search().hasOwnProperty('tab')) {
            this.tabIndex = parseInt(this.$location.search().tab);
        } else {
            this.tabIndex = idx;
            this.$location.search('tab', idx);
        }

        for (let i = 0; i < this.activeTab.length; i++) {
            this.activeTab[i] = (i == this.tabIndex);
        }

        // Refresh the charts
        this.$timeout(() => {
            window.dispatchEvent(new Event('resize'));

            for (let k in this.api) {
                if (this.api.hasOwnProperty(k)) {
                    this.api[k].refresh();
                }
            }
        }, 50);
    };




    selectMetadataField = (option) => {
        this.selectedMetadataField = option;
    };






    changeSunburstDataMode = (sunburstDataMode) => {
        this.sunburstDataMode = sunburstDataMode;
        this.activeCompoundClassData = this.compoundClassData[this.sunburstDataMode];
        this.activeTableData = this.compoundClassData[this.sunburstDataMode][0].children;
        this.tableDataPage = 1;

        this.$timeout( () => {
            this.api.compoundClassChart.refresh();
        }, 50);
    };

    tableDataSort = (key) => {
        if (this.tableSort.substring(1) == key) {
            this.tableSort = (this.tableSort.charAt(0) == '+' ? '-' : '+') + key;
        } else {
            this.tableSort = '-'+ key;
        }
    };

    tableDataClick = (node) => {
        this.activeCompoundClassData = [node];
        this.activeTableData = node.children;

        this.$timeout(() => {
            this.api.compoundClassChart.refresh();
        }, 50);
    };

    tableDataExecuteQuery = (node) => {
        this.SpectraQueryBuilderService.prepareQuery();
        this.SpectraQueryBuilderService.addGeneralClassificationToQuery(node.name);
        this.SpectraQueryBuilderService.executeQuery();
    };


    /**
     * Submit query from clicked metadata link
     * @param name
     * @param value
     */
    executeQuery = (name, value) => {
        this.SpectraQueryBuilderService.prepareQuery();
        this.SpectraQueryBuilderService.addMetaDataToQuery(name, value);
        this.SpectraQueryBuilderService.executeQuery();
    };

}

let SpectraDatabaseIndexComponent = {
    selector: "spectraDatabaseIndex",
    templateUrl: "../../views/spectra/dbindex/dbindex.html",
    bindings: {},
    controller: SpectraDatabaseIndexController
}

angular.module('moaClientApp')
        .component(SpectraDatabaseIndexComponent.selector, SpectraDatabaseIndexComponent);



