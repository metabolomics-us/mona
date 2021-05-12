/**
 * Created by sajjan on 11/13/15.
 */
import * as angular from 'angular';

class QueryTreeController{
    private static $inject = ['$scope', 'DownloadService', '$log', 'REST_BACKEND_SERVER'];
    private $scope;
    private DownloadService;
    private $log;
    private REST_BACKEND_SERVER;
    private showEmptyDownloads;
    private queries;
    private queryTree;
    private static;

    constructor($scope, DownloadService, $log, REST_BACKED_SERVER) {
        this.$scope = $scope;
        this.DownloadService = DownloadService;
        this.$log = $log;
        this.REST_BACKEND_SERVER = REST_BACKED_SERVER;
    }

    $onInit = () => {
        this.showEmptyDownloads = false;
        this.queries = {};
        this.queryTree = [];

        this.getPredefinedQueries();
        this.getStaticDownloads();
    }

    executeQuery = (node) => {
        return '/spectra/browse?query='+ node.query;
    };

    downloadJSON = (node) => {
        return this.REST_BACKEND_SERVER +'/rest/downloads/retrieve/'+ node.jsonExport.id;
    };

    downloadMSP = (node) => {
        return this.REST_BACKEND_SERVER +'/rest/downloads/retrieve/'+ node.mspExport.id;
    };

    downloadSDF = (node) => {
        return this.REST_BACKEND_SERVER +'/rest/downloads/retrieve/'+ node.sdfExport.id;
    };

    /**
     * Get predefined queries and build query tree
     */
    getPredefinedQueries = () => {
        this.DownloadService.getPredefinedQueries(
            (data) => {
                // Header entry for libraries, which is displayed by default if any libraries are being displayed
                data.unshift({
                    label: "Libraries",
                    query: null,
                    jsonExport: null,
                    mspExport: null,
                    sdfExport: null,
                    display: data.some(function(x) { return x.label.indexOf('Libraries') > -1 && x.queryCount > 0; })
                });

                // Set up all nodes
                for (let i = 0; i < data.length; i++) {
                    this.queries[data[i].label] = data[i];

                    let label = data[i].label.split(' - ');
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
                for (let i = 0; i < data.length; i++) {
                    let label = data[i].label.split(' - ');
                    data[i].singleLabel = label.pop();
                    let parentLabel = label.join(" - ");

                    if (data[i].depth === 1) {
                        data[i].parent = -1;
                        this.queryTree.push(data[i]);
                    } else {
                        for (let j = 0; j < data.length; j++) {
                            if (data[j].label === parentLabel) {
                                data[i].parent = j;
                                data[j].children.push(data[i]);
                                break;
                            }
                        }
                    }
                }

                // Sort query tree
                this.queryTree.sort((a, b) => {
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
            (error) => {
                this.$log.error('query tree failed: ' + error);
            }
        );
    };

    getStaticDownloads = () => {
        this.static = {};

        this.DownloadService.getStaticDownloads(
            function(data) {
                data.forEach(function(x) {
                    if (angular.isDefined(x.category)) {
                        let categoryName = x.category[0].toUpperCase() + x.category.substr(1);

                        if (!this.static.hasOwnProperty(categoryName)) {
                            this.static[categoryName] = [];
                        }

                        x.path = this.REST_BACKEND_SERVER +'/rest/downloads/static/'+ x.category +'/'+ x.fileName;
                        this.static[categoryName].push(x);
                    } else {
                        if (!this.static.hasOwnProperty('General')) {
                            this.static['General'] = [];
                        }

                        x.path = this.REST_BACKEND_SERVER +'/rest/downloads/static/'+ x.fileName;
                        this.static['General'].push(x);
                    }
                });
            },
            (error) => {
                this.$log.error('query tree failed: ' + error);
            }
        );
    };

}

let QueryTreeComponent = {
    selector: "queryTree",
    templateUrl: "../../../views/spectra/dbindex/queryTree.html",
    bindings: {},
    controller: QueryTreeController
}
angular.module('moaClientApp')
    .component(QueryTreeComponent.selector, QueryTreeComponent)