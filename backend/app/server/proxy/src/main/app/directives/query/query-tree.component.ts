/**
 * Modified version of http://github.com/eu81273/angular.treeview
 */
import {AfterViewInit, Component, ElementRef, Inject} from "@angular/core";
import {downgradeComponent} from "@angular/upgrade/static";
import * as angular from 'angular';
import {Observable, of} from "rxjs";
import {delay} from "rxjs/operators";

@Component({
    selector: 'query-tree-view',
    template: `<div #treeBinder></div>`
})
export class QueryTreeComponent implements AfterViewInit {
    private templateHtml$: Observable<string>;
    private treeId;
    private treeModel;
    constructor(@Inject(ElementRef) private elementRef: ElementRef) {}

    ngAfterViewInit(): void {
        let treeId = this.elementRef.nativeElement.treeId;
        let treeModel = this.elementRef.nativeElement.treeModel;
        let depth = (typeof this.elementRef.nativeElement.queryTreeDepth === 'undefined') ? 0: parseInt(this.elementRef.nativeElement.queryTreeDepth);

        let style = depth > 0 ? 'padding-left: ' + (3 * depth) + 'em' : '';

        let templateSecondary: string;
        let templatePrime = `<ng-container *ngIf="showHidden || node.display">
                <div class="list-group-item" style="${style}" *ngFor="let node of treeModel">
                <i class="fa fa-folder-o" [hidden]="node.children.length && node.collapsed" (click)="${treeId}.selectNodeHead(node)"></i>
                <i class="fa fa-folder-open-o" [hidden]="node.children.length && !node.collapsed" (click)="${treeId}.selectNodeHead(node)"></i>
                <i class="fa fa-file-text-o" [hidden]="node.children.length"></i>

                <span *ngIf="node.query !== null"><a href="{{executeQuery(node)}}"><i class="fa fa-search"></i> {{node.singleLabel}}</a> ({{node.queryCount | number:0}} {{node.queryCount == 1 ? "spectrum" : "spectra"}})</span>
                <span *ngIf="node.query === null"> {{node.singleLabel}}</span>'

                <span class="pull-right" [hidden]="node.jsonExport !== null || node.mspExport !== null || node.sdfExport !== null" ngbDropdown>
                    <a href ngbDropdownToggle class="dropdown-toggle"><i class="fa fa-download"></i> Download</a>
                    <ul class="uib-dropdown-menu dropdown-menu-right" placement="right" ngbDropdownMenu>
                        <li *ngIf="node.jsonExport !== null"><a href="{{$ctrl.downloadJSON(node)}}" target="_self" download data-ga-track-event="[\'download\', \'click\', node.downloadLabel +\'.json\']"><i class="fa fa-download"></i> JSON (Internal MoNA Format) ({{node.jsonExport.size | bytes}})</a></li>
                        <li *ngIf="node.mspExport !== null"><a href="{{$ctrl.downloadMSP(node)}}" target="_self" download data-ga-track-event="[\'download\', \'click\', node.downloadLabel +\'.msp\']"><i class="fa fa-download"></i> MSP (NIST compatible) ({{node.mspExport.size | bytes}})</a></li>
                        <li *ngIf"node.sdfExport !== null"><a href="{{$ctrl.downloadSDF(node)}}" target="_self" download data-ga-track-event="[\'download\', \'click\', node.downloadLabel +\'.sdf\']"><i class="fa fa-download"></i> SDF (NIST compatible) ({{node.sdfExport.size | bytes}})</a></li>
                    </ul>
                </span>
                <span class="pull-right" *ngIf="node.jsonExport === null && node.mspExport === null && node.query !== null">Export generation in progress...</span>

            </div>
            </ng-container>
            <div [hidden]="node.collapsed" class="list-group" *ngIf="node.children.length" data-query-tree-view data-query-tree-depth="' + (depth + 1) + '" data-tree-id="' + treeId + '" data-tree-model="node.children"></div>`;

        if (treeId && treeModel) {
            if (depth === 0) {
                templateSecondary = `<div class="form-group query-tree-display-option"><label><input type="checkbox" ngModel="showHidden"> Display Hidden Downloads</label></div>
                    <div class="list-group panel-query-tree well">${templatePrime}</div>`;

                // Create tree object if not exists
                treeId = treeId || {};

                // Collapse/expand node
                treeId.selectNodeHead = treeId.selectNodeHead || function (selectedNode) {
                    selectedNode.collapsed = !selectedNode.collapsed;
                };

                // If node label clicks,
                treeId.selectNodeLabel = treeId.selectNodeLabel || function (selectedNode) {

                    //remove highlight from previous node
                    if (treeId.currentNode && treeId.currentNode.selected) {
                        treeId.currentNode.selected = undefined;
                    }

                    //set highlight to selected node
                    selectedNode.selected = 'selected';

                    //set currentNode
                    treeId.currentNode = selectedNode;
                };
                this.templateHtml$ = of(templateSecondary).pipe(delay(1000));
            }
            else {
                this.templateHtml$ = of(templatePrime).pipe(delay(1000));
            }
            this.templateHtml$.subscribe((data) => this.elementRef.nativeElement.innerHTML = data);
        }

    }
}

angular.module('moaClientApp')
    .directive('queryTreeView', downgradeComponent({
        component: QueryTreeComponent
    }));
