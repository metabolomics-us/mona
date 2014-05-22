/**
 * Defines all our routes in this application
 */
massspecsOfAmerica.config(['$routeProvider',
    function ($routeProvider) {
        $routeProvider.
            when('/submitters', {
                templateUrl: 'partial/submitters/list.html',
                controller: 'SubmitterController'
            }
        ).
            when('/upload/single', {
                templateUrl: 'partial/upload/single.html',
                controller: 'SpectraController'
            }
        ).
            otherwise({
                redirectTo: '/submitters'
            }
        )
    }
]
);