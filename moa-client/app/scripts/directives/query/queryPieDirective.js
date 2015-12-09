(function() {
    'use strict';

    angular.module('moaClientApp')
      .directive('queryPie', queryPie);

    function queryPie() {
        var directive = {
            restrict: 'E',
            template: '<div style="width: 100%; height: 100%; display: inline-block;">' +
            '    <div class="statistics-chart" style="width: 100%; height: 100%"></div>' +
            '</div>',
            replace: 'true',
            scope: {
                name: '=',
                data: '=ngModel'
            },
            link: linkFunc,
            controller: queryPieController
        };

        return directive;
    }


    function linkFunc(scope, elem, attrs) {
        var opts = {
            series: {
                pie: {
                    show: true
                }
            },
            grid: {
                hoverable: true,
                clickable: true
            }
        };

        if (typeof scope.data !== 'undefined') {
            var plotData = [];
            var minValue = 0;

            if (scope.data.length > 10) {
                var lengths = [];

                for (var i = 0; i < scope.data.length; i++)
                    lengths.push(scope.data[i].count)

                lengths.sort(function(a, b) {
                    return b - a
                });
                minValue = lengths[9];
            }

            for (var i = 0; i < scope.data.length; i++) {
                if (scope.data[i].count >= minValue) {
                    plotData.push({
                        label: scope.data[i].value,
                        data: scope.data[i].count
                    });
                }
            }

            var chart = $.plot(elem, plotData, opts);
            elem.show();
        }

        elem.bind("plotclick", scope.redirect);
    }

    queryPieController.$inject = ['$scope', '$location', 'SpectraQueryBuilderService'];

    function queryPieController($scope, $location, SpectraQueryBuilderService) {
        $scope.redirect = function(event, pos, obj) {
            if (typeof obj !== 'undefined') {
                var query = {};
                query[$scope.name] = obj.series.label;

                SpectraQueryBuilderService.compileQuery(query);
                $location.path('/spectra/browse/');
                $scope.$apply();
            }
        }
    }
})();