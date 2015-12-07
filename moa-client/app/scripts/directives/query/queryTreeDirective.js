/**
 * Modified version of http://github.com/eu81273/angular.treeview
 */

(function() {
    'use strict';

    app.directive('queryTreeView', ['$compile', function($compile) {
        return {
            restrict: 'A',
            link: function(scope, element, attrs) {
                // References to data and index for recursion
                var treeId = attrs.treeId;
                var treeModel = attrs.treeModel;
                var depth = angular.isUndefined(attrs.queryTreeDepth) ? 0 : parseInt(attrs.queryTreeDepth);

                var style = depth > 0 ? 'padding-left: ' + (3 * depth) + 'em' : '';

                var template =
                  '<p class="list-group-item" style="' + style + '" data-ng-repeat-start="node in ' + treeModel + '">' +
                  '    <i class="fa fa-folder-open-o" data-ng-show="node.children.length"></i>' +
                  '    <i class="fa fa-file-text-o" data-ng-hide="node.children.length"></i> ' +
                  '    <a href="" ng-click="executeQuery(node)"><i class="fa fa-search"></i> {{node.formattedLabel}}</a> ({{node.queryCount | number:0}})' +
                  '    <span class="pull-right" ng-if="node.exportId !== undefined"><a href="" ng-click="downloadQuery(node)"><i class="fa fa-download"></i> Download</a></span>' +
                  '</p>' +
                  '<div class="list-group" data-ng-repeat-end ng-if="node.children.length" data-query-tree-view data-query-tree-depth="' + (depth + 1) + '" data-tree-id="' + treeId + '" data-tree-model="node.children"></div>';

                if (treeId && treeModel) {
                    if (depth === 0) {
                        template = '<div class="list-group panel-query-tree well">' + template + '</div>';
                    }

                    element.html('').append($compile(template)(scope));
                }
            }
        };
    }]);
})();