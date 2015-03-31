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

        replace: 'true',
        template:
        '<div style="width: 100%; height: 100%; display: inline-block;">'+
        '    <div class="statistics-chart" style="width: 100%; height: 100%"></div>'+
        '</div>',

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
            var opts  = {
                series: {
                    pie: {
                        show: true,
                        //radius: 3/4
                        //label: {
                        //    show: true,
                        //    radius: 1,
                        //    formatter: function(label, series){
                        //        return '<div style="font-size: 8pt; text-align: center; padding: 2px; color: white;">'+ label +' ('+ Math.round(series.percent) +'%)</div>';
                        //    },
                        //    background: {
                        //        opacity: 0.8
                        //    },
                        //    threshold: 0.01
                        //}
                    }
                },
                grid: {
                    hoverable: true,
                    clickable: true
                }
            };

            if(typeof scope.data != 'undefined') {
                var plotData = [];
                var minValue = 0;

                if(scope.data.length > 10) {
                    var lengths = [];

                    for (var i = 0; i < scope.data.length; i++)
                        lengths.push(scope.data[i].count)

                    lengths.sort(function(a, b){return b-a});
                    minValue = lengths[9];
                }

                for (var i = 0; i < scope.data.length; i++) {
                    if(scope.data[i].count >= minValue) {
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
    };
});
