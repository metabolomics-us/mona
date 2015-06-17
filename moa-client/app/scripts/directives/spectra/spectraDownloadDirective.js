/**
 * Created by wohlgemuth on 6/16/15.
 */
/**
 * Created by wohlgemuth on 10/16/14.
 */
app.directive('spectraDownload', function(Spectrum, $http, $filter, $log,REST_BACKEND_SERVER ) {
    return {
        require: "ngModel",
        restrict: "A",
        replace: true,
        scope:{
            spectra:'=spectra'
        },
        templateUrl: '/views/templates/spectra/download.html',
        controller: function($scope,SpectraQueryBuilderService){

            /**
             * attempts to download a msp file
             */
            $scope.asMsp = function(){

                if(angular.isDefined($scope.spectra)) {
                    var uri = $filter('spectraDownloadAsMsp')($scope.spectra.id);

                    $http.get(uri).then(function (returnData) {
                        $scope.downloadData(returnData.data, $scope.spectra.id + ".msp");
                    });
                }
                else{
                    var uri = REST_BACKEND_SERVER + "/rest/spectra/search";

                    $http.post(uri,{query:SpectraQueryBuilderService.getQuery()}).then(function (returnData) {
                        $scope.downloadData(returnData.data, "result.msp");
                    });

                }
            };

            /**
             * does the actual downloading of the content
             * @param data
             * @param name
             */
            $scope.downloadData = function(data,name){
                var hiddenElement = document.createElement('a');

                hiddenElement.href = 'data:attachment/csv,' + encodeURI(data);
                hiddenElement.target = '_blank';
                hiddenElement.download = name;

                document.body.appendChild(hiddenElement);
                hiddenElement.click();
                document.body.removeChild(hiddenElement);

            };

            /**
             * attempts to download as a mona file
             */
            $scope.asMona = function(){


                if(angular.isDefined($scope.spectra)) {

                    var uri = $filter('spectraDownload')($scope.spectra.id);

                    $http.get(uri).then(function(returnData){
                        $scope.downloadData($filter('json')(returnData.data), $scope.spectra.id + ".json");
                    });
                }
                else{
                    var uri = REST_BACKEND_SERVER + "/rest/spectra/search";

                    $http.post(uri,{query:SpectraQueryBuilderService.getQuery()}).then(function (returnData) {
                        $scope.downloadData($filter('json')(returnData.data), "result.json");
                    });

                }
            }
        }
    };
});
