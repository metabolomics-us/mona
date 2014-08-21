/**
 * Created by sajjan on 8/21/14.
 */

app.factory('SpectrumCache', function($cacheFactory) {
    return $cacheFactory('spectra');
});

app.factory('ScrollCache', function($cacheFactory) {
    return $cacheFactory('scroll');
});