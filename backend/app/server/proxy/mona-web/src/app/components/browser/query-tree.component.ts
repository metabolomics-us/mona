/**
 * Created by sajjan on 11/13/15.
 */
import {Download} from "../../services/persistence/download.resource";
import {NGXLogger} from "ngx-logger";
import {environment} from "../../../environments/environment";
import {Component, OnInit} from "@angular/core";
import {first} from "rxjs/operators";

@Component({
    selector: 'query-tree',
    templateUrl: '../../views/spectra/dbindex/queryTree.html'

})
export class QueryTreeComponent implements OnInit {
    public showEmptyDownloads;
    public queries;
    public queryTree;
    public static: any[] = [];
    public tree;

    constructor(public download: Download, public logger: NGXLogger) {}

    ngOnInit() {
        this.showEmptyDownloads = false;
        this.queries = {};
        this.queryTree = [];
        this.tree = {};

        this.getPredefinedQueries();
        this.getStaticDownloads();
    }

    executeQuery(node: any): string {
        return `${environment.REST_BACKEND_SERVER}/spectra/browse?query=${node.query}`;
    };

    downloadJSON(node: any): string {
        return `${environment.REST_BACKEND_SERVER}/rest/downloads/retrieve/${node.jsonExport.id}`;
    };

    downloadMSP(node: any): string {
        return `${environment.REST_BACKEND_SERVER}/rest/downloads/retrieve/${node.mspExport.id}`;
    };

    downloadSDF(node: any): string {
        return `${environment.REST_BACKEND_SERVER}/rest/downloads/retrieve/${node.sdfExport.id}`;
    };

    /**
     * Get predefined queries and build query tree
     */
    getPredefinedQueries = () => {
        let data;
        this.download.getPredefinedQueries().pipe(first()).subscribe(
            (res: any) => {
                data = res;
                // Header entry for libraries, which is displayed by default if any libraries are being displayed
                data.unshift({
                    label: "Libraries",
                    query: null,
                    jsonExport: null,
                    mspExport: null,
                    sdfExport: null,
                    display: data.some((x) => { return x.label.indexOf('Libraries') > -1 && x.queryCount > 0; })
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
                this.logger.error('query tree failed: ' + error);
            }
        );
    };

    getStaticDownloads = () => {
        this.download.getStaticDownloads().pipe(first()).subscribe(
            (res: any) => {
                res.forEach((x) => {
                    if (typeof x.category !== 'undefined') {
                        let categoryName = x.category[0].toUpperCase() + x.category.substr(1);

                        if (!this.static.hasOwnProperty(categoryName)) {
                            this.static[categoryName] = [];
                        }

                        x.path = `${environment.REST_BACKEND_SERVER}/rest/downloads/static/${x.category}/${x.fileName}`;
                        this.static[categoryName].push(x);
                    } else {
                        if (!this.static.hasOwnProperty('General')) {
                            this.static['General'] = [];
                        }

                        x.path = `${environment.REST_BACKEND_SERVER}/rest/downloads/static/${x.fileName}`;
                        this.static['General'].push(x);
                    }
                });
            },
            (error) => {
                this.logger.error('query tree failed: ' + error);
            }
        );
    };

}
