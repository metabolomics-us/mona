/**
 * Created by wohlgemuth on 6/16/15.
 */
/**
 * Created by wohlgemuth on 10/16/14.
 */
app.directive('spectraDownload', function(Spectrum, $http, $filter, $log, REST_BACKEND_SERVER) {
    return {
        require: "ngModel",
        restrict: "A",
        replace: true,
        scope:{
            spectra:'=spectra'
        },
        templateUrl: '/views/templates/spectra/download.html',

        controller: function($scope, SpectraQueryBuilderService, dialogs) {
            /**
             * does the actual downloading of the content
             * @param data
             * @param name
             */
            $scope.downloadData = function(data, name) {
                var hiddenElement = document.createElement('a');

                hiddenElement.href = 'data:attachment/csv,' + encodeURI(data);
                hiddenElement.target = '_blank';
                hiddenElement.download = name;

                document.body.appendChild(hiddenElement);
                hiddenElement.click();
                document.body.removeChild(hiddenElement);
            };

            /**
             * attempts to download a msp file
             */
            $scope.downloadAsMsp = function() {
                if(angular.isDefined($scope.spectra)) {
                    var uri = $filter('spectraDownloadAsMsp')($scope.spectra.id);

                    $http.get(uri).then(function (returnData) {
                        $scope.downloadData(returnData.data, $scope.spectra.id + ".msp");
                    });
                } else {
                    var query = angular.copy(SpectraQueryBuilderService.getQuery());
                    query.format = 'msp';

                    submitQueryExportRequest(query);
                }
            };

            /**
             * attempts to download as a mona file
             */
            $scope.downloadAsMona = function() {
                if(angular.isDefined($scope.spectra)) {
                    var uri = $filter('spectraDownload')($scope.spectra.id);

                    $http.get(uri).then(function(returnData){
                        $scope.downloadData($filter('json')(returnData.data), $scope.spectra.id + ".json");
                    });
                } else {
                    var query = angular.copy(SpectraQueryBuilderService.getQuery());
                    query.format = 'json';

                    submitQueryExportRequest(query);
                }
            };

            /**
             * submit query for exporting and show modal dialog response
             */
            var submitQueryExportRequest = function(query) {
                var uri = REST_BACKEND_SERVER + "/rest/spectra/search/export";

                $http.post(uri, query).then(
                    function (response) {
                        dialogs.notify('Export request successful!', 'Your query export request has been submitted.  '+
                            'You will receive an email with a download link when the export has been completed.  '+
                            'This can take up to 24 hours for very large queries.',
                            {size: 'md', backdrop: 'static'});
                    },
                    function (response) {
                        dialogs.error('Error submitting request!',
                            response.status == 403 ? "You must be logged in to request a query export." :
                            "Could not reach MoNA server!",
                            {size: 'md', backdrop: 'static'});
                    }
                );
            }
        }
    };
});
