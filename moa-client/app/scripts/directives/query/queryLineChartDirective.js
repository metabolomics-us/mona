(function() {
    'use strict';

    angular.module('moaClientApp')
      .directive('queryLineChart', queryLineChart);

    function queryLineChart() {
        var directive = {
            require: 'ngModel',
            restrict: 'E',
            replace: 'true',
            template: '<div style="width: 100%; height: 100%; display: inline-block;">' +
            '    <div class="statistics-chart" style="width: 100%; height: 100%"></div>' +
            '</div>',
            link: linkFunc
        };

        return directive;
    }

    function linkFunc(scope, elem, attrs) {
        var opts = {
            //series: {
            //    pie: {
            //        show: true
            //    }
            //},
            xaxis: {
                mode: "time"
            },
            grid: {
                hoverable: true
            }
        };

        var placeholder = $(elem).find(".statistics-chart");
        var chart = null;

        scope.$watch(attrs.ngModel, function(v) {
            if (angular.isDefined(v)) {
                var plotData = [];

                for (var i = 0; i < v.length; i++) {
                    plotData.push([Date.parse(v[i].date), v[i].count]);
                }

                if (!chart) {
                    $.plot(placeholder, [plotData], opts);
                    placeholder.show();
                } else {
                    chart.setData([plotData]);
                    chart.setupGrid();
                    chart.draw();
                }
            }
        }, true);
    }
})();