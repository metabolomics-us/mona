/**
 * Modified version of http://github.com/eu81273/angular.treeview
 */
import {AfterViewInit, Component, ElementRef, EventEmitter, Inject, Input, OnInit, Output} from "@angular/core";
import {downgradeComponent} from "@angular/upgrade/static";
import {NGXLogger} from "ngx-logger";
import * as angular from 'angular';
import {Observable, of} from "rxjs";
import {delay} from "rxjs/operators";
import {environment} from "../../environments/environment";

@Component({
    selector: 'query-tree-view',
    templateUrl: '../../views/templates/query/queryTreeView.html'
})
export class QueryTreeViewComponent implements OnInit {
    private templateHtml$: Observable<string>;
    public showHidden;
    public templatePrime;
    public newStyle;
    public depth;

    @Input() public treeId;
    @Input() public treeModel;
    @Input() public treeDepth;

    constructor(@Inject(ElementRef) private elementRef: ElementRef, @Inject(NGXLogger) private logger: NGXLogger) {}

    ngOnInit(): void {
        this.depth = (typeof this.treeDepth === 'undefined') ? 0: parseInt(this.treeDepth);
        this.newStyle = this.depth > 0 ? 'padding-left: ' + (3 * this.depth) + 'em' : '';

        if (this.treeId && this.treeModel) {
            if (this.depth === 0) {

                // Create tree object if not exists
                this.treeId = this.treeId || {};

                // Collapse/expand node
                this.treeId.selectNodeHead = this.treeId.selectNodeHead || function (selectedNode) {
                    selectedNode.collapsed = !selectedNode.collapsed;
                };

                // If node label clicks,
                this.treeId.selectNodeLabel = this.treeId.selectNodeLabel || function (selectedNode) {

                    //remove highlight from previous node
                    if (this.treeId.currentNode && this.treeId.currentNode.selected) {
                        this.treeId.currentNode.selected = undefined;
                    }

                    //set highlight to selected node
                    selectedNode.selected = 'selected';

                    //set currentNode
                    this.treeId.currentNode = selectedNode;
                };
            }
        }

    }

    executeQuery(node: any): string {
        return `/spectra/browse?query=${node.query}`;
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
}

angular.module('moaClientApp')
    .directive('queryTreeView', downgradeComponent({
        component: QueryTreeViewComponent,
        inputs: ['treeId', 'treeModel', 'treeDepth'],
    }));
