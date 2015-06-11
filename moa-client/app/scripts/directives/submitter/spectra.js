
/**
 * provides us with some feedback how many spectra a certain person uploaded
 */
app.directive('spectraCountForUser', function($compile,StatisticsService){
    return {
        replace: true,
        scope: {
            user: '=user'
        },

        controller: function ($scope) {

            $scope.spectraCount = "loading...";

        },
        link: function ($scope, element, attrs, ngModel) {
            StatisticsService.spectraCount({id:$scope.user.id},
                function(data){
                    $scope.spectraCount = data.count;
                }
            );
        },

        template: '<span>{{spectraCount}}</span>'
    }
});

app.directive('spectraScoreForUser', function($compile,StatisticsService){
    return {
        replace: true,
        scope: {
            user: '=user'
        },

        controller: function ($scope) {

        },
        link: function ($scope, element, attrs, ngModel) {
            StatisticsService.spectraScore({id:$scope.user.id},
                function(data){
                    $scope.score = data.score;
                }
            );
        },

        templateUrl: '/views/templates/scoreSpectra.html'
    }
});

app.directive('spectraTopScoresForUsers', function($compile,StatisticsService, Submitter){
    return {
        replace: true,

        scope: {
            limit: '@'
        },
        controller: function ($scope) {

        },
        link: function ($scope, element, attrs, ngModel) {
            StatisticsService.spectraTopScores({max:$scope.limit},
                function(data){
                    var scores = data;

                    angular.forEach(scores,function(score){
                        score.submitter = Submitter.get({id:score.submitter});
                    });
                    $scope.scores = scores;

                }
            );
        },

        templateUrl: '/views/templates/scores/hallOfFame.html'
    }
});

/**
 * links a metadata field to a query builder and executes the spectra query for us
 */
app.directive('gwSubmitterQuery', function () {
    return {

        replace: true,
        transclude: true,
        templateUrl: '/views/templates/metaQuery.html',
        restrict: 'A',
        scope: {
            submitter: '=submitter'
        },
        link: function ($scope, element, attrs, ngModel) {

        },

        //controller to handle building new queries
        controller: function ($scope, $element, SpectraQueryBuilderService, QueryCache, $location) {

            //receive a click
            $scope.newQuery = function () {
                //build a mona query based on this label
                SpectraQueryBuilderService.prepareQuery();

                //add it to query
                SpectraQueryBuilderService.addUserToQuery($scope.submitter.emailAddress);

                //assign to the cache

                //run the query and show it's result in the spectra browser
                $location.path("/spectra/browse/");
            };

            //receive a click
            $scope.addToQuery = function () {
                SpectraQueryBuilderService.addUserToQuery($scope.submitter.emailAddress);
                $location.path("/spectra/browse/");
            };


            //receive a click
            $scope.removeFromQuery = function () {
                //build a mona query based on this label
                SpectraQueryBuilderService.removeUserFromQuery($scope.submitter.emailAddress);

                //run the query and show it's result in the spectra browser
                $location.path("/spectra/browse/");
            };
        }
    }
});