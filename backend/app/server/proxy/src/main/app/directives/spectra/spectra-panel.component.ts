/**
 * Created by wohlgemuth on 10/16/14.
 */
import {Component, Inject, Input, OnInit} from "@angular/core";
import {downgradeComponent} from "@angular/upgrade/static";
import {SpectrumCacheService} from "../../services/cache/spectrum-cache.service";
import * as angular from 'angular';

@Component({
    selector: 'display-spectra-panel',
    templateUrl: '../../views/spectra/display/panel.html'
})
export class SpectraPanelComponent implements OnInit{
    @Input() public spectrum;
    private IMPORTANT_METADATA;
    private importantMetadata;
    private secondaryMetadata;

    constructor(@Inject(SpectrumCacheService) private spectrumCache: SpectrumCacheService) {}

    ngOnInit() {
        console.log(this.spectrum);
        // Top 10 important metadata fields
        this.IMPORTANT_METADATA = [
            'ms level', 'precursor type', 'precursor m/z', 'instrument', 'instrument type',
            'ionization', 'ionization mode', 'collision energy', 'retention time', 'retention index'
        ];

        this.importantMetadata = [];
        this.secondaryMetadata = [];

        this.spectrum.metaData.forEach((metaData, index) => {
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

        this.spectrum.metaData = this.importantMetadata.concat(this.secondaryMetadata).slice(0, 10);
    }

    truncateDecimal = (s, length) => {
        return (typeof(s) === 'number') ?  s.toFixed(length) :  s;
    };

    /**
     * displays the spectrum for the given index
     */
    viewSpectrum = () => {
        this.spectrumCache.setSpectrum(this.spectrum);

        return '/spectra/display/' + this.spectrum.id;
    };

}

angular.module('moaClientApp')
    .directive('displaySpectraPanel', downgradeComponent({
        component: SpectraPanelComponent,
        inputs: ['spectrum']
    }));

