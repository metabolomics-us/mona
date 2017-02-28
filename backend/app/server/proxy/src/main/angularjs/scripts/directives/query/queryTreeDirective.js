/**
 * Modified version of http://github.com/eu81273/angular.treeview
 */

(function() {
    'use strict';

    queryTreeView.$inject = ['$compile'];
    angular.module('moaClientApp')
        .directive('queryTreeView', queryTreeView);

    /* @ngInject */
    function queryTreeView($compile) {
        return {
            restrict: 'A',

            /**
             * Recursive tree-building function
             * @param scope
             * @param element
             * @param attrs
             * @param attrs.treeId
             * @param attrs.treeModel
             * @param attrs.queryTreeDepth
             */
            link: function(scope, element, attrs) {
                // References to data and index for recursion
                var treeId = attrs.treeId;
                var treeModel = attrs.treeModel;
                var depth = angular.isUndefined(attrs.queryTreeDepth) ? 0 : parseInt(attrs.queryTreeDepth);

                var style = depth > 0 ? 'padding-left: '+ (3 * depth) +'em' : '';

                var template =
                    '<div class="list-group-item" style="'+ style +'" data-ng-repeat-start="node in '+ treeModel +'">' +
                    '    <i class="fa fa-folder-o" data-ng-show="node.children.length && node.collapsed" data-ng-click="' + treeId + '.selectNodeHead(node)"></i>' +
                    '    <i class="fa fa-folder-open-o" data-ng-show="node.children.length && !node.collapsed" data-ng-click="' + treeId + '.selectNodeHead(node)"></i>' +
                    '    <i class="fa fa-file-text-o" data-ng-hide="node.children.length"></i>' +

                    '    <span data-ng-if="node.query !== null"><a data-ng-href="{{executeQuery(node)}}"><i class="fa fa-search"></i> {{node.label}}</a> ({{node.queryCount | number:0}} {{node.queryCount == 1 ? "spectrum" : "spectra"}})</span>'+
                    '    <span data-ng-if="node.query === null"> {{node.label}}</span>'+

                    '    <span class="pull-right" data-ng-show="node.jsonExport !== null || node.mspExport !== null" data-uib-dropdown>' +
                    '        <a href data-uib-dropdown-toggle class="dropdown-toggle"><i class="fa fa-download"></i> Download</a>' +
                    '        <ul class="uib-dropdown-menu dropdown-menu-right">' +
                    '            <li data-ng-if="node.jsonExport !== null"><a data-ng-href="{{downloadJSON(node)}}" target="_self" download data-ga-track-event="[\'download\', \'click\', node.downloadLabel +\'.json\']"><i class="fa fa-download"></i> JSON (Internal MoNA Format) ({{node.jsonExport.size | bytes}})</a></li>' +
                    '            <li data-ng-if="node.mspExport !== null"><a data-ng-href="{{downloadMSP(node)}}" target="_self" download data-ga-track-event="[\'download\', \'click\', node.downloadLabel +\'.msp\']"><i class="fa fa-download"></i> MSP (NIST compatible) ({{node.mspExport.size | bytes}})</a></li>' +
                    '        </ul>' +
                    '    </span>' +
                    '    <span class="pull-right" data-ng-if="node.jsonExport === null && node.mspExport === null && node.query !== null">Export generation in progress...</span>' +

                    '</div>'+
                    '<div data-ng-hide="node.collapsed" class="list-group" data-ng-repeat-end data-ng-if="node.children.length" data-query-tree-view data-query-tree-depth="'+ (depth + 1) +'" data-tree-id="'+ treeId +'" data-tree-model="node.children"></div>';

                if(treeId && treeModel) {
                    if(depth == 0) {
                        template = '<div class="list-group panel-query-tree well">'+ template +'</div>';

                        // Create tree object if not exists
                        scope[treeId] = scope[treeId] || {};

                        // Collapse/expand node
                        scope[treeId].selectNodeHead = scope[treeId].selectNodeHead || function(selectedNode) {
                            selectedNode.collapsed = !selectedNode.collapsed;
                        };

                        // If node label clicks,
                        scope[treeId].selectNodeLabel = scope[treeId].selectNodeLabel || function(selectedNode) {

                            //remove highlight from previous node
                            if( scope[treeId].currentNode && scope[treeId].currentNode.selected ) {
                                scope[treeId].currentNode.selected = undefined;
                            }

                            //set highlight to selected node
                            selectedNode.selected = 'selected';

                            //set currentNode
                            scope[treeId].currentNode = selectedNode;
                        };
                    }

                    element.html('').append($compile(template)(scope));
                }
            }
        };
    }
})();
