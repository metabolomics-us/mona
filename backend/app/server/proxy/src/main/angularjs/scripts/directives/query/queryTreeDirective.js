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
        var directive = {
            restrict: 'A',

            link: function(scope, element, attrs) {
                // References to data and index for recursion
                var treeId = attrs.treeId;
                var treeModel = attrs.treeModel;
                var depth = angular.isUndefined(attrs.queryTreeDepth) ? 0 : parseInt(attrs.queryTreeDepth);

                var style = depth > 0 ? 'padding-left: '+ (3 * depth) +'em' : '';

                var template =
                    '<p class="list-group-item" style="'+ style +'" data-ng-repeat-start="node in '+ treeModel +'">' +
                    '    <i class="fa fa-folder-o" data-ng-show="node.children.length && node.collapsed" data-ng-click="' + treeId + '.selectNodeHead(node)"></i>' +
                    '    <i class="fa fa-folder-open-o" data-ng-show="node.children.length && !node.collapsed" data-ng-click="' + treeId + '.selectNodeHead(node)"></i>' +
                    '    <i class="fa fa-file-text-o" data-ng-hide="node.children.length"></i>' +

                    '    <span ng-if="node.query !== null"><a href="" ng-click="executeQuery(node)"><i class="fa fa-search"></i> {{node.label}}</a> ({{node.queryCount | number:0}} {{node.queryCount == 1 ? "spectrum" : "spectra"}})</span>'+
                    '    <span ng-if="node.query === null"> {{node.label}}</span>'+

                    '    <span class="pull-right">' +
                    '        <span ng-if="node.jsonExport !== null"><a ng-href="{{downloadJSON(node)}}"><i class="fa fa-download"></i> Download JSON</a> ({{node.jsonExport.size | bytes}})</span>&nbsp;' +
                    '        <span ng-if="node.mspExport !== null"><a ng-href="{{downloadMSP(node)}}"><i class="fa fa-download"></i> Download MSP</a> ({{node.mspExport.size | bytes}})</span>  ' +
                    '        <span ng-if="node.jsonExport === null && node.mspExport === null && node.query !== null">Export generation in progress...</span>' +
                    '   </span>' +
                    '</p>'+
                    '<div data-ng-hide="node.collapsed" class="list-group" data-ng-repeat-end ng-if="node.children.length" data-query-tree-view data-query-tree-depth="'+ (depth + 1) +'" data-tree-id="'+ treeId +'" data-tree-model="node.children"></div>';

                if(treeId && treeModel) {
                    if(depth == 0) {
                        template = '<div class="list-group panel-query-tree well">'+ template +'</div>';

                        //create tree object if not exists
                        scope[treeId] = scope[treeId] || {};

                        //if node head clicks,
                        scope[treeId].selectNodeHead = scope[treeId].selectNodeHead || function( selectedNode ){

                            //Collapse or Expand
                            selectedNode.collapsed = !selectedNode.collapsed;
                        };

                        //if node label clicks,
                        scope[treeId].selectNodeLabel = scope[treeId].selectNodeLabel || function( selectedNode ){

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

        return directive;
    }
})();
