
/**
 *
 * simple direcitve to calculate our score for us
 */
app.directive('scoreSpectra', function ($compile, $filter,Spectrum, $log) {
    return {
        //must be an attribute
        restrict: 'A',
        replace: true,
        templateUrl: '/views/templates/scoreSpectra.html',
        scope: {
            ngModel: '='
        },
        require: 'ngModel',

        /**
         * watches for changes and is used to modify the query terms on the fly
         * @param $scope
         * @param QueryCache
         * @param $log
         * @param $rootScope
         */
        controller: function ($scope) {

        },

        //decorate our elements based on there properties
        link: function ($scope, element, attrs, ngModel) {

            var delayedSpectrum = $scope.ngModel;

            //calculate the score of our spectrum
            if (angular.isDefined(delayedSpectrum.score) && delayedSpectrum.score !== null) {
                if (angular.isDefined(delayedSpectrum.score.scaledScore)) {
                    $scope.score = delayedSpectrum.score.scaledScore;
                }

            }

            if(!angular.isDefined($scope.score)){
                $scope.score = 0;

                //scoring the spectra on the fly if it hasn't been scored yet
                Spectrum.score({id:delayedSpectrum.id},function(result){

                    //adjusting the score with the just generated value
                    $scope.score = result.explaination.scaledScore ;
                });

            }

            $scope.score = $filter('number')($scope.score, 0);
        }
    }
});
