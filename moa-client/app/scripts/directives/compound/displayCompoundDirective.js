/**
 * Created by wohlgemuth on 10/16/14.
 */
app.directive('displayCompoundInfo', function() {
    return {
        require: "ngModel",
        restrict: "A",
        replace: true,
        scope:{
            compound:'=compound'
        },
        templateUrl: '/views/compounds/display/template/displayCompound.html',
        controller: function($scope){

            //calculate some unique id for the compound picture
            $scope.pictureId = Math.floor(Math.random()*1000);

        }
    };
});
