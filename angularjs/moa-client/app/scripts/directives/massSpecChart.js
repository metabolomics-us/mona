/**
 * Created by sajjan on 6/12/14.
 */

'use strict';

app.directive('chart', function () {
    return{
        restrict: 'E',
        link: function(scope, elem, attrs){

            var options = {
                    series: {
                        color: '#00f',
                        bars: {
                            show: true,
                            barWidth: 0.001,
                            align: "center"
                        }
                    },
                    grid: {
                        labelMargin: 10,
                        backgroundColor: '#fff',
                        color: '#e2e6e9',
                        borderColor: null
                    }
                };

            var data = scope[attrs.ngModel].split(' ').map(function(x) { return x.split(':').map(Number)});
            $.plot(elem, [q], options);
            elem.show();

            /*
            // If the data changes somehow, update it in the chart
            scope.$watch('data', function(v){
                if(!chart){
                    chart = $.plot(elem, v , options);
                    elem.show();
                }else{
                    chart.setData(v);
                    chart.setupGrid();
                    chart.draw();
                }
            });
            */
        }
    };
});
