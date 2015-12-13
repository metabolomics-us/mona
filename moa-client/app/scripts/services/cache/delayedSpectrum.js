/*
 * a factory that returns cache spectrum, if any.
 * Usage: factory is resolved in route.js, which injects the dependency to our
 * ViewSpectrumController
 */

(function() {
    'use strict';

    angular.module('moaClientApp')
      .factory('delayedSpectrum', delayedSpectrumService);

    function delayedSpectrumService() {

        var service = {
            delayedSpectrum: delayedSpectrum
        };

        return service;
    }


    // If a spectrum is not cached or the id requested does not match the
    // cached spectrum, request it from the REST api
    delayedSpectrumService.$inject = ['Spectrum', '$route', 'SpectrumCache'];

    function delayedSpectrum(Spectrum, $route, SpectrumCache) {

        if (!SpectrumCache.hasSpectrum() || SpectrumCache.getSpectrum().id !== $route.current.params.id) {
            return Spectrum.get(
              {id: $route.current.params.id},
              function(data) {
              },
              function(error) {
                  alert('failed to obtain spectrum: ' + error);
              }
            ).$promise;
        }

        else {
            var spectrum = SpectrumCache.getSpectrum();
            SpectrumCache.removeSpectrum();
            return spectrum;
        }

    }

})();
