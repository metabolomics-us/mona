/**
 * Created by wohlgemuth on 10/16/14.
 */
import * as angular from 'angular';

class SpectraPanelDirective {
    constructor() {
        return {
            require: "ngModel",
            restrict: "A",
            templateUrl: '../../views/spectra/display/panel.html',
            replace: true,
            scope: {
                spectrum: '=spectrum'
            },
            controller: SpectraPanelController,
            controllerAs: '$ctrl'
        };
    }
}

class SpectraPanelController {
    private static $inject = ['$scope', 'SpectrumCache'];
    private $scope;
    private SpectrumCache;
    private IMPORTANT_METADATA;
    private importantMetadata;
    private secondaryMetadata;

    constructor($scope, SpectrumCache) {
        this.$scope = $scope;
        this.SpectrumCache = SpectrumCache;
    }

    $onInit = () => {
        // Top 10 important metadata fields
        this.IMPORTANT_METADATA = [
            'ms level', 'precursor type', 'precursor m/z', 'instrument', 'instrument type',
            'ionization', 'ionization mode', 'collision energy', 'retention time', 'retention index'
        ];

        this.importantMetadata = [];
        this.secondaryMetadata = [];

        angular.forEach(this.$scope.spectrum.metaData, (metaData, index) => {
            metaData.value = this.truncateDecimal(metaData.value, 4);

            if (this.IMPORTANT_METADATA.indexOf(metaData.name.toLowerCase()) > -1) {
                this.importantMetadata.push(metaData);
            } else {
                this.secondaryMetadata.push(metaData);
            }
        });

        this.importantMetadata = this.importantMetadata.sort((a, b) =>  {
            if(this.IMPORTANT_METADATA.indexOf(b.name.toLowerCase()) < this.IMPORTANT_METADATA.indexOf(a.name.toLowerCase())){
                return -1
            }
            if(this.IMPORTANT_METADATA.indexOf(b.name.toLowerCase()) > this.IMPORTANT_METADATA.indexOf(a.name.toLowerCase())){
                return 1
            }
            else{
                return 0
            }
        });

        this.$scope.spectrum.metaData = this.importantMetadata.concat(this.secondaryMetadata).slice(0, 10);
    }

    truncateDecimal = (s, length) => {
        return (typeof(s) === 'number') ?  s.toFixed(length) :  s;
    };

    /**
     * displays the spectrum for the given index
     */
    viewSpectrum = () => {
        this.SpectrumCache.setSpectrum(this.$scope.spectrum);

        return '/spectra/display/' + this.$scope.spectrum.id;
    };

}

angular.module('moaClientApp')
    .directive('displaySpectraPanel', SpectraPanelDirective);

