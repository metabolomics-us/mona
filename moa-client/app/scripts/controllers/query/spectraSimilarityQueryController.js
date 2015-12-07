(function() {
    /**
     * Created by sajjan on 8/14/15.
     */
    'use strict';

    moaControllers.SpectraSimilarityQueryController = ['$scope', '$location', 'UploadLibraryService', 'SplashService', 'SpectraQueryBuilderService',
        function($scope, $location, UploadLibraryService, SplashService, SpectraQueryBuilderService) {
            $scope.queryState = 0;
            $scope.inputMode = 'upload';

            $scope.spectrumIons = [];
            $scope.spectraCount = 0;

            $scope.queryOptions = {};


            /**
             * Sort order for the ion table - default m/z ascending
             */
            $scope.ionTableSort = 'ion';
            $scope.ionTableSortReverse = false;

            $scope.sortIonTable = function(column) {
                if (column === 'ion') {
                    $scope.ionTableSortReverse = ($scope.ionTableSort === '+ion') ? !$scope.ionTableSortReverse : false;
                    $scope.ionTableSort = '+ion';
                }
                else if (column === 'intensity') {
                    $scope.ionTableSortReverse = ($scope.ionTableSort === '-intensity') ? !$scope.ionTableSortReverse : false;
                    $scope.ionTableSort = '-intensity';
                }
                else if (column === 'annotation') {
                    $scope.ionTableSortReverse = ($scope.ionTableSort === '-annotation') ? !$scope.ionTableSortReverse : false;
                    $scope.ionTableSort = '-annotation';
                }
            };


            /**
             * Parse spectra
             * @param files
             */
            $scope.parseFiles = function(files) {
                $scope.queryState = 1;

                UploadLibraryService.loadSpectraFile(files[0],
                  function(data, origin) {
                      UploadLibraryService.processData(data, function(spectrum) {
                          $scope.$apply(function() {
                              if ($scope.spectraCount === 0) {
                                  // Create list of ions
                                  $scope.spectrumIons = spectrum.spectrum.split(' ').map(function(x) {
                                      x = x.split(':');
                                      var annotation = '';

                                      for (var i = 0, l = spectrum.meta.length; i < l; i++) {
                                          if (spectrum.meta[i].category === 'annotation' && spectrum.meta[i].value === x[0]) {
                                              annotation = spectrum.meta[i].name;
                                          }
                                      }

                                      return {
                                          ion: parseFloat(x[0]),
                                          intensity: parseFloat(x[1]),
                                          ionStr: x[0],
                                          intensityStr: x[1],
                                          annotation: annotation,
                                          selected: true
                                      }
                                  });

                                  $scope.showIonTable = ($scope.spectrumIons.length < 500);
                                  $scope.queryState = 2;
                              }

                              $scope.spectraCount++;
                          });
                      }, origin);
                  },
                  function(progress) {
                  }
                );
            };

            /**
             * utilizes a splash based search
             * @param splash
             */
            $scope.parseSplash = function(splash) {

                $scope.splash = splash;
                $scope.queryState = 3;
            };

            $scope.parsePastedSpectrum = function(pastedSpectrum) {
                $scope.spectrumIons = pastedSpectrum.split(' ').map(function(x) {
                    x = x.split(':');
                    var annotation = '';

                    return {
                        ion: parseFloat(x[0]),
                        intensity: parseFloat(x[1]),
                        ionStr: x[0],
                        intensityStr: x[1],
                        annotation: annotation,
                        selected: true
                    }
                });

                $scope.showIonTable = ($scope.spectrumIons.length < 500);
                $scope.queryState = 2;
                $scope.spectraCount = 1;
            };


            $scope.query = function() {
                if (angular.isDefined($scope.queryOptions.queryType)) {

                    //splash based queries
                    if ($scope.queryState === 3) {

                        SpectraQueryBuilderService.prepareQuery();

                        if ($scope.queryOptions.queryType === "exact") {
                            SpectraQueryBuilderService.addExactSpectraSearchToQuery($scope.splash);
                        }
                        else if ($scope.queryOptions.queryType === 'histogram') {
                            SpectraQueryBuilderService.addMatchingHistogramToQuery($scope.splash);
                        }
                        $location.path("/spectra/browse/");

                    }
                    //spectra based queries
                    else {
                        // Define splash object
                        var splashObject = {ions: [], type: 'MS'};

                        // Create spectrum string
                        var spectrumString = '';

                        for (var i = 0, l = $scope.spectrumIons.length; i < l; i++) {
                            if ($scope.spectrumIons[i].selected) {
                                spectrumString += (spectrumString === '' ? '' : ' ')
                                  + $scope.spectrumIons[i].ionStr
                                  + ':' + $scope.spectrumIons[i].intensityStr;

                                splashObject.ions.push({
                                    mass: $scope.spectrumIons[i].ion,
                                    intensity: $scope.spectrumIons[i].intensity
                                });
                            }
                        }

                        // Perform similarity search
                        if ($scope.queryOptions.queryType === 'similar') {
                            SpectraQueryBuilderService.prepareQuery();
                            SpectraQueryBuilderService.addSimilarSpectraToQuery(null, spectrumString);
                            $location.path("/spectra/browse/");
                        }

                        // Perform exact search, and get splash id
                        else {
                            SplashService.splashIt(splashObject).$promise.then(function(data) {
                                SpectraQueryBuilderService.prepareQuery();
                                SpectraQueryBuilderService.addExactSpectraSearchToQuery(data.splash);
                                $location.path("/spectra/browse/");
                            });
                        }
                    }
                }
            };

            // For testing
            //(function() {
            //    //$scope.parsePastedSpectrum('603.5352:100 265.253:5.005 744.5543:5.005');
            //    $scope.parsePastedSpectrum('63:0.14 155:0.45 80:6.5307 106:1.2801 90:2.7203 75:0.43 82:0.7601 171:5.1805 186:73.2273 42:0.17 41:0.2 133:0.11 159:9.4309 189:6.6007 109:0.6401 43:0.14 92:0.24 39:0.22 56:0.16 58:0.25 105:2.6803 107:100 76:0.25 77:34.0034 170:0.7401 81:0.34 67:0.11 190:0.47 53:0.44 57:0.11 184:0.7501 50:0.23 156:0.5301 183:0.9401 78:12.7813 93:0.15 168:0.17 158:1.2601 188:70.9471 108:9.711 160:0.7301 169:5.1005 185:9.1109 79:83.9584 91:0.5501 52:0.15 172:0.5701 187:14.0414 51:0.41 55:0.14 157:9.771 89:3.9604')
            //    $scope.queryState = 2;
            //})();
        }];
})();