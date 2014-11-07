/**
 * Created by sajjan on 11/7/14.
 */
'use strict';

app.directive('queryPie', function(){
    return{
        restrict: 'E',

        scope: {
            name: '=',
            data: '=ngModel'
        },

        controller: function($scope, $location, SpectraQueryBuilderService) {
            $scope.redirect = function(event, pos, obj) {
                if(typeof obj != 'undefined') {
                    var query = {};
                    query[$scope.name] = obj.series.label;

                    SpectraQueryBuilderService.compileQuery(query);
                    $location.path('/spectra/browse/');
                    $scope.$apply();
                }
            }
        },

        link: function(scope, elem, attrs) {
            var chart = null;
            var opts  = {
                series: {
                    pie: {
                        show: true
                    }
                },
                grid: {
                    hoverable: true,
                    clickable: true
                },
                legend: {
                    show: false
                }
            };

            var data = scope.data;

            scope.$watch('data', function(v) {
                if(typeof v != 'undefined') {
                    var plotData = [];
                    var minValue = 0;

                    if(v.length > 10) {
                        var lengths = [];

                        for (var i = 0; i < v.length; i++)
                            lengths.push(v[i].count)

                        minValue = lengths[9];
                    }

                    for (var i = 0; i < v.length; i++) {
                        if(v[i].count >= minValue) {
                            plotData.push({
                                label: v[i].value,
                                data: v[i].count
                            });
                        }
                    }

                    if (!chart) {
                        chart = $.plot(elem, plotData, opts);
                        elem.show();
                    } else {
                        chart.setData(plotData);
                        chart.setupGrid();
                        chart.draw();
                    }
                }
            });

            elem.bind("plotclick", scope.redirect);
        }
    };
});
