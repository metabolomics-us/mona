/**
 * Created by sajjan on 11/13/15.
 */
'use strict';

moaControllers.QueryTreeController = function($scope, Spectrum, $location, $log, SpectraQueryBuilderService) {
    $scope.executeQuery = function(node) {
        SpectraQueryBuilderService.setQuery(JSON.parse(node.query));
        $location.path('/spectra/browse');
    };

    $scope.downloadQuery = function(node) {

    };

    (function() {
        $scope.queries = {};
        $scope.queryTree = [];

        // Get predefined queries and build query tree
        Spectrum.getPredefinedQueries(
            function(data) {
                // Identify node parents
                for(var i = 0; i < data.length; i++) {
                    $scope.queries[data[i].label] = data[i];

                    var label = data[i].label.split(' - ');
                    data[i].depth = label.length;
                    data[i].id = i;
                    data[i].children = [];
                    data[i].singleLabel = label.pop();

                    var parentLabel = label.join(" - ");

                    if(data[i].depth == 1) {
                        data[i].parent = -1;
                        $scope.queryTree.push(data[i]);
                    } else {
                        for(var j = 0; j < data.length; j++) {
                            if(data[j].label == parentLabel) {
                                data[i].parent = j;
                                data[j].children.push(data[i]);
                                break;
                            }
                        }
                    }
                }
            },
            function (error) {
                $log.error('query tree failed: ' + error);
            }
        );
    })();
};


/**
 * Modified version of http://github.com/eu81273/angular.treeview
 */
app.directive('queryTreeView', ['$compile', function($compile) {
    return {
        restrict: 'A',
        link: function(scope, element, attrs) {
            // References to data and index for recursion
            var treeId = attrs.treeId;
            var treeModel = attrs.treeModel;
            var depth = angular.isUndefined(attrs.queryTreeDepth) ? 0 : parseInt(attrs.queryTreeDepth);

            var style = depth > 0 ? 'padding-left: '+ (3 * depth) +'em' : '';

            var template =
                '<p class="list-group-item" style="'+ style +'" data-ng-repeat-start="node in '+ treeModel +'">' +
                '    <i class="fa fa-folder-open-o" data-ng-show="node.children.length"></i>' +
            //    '    <i class="fa fa-file-text-o" data-ng-hide="node.children.length"></i> ' +
                '    <span><a href="" ng-click="executeQuery(node)"><i class="fa fa-search"></i> {{node.singleLabel | titlecase}}</a></span>' +
                '    <span class="pull-right" ng-if="node.exportId !== undefined"><a href="" ng-click="downloadQuery(node)"><i class="fa fa-download"></i> Download</a></span>' +
                '</p>'+
                '<div class="list-group" data-ng-repeat-end ng-if="node.children.length" data-query-tree-view data-query-tree-depth="'+ (depth + 1) +'" data-tree-id="'+ treeId +'" data-tree-model="node.children"></div>';

            if(treeId && treeModel) {
                if(depth == 0) {
                    template = '<div class="list-group panel-query-tree well">'+ template +'</div>';
                }

                element.html('').append($compile(template)(scope));
            }
        }
    };
}]);
