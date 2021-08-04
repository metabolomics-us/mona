/**
 * Created by sajjan on 8/21/14.
 *
 * Stores spectra browser data, individual spectrum, and query data for
 * persistence between controllers and views
 */

import {NGXLogger} from "ngx-logger";
import {Inject} from "@angular/core";
import {downgradeInjectable} from "@angular/upgrade/static";
import * as angular from 'angular';

export class SpectrumCacheService{
    private browserSpectra;
    private browserSpectraScroll;
    private browserLocation;
    private spectrum;

    constructor(@Inject(NGXLogger) private logger: NGXLogger) {
        /**
         * Stored browser spectra
         */
        this.browserSpectra = null;

        /**
         * Stored browser spectra scoll location
         */
        this.browserSpectraScroll = null;

        /**
         * Stored browser scroll location
         */
        this.browserLocation = null;

        /**
         * Stored spectrum for viewing
         */
        this.spectrum = null;
    }

    /**
     * Clear all values stored in this cache factory
     */
    clear = () => {
        this.removeBrowserSpectra();
        this.removeBrowserLocation();
        this.removeSpectrum();
    };


    hasBrowserSpectra = () => {
        return this.browserSpectra !== null;
    };
    getBrowserSpectra = () =>{
        return this.browserSpectra;
    };
    setBrowserSpectra = (browserSpectra) =>{
        this.browserSpectraScroll = $(window).scrollTop();
        this.browserSpectra = browserSpectra;
    };
    removeBrowserSpectra = () =>{
        this.browserSpectraScroll = null;
        this.browserSpectra = null;
    };

    getBrowserSpectraScrollLocation = () =>{
        return this.browserSpectraScroll;
    };


    hasBrowserLocation = () =>{
        return this.browserLocation !== null;
    };
    getBrowserLocation = () =>{
        return this.browserLocation;
    };
    setBrowserLocation = (browserLocation) =>{
        this.browserLocation = browserLocation;
    };
    removeBrowserLocation = () =>{
        this.browserLocation = null;
    };


    hasSpectrum = () =>{
        return this.spectrum !== null;
    };
    getSpectrum = () =>{
        return this.spectrum;
    };
    setSpectrum = (spectrum) =>{
        this.spectrum = spectrum;
    };
    removeSpectrum = () =>{
        this.spectrum = null;
    };
}

angular.module('moaClientApp')
    .factory('SpectrumCache', downgradeInjectable(SpectrumCacheService));
