/**
 * Created by sajjan on 10/20/15.
 */
'use strict';


app.factory('httpResponseInterceptor', function($q, $location) {
    return {
        response: function(response) {
            return $q(
                function success(response) {
                    console.log('Intercepted success '+ response.status)
                    console.log(response)
                    return response;
                },
                function error(response) {
                    console.log('Intercepted error '+ response.status)
                    console.log(response)
                    //if (response.status == 500) {
                    //    $location.path('/500');
                    //}

                    return $q.reject(response);
                }
            );
        }
    }
});

