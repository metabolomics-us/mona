/**
 * Created by sajjan on 6/12/14.
 */

'use strict';

app.directive('chart', function () {
    return{
        restrict: 'E',
        link: function(scope, element, attrs) {
            // Get spectrum data
            var data = scope[attrs.ngModel].spectrum
            data = data.split(' ').map(function(x) { return x.split(':').map(Number) });

            // Find maximal m/z and intensity values
            var mz_max = Math.max.apply(Math, data.map(function(x) { return x[0]; }));
            var intensity_max = Math.max.apply(Math, data.map(function(x) { return x[1]; }));


            // Define options
            var options = {
                    series: {
                        color: '#00f',
                        bars: {
                            show: true,
                            barWidth: 0.00001,
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

            if('mini' in attrs) {
                options.xaxis = { ticks: false } 
                options.yaxis = { ticks: false } 

                // Filter low intensity peaks
                data = data.filter(function(x) { return x[1] > 0.01 * intensity_max });
            }

            $.plot(element, [data], options);
            element.show();
        }
    };
});
