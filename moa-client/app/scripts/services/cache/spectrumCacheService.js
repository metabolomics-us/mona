/**
 * Created by sajjan on 8/21/14.
 *
 * Stores spectra browser data, individual spectrum, and query data for
 * persistence between controllers and views
 */

(function() {
    'use strict';
    angular.module('moaClientApp')
      .factory('SpectrumCache', ['$log', '$window', function($log, $window) {
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
           * Clear all values stored in this cache factory
           */
          this.clear = function() {
              this.removeBrowserSpectra();
              this.removeBrowserLocation();
              this.removeSpectrum();
          };


          this.hasBrowserSpectra = function() {
              return this.browserSpectra !== null;
          };
          this.getBrowserSpectra = function() {
              return this.browserSpectra;
          };
          this.setBrowserSpectra = function(browserSpectra) {
              this.browserSpectraScroll = $(window).scrollTop();
              this.browserSpectra = browserSpectra;
          };
          this.removeBrowserSpectra = function() {
              this.browserSpectraScroll = null;
              this.browserSpectra = null;
          };

          this.getBrowserSpectraScrollLocation = function() {
              return this.browserSpectraScroll;
          };


          this.hasBrowserLocation = function() {
              return this.browserLocation !== null;
          };
          this.getBrowserLocation = function() {
              return this.browserLocation;
          };
          this.setBrowserLocation = function(browserLocation) {
              this.browserLocation = browserLocation;
          };
          this.removeBrowserLocation = function() {
              this.browserLocation = null;
          };


          this.hasSpectrum = function() {
              return this.spectrum !== null;
          };
          this.getSpectrum = function() {
              return this.spectrum;
          };
          this.setSpectrum = function(spectrum) {
              this.spectrum = spectrum;
          };
          this.removeSpectrum = function() {
              this.spectrum = null;
          };
      }]);
})();