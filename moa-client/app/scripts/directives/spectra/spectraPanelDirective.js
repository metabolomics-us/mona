/**
 * Created by wohlgemuth on 10/16/14.
 */
(function() {
    'use strict';

    angular.module('moaClientApp')
        .directive('displaySpectraPanel', displaySpectraPanel);

    function displaySpectraPanel() {
        var directive = {
            require: "ngModel",
            restrict: "A",
            templateUrl: '/views/spectra/display/panel.html',
            replace: true,
            scope: {
                spectrum: '=spectrum'
            },
            controller: displaySpectraPanelController
        };

        return directive;
    }

    /* @ngInject */
    function displaySpectraPanelController($scope, $location, SpectrumCache) {

        var truncateDecimal = function(s, length) {
            var regex = new RegExp("\\s*(\\d+\\.\\d{" + length + "})\\d*\\s*");
            var m = s.match(regex);
            return (m !== null) ? s.replace(m[0].trim(), m[1]) : s;
        };

        angular.forEach($scope.spectrum.metaData, function(meta, index) {
            if (meta.category !== 'annotation' && meta.deleted !== 'true'
              && meta.hidden !== 'true' && meta.computed !== 'true') {
                meta.value = truncateDecimal(meta.value, 4);
            }
        });

        // Temporary Origin String
        var tags = [];
        for (var i = 0; i < $scope.spectrum.tags.length; i++) {
            tags.push($scope.spectrum.tags[i].text);
        }

        if (tags.indexOf('massbank') > -1) {
            var accession = null;
            var authors = null;

            for (var i = 0; i < $scope.spectrum.metaData.length; i++) {
                if ($scope.spectrum.metaData[i].name == 'accession')
                    accession = $scope.spectrum.metaData[i].value;
                if ($scope.spectrum.metaData[i].name == 'authors')
                    authors = $scope.spectrum.metaData[i].value;
            }

            $scope.origin = 'Originally submitted to the MassBank Spectral Database as <a href="http://www.massbank.jp/jsp/Dispatcher.jsp?type=disp&id='+ accession +'&site=23">'+ accession +'</a>';
            $scope.authors = 'Authors: '+ authors;
        } else if(tags.indexOf('gnps')) {
            var accession = null;
            var authors = [];

            for (var i = 0; i < $scope.spectrum.metaData.length; i++) {
                if ($scope.spectrum.metaData[i].name == 'spectrumid')
                    accession = $scope.spectrum.metaData[i].value;
                if ($scope.spectrum.metaData[i].name == 'authors')
                    authors.push($scope.spectrum.metaData[i].value);
            }

            $scope.origin = 'Originally submitted to the GNPS Library as <a href="http://gnps.ucsd.edu/ProteoSAFe/gnpslibraryspectrum.jsp?SpectrumID='+ accession +'">'+ accession +'</a>';
            $scope.authors = 'Authors: '+ authors.join(', ');
        } else {
            $scope.origin = "Originally submitted to the MoNA Spectral Library";
        }

        /**
         * displays the spectrum for the given index
         * @param id
         * @param index
         */
        $scope.viewSpectrum = function(id) {
            SpectrumCache.setBrowserSpectra($scope.spectrum);
            SpectrumCache.setSpectrum($scope.spectrum);
            $location.path('/spectra/display/' + $scope.spectrum.id);
        };
    }
})();

