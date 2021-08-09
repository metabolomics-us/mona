/**
 * Created by sajjan on 11/6/14.
 */

import {HttpClient} from "@angular/common/http";
import {Location} from "@angular/common";
import {SpectraQueryBuilderService} from "../../services/query/spectra-query-builder.service";
import {environment} from "../../../environments/environment";
import {Component, OnInit} from "@angular/core";
import {ActivatedRoute, ParamMap, Router} from "@angular/router";
import {first, map} from "rxjs/operators";
import * as d3 from 'd3';
import 'nvd3';

@Component({
    selector: 'spectra-database-index',
    templateUrl: '../../views/spectra/dbindex/dbindex.html'
})
export class SpectraDatabaseIndexComponent implements OnInit {
    public api;
    public metadataFields;
    public selectedMetadataField;
    public metadataChartOptions;
    public sunburstOptions;
    public getMetadataValues;
    public getGlobalStatistics;
    public globalData;
    public getCompoundClassStatistics;
    public currentPage;
    public activeTableData;
    public compoundClassData;
    public tableDataPage;
    public tableSort;
    public buildHierarchy;
    public sunburstDataMode;
    public activeCompoundClassData;
    public tabParam;

    constructor( public http: HttpClient,  public location: Location,  public spectraQueryBuilderService: SpectraQueryBuilderService,
                 public route: ActivatedRoute,  public router: Router) {

    }

    ngOnInit() {
        this.tabParam = undefined;
        this.route.queryParamMap.pipe(
            map((params: ParamMap) => {
                this.tabParam = params.get('tab');
            })
        );
        this.api = {};
        //this.activeTab = [false, false, false];
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
                this.http.get(`${environment.REST_BACKEND_SERVER}/rest/metaData/values?name=${field.name}`)
                    .pipe(first())
                    .subscribe(
                        (response: any) => {
                            field.data = [];

                            // Transform data for D3 plot
                            response.values.forEach((x) => {
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
            return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/statistics/global`)
                .pipe(first())
                .subscribe(
                    (response: any) => {
                        this.globalData = response;
                    },
                    (response) => {
                    }
                );
        };

        /**
         * Query for compound class statistics
         */
        this.getCompoundClassStatistics = () => {
            return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/statistics/compoundClasses`)
                .pipe(first())
                .subscribe(
                    (response: any) => {
                        let transformedData = response.map((x) => {
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

        //this.selectTab(0);

        this.getGlobalStatistics();
        this.getCompoundClassStatistics();
        this.getMetadataValues();
    }

    /*selectTab = (idx) => {
        // Set tab
        if (typeof this.tabIndex === 'undefined' && typeof this.tabParam !== 'undefined') {
            this.tabIndex = parseInt(this.tabParam);
        } else {
            this.tabIndex = idx;
            this.router.navigate(['/spectra/statistics', {tab: this.tabIndex}]).then();
        }

        for (let i = 0; i < this.activeTab.length; i++) {
            this.activeTab[i] = (i == this.tabIndex);
        }

        // Refresh the charts
        setTimeout(() => {
            window.dispatchEvent(new Event('resize'));

            for (let k in this.api) {
                if (this.api.hasOwnProperty(k)) {
                    this.api[k].refresh();
                }
            }
        }, 50);
    }; */




    selectMetadataField = (option) => {
        this.selectedMetadataField = option;
    };






    changeSunburstDataMode = (sunburstDataMode) => {
        this.sunburstDataMode = sunburstDataMode;
        this.activeCompoundClassData = this.compoundClassData[this.sunburstDataMode];
        this.activeTableData = this.compoundClassData[this.sunburstDataMode][0].children;
        this.tableDataPage = 1;

        setTimeout( () => {
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

        setTimeout(() => {
            this.api.compoundClassChart.refresh();
        }, 50);
    };

    tableDataExecuteQuery = (node) => {
        this.spectraQueryBuilderService.prepareQuery();
        this.spectraQueryBuilderService.addGeneralClassificationToQuery(node.name);
        this.spectraQueryBuilderService.executeQuery();
    };


    /**
     * Submit query from clicked metadata link
     * @param name
     * @param value
     */
    executeQuery = (name, value) => {
        this.spectraQueryBuilderService.prepareQuery();
        this.spectraQueryBuilderService.addMetaDataToQuery(name, value, undefined);
        this.spectraQueryBuilderService.executeQuery();
    };

}
