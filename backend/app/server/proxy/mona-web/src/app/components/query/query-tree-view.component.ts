/**
 * Modified version of http://github.com/eu81273/angular.treeview
 */
import {Component, ElementRef, Input, OnInit} from '@angular/core';
import {NGXLogger} from 'ngx-logger';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';

@Component({
    selector: 'query-tree-view',
    templateUrl: '../../views/templates/query/queryTreeView.html'
})
export class QueryTreeViewComponent implements OnInit {
    public templateHtml$: Observable<string>;
    public showHidden;
    public templatePrime;
    public newStyle;
    public depth;

    @Input() public treeId;
    @Input() public treeModel;
    @Input() public treeDepth;

    constructor( public elementRef: ElementRef,  public logger: NGXLogger) {}

    ngOnInit(): void {
        console.log(this.treeModel);
        this.depth = (typeof this.treeDepth === 'undefined') ? 0 : parseInt(this.treeDepth, 10);
        this.newStyle = this.depth > 0 ? 'padding-left: ' + (3 * this.depth) + 'em' : '';

        if (this.treeId && this.treeModel) {
            if (this.depth === 0) {

                // Create tree object if not exists
                this.treeId = this.treeId || {};

                // Collapse/expand node
                if (this.treeId.selectNodeHead) {
                  this.treeId.selectNodeHead = this.treeId.selectNodeHead;
                } else {
                  this.treeId.selectNodeHead = (selectedNode) => {
                    selectedNode.collapsed = !selectedNode.collapsed;
                  };
                }

                // If node label clicks,
                if (this.treeId.selectNodeLabel) {
                  this.treeId.selectNodeLabel = this.treeId.selectNodeLabel;
                } else {
                  this.treeId.selectNodeLabel = (selectedNode) => {

                    // remove highlight from previous node
                    if (this.treeId.currentNode && this.treeId.currentNode.selected) {
                      this.treeId.currentNode.selected = undefined;
                    }

                    // set highlight to selected node
                    selectedNode.selected = 'selected';

                    // set currentNode
                    this.treeId.currentNode = selectedNode;
                  };
                }
            }
        }

    }

    executeQuery(node: any): string {
        return `/spectra/browse?query=${node.query}`;
    }

    downloadJSON(node: any): string {
        return `${environment.REST_BACKEND_SERVER}/rest/downloads/retrieve/${node.jsonExport.id}`;
    }

    downloadMSP(node: any): string {
        return `${environment.REST_BACKEND_SERVER}/rest/downloads/retrieve/${node.mspExport.id}`;
    }

    downloadSDF(node: any): string {
        return `${environment.REST_BACKEND_SERVER}/rest/downloads/retrieve/${node.sdfExport.id}`;
    }
}
