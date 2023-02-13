/**
 * Created by sajjan on 8/21/14.
 *
 * Stores spectra browser data, individual spectrum, and query data for
 * persistence between controllers and views
 */

import {NGXLogger} from 'ngx-logger';
import {Injectable} from '@angular/core';

@Injectable()
export class SpectrumCacheService{
    browserSpectra;
    browserSpectraScroll;
    browserLocation;
    spectrum;
    currentCount;

    constructor(public logger: NGXLogger) {
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

      /**
       * Stored Count
       */
      this.currentCount = {};
    }

  /**
     * Clear all values stored in this cache factory
     */
    clear() {
        this.removeBrowserSpectra();
        this.removeBrowserLocation();
        this.removeSpectrum();
    }


    hasBrowserSpectra() {
        return this.browserSpectra !== null;
    }

    getBrowserSpectra() {
        return this.browserSpectra;
    }

    setBrowserSpectra(browserSpectra) {
        this.browserSpectraScroll = window.scrollTo(0, 0);
        this.browserSpectra = browserSpectra;
    }

    removeBrowserSpectra() {
        this.browserSpectraScroll = null;
        this.browserSpectra = null;
    }

    getBrowserSpectraScrollLocation() {
        return this.browserSpectraScroll;
    }

    hasBrowserLocation() {
      return this.browserLocation !== null;
    }

    getBrowserLocation() {
        return this.browserLocation;
    }

    setBrowserLocation(browserLocation) {
        this.browserLocation = browserLocation;
    }

    removeBrowserLocation() {
        this.browserLocation = null;
    }

    hasSpectrum() {
        return this.spectrum !== null;
    }

    getSpectrum() {
        return this.spectrum;
    }

    setSpectrum(spectrum) {
        this.spectrum = spectrum;
    }

    removeSpectrum() {
        this.spectrum = null;
    }

    hasCurrentCount(query: string) {
      return query in this.currentCount;
    }

    getCurrentCount(query: string) {
      return this.currentCount[query];
    }

    setCurrentCount(query: string, count: number) {
      this.currentCount[query] = count;
    }

    removeCurrentCount(query) {
      delete this.currentCount[query];
    }


}
