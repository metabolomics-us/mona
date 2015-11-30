
/**
 * provides us with some feedback how many spectra a certain person uploaded
 */
app.directive('spectraCountForUser', function($compile, StatisticsService){
    return {
        replace: true,

        scope: {
            user: '=user'
        },

        controller: function ($scope) {
            $scope.spectraCount = "Loading...";
        },

        link: function (scope, element, attrs, ngModel) {
            StatisticsService.spectraCount({id: scope.user.id},
                function(data) {
                    scope.spectraCount = data.count;
                }
            );
        },

        template: '<span>{{spectraCount}}</span>'
    }
});

app.directive('spectraScoreForUser', function($compile, StatisticsService){
    return {
        replace: true,

        scope: {
            user: '=user'
        },

        controller: function ($scope) {},

        link: function (scope, element, attrs, ngModel) {
            StatisticsService.spectraScore({id: scope.user.id},
                function(data) {
                    scope.score = data.score;
                }
            );
        },

        templateUrl: '/views/templates/scoreSpectra.html'
    }
});

app.directive('spectraTopScoresForUsers', function($compile, StatisticsService, Submitter){
    return {
        replace: true,

        scope: {
            limit: '@'
        },

        controller: function ($scope) {},

        link: function (scope, element, attrs, ngModel) {
            StatisticsService.spectraTopScores({max: scope.limit},
                function(data) {
                    var scores = data;

                    angular.forEach(scores,function(score) {
                        score.submitter = Submitter.get({id: score.submitter});
                    });

                    scope.scores = scores;
                }
            );
        },

        templateUrl: '/views/templates/scores/hallOfFame.html'
    }
});
