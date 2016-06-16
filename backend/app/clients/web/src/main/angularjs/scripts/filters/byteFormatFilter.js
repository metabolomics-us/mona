/**
 * Created by sajjan on 2/26/16.
 */
(function() {
    'use strict';

    // https://gist.github.com/thomseddon/3511330
    angular.module('moaClientApp')
        .filter('bytes', function() {
            return function(bytes, precision) {
                if (bytes == 0)
                    return '0 bytes';

                if (isNaN(parseFloat(bytes)) || !isFinite(bytes))
                    return '-';

                if (angular.isUndefined(precision))
                    precision = 1;

                var units = ['bytes', 'kB', 'MB', 'GB', 'TB', 'PB'];
                var number = Math.floor(Math.log(bytes) / Math.log(1024));

                return (bytes / Math.pow(1024, Math.floor(number))).toFixed(precision) +  ' ' + units[number];
            }
        });
})();
