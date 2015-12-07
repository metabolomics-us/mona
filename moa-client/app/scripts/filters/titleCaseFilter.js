(function() {
    'use strict';

    app.filter('titlecase', function() {
        return function(s) {
            s = ( angular.isUndefined(s) || s === null ) ? '' : s.toString();

            if(s.toUpperCase() === s) {
                return s;
            } else {
                return s.toLowerCase().replace(/\b([a-z])/g, function (ch) {
                    return ch.toUpperCase();
                });
            }
        };
    });
})();