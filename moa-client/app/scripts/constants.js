/**
 * Number of spectra/compounds to view
 * 7 fills out a screen nicely and has a decent response time at the same time
 * Toggle for whether commonly used data (tags, metadata, etc) should be
 * internally cached
 */

(function() {
    angular.module('moaClientApp')
      .constant('INTERNAL_CACHING', true)
      .constant('MAX_SPECTRA', 7)
      .constant('MAX_COMPOUNDS', 20)
      .constant('MAX_OBJECTS', 20);
})();