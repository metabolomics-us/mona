/**
 * displays our spectra
 * @param $scope
 * @param $modalInstance
 * @param spectrum
 * @param massSpec
 * @constructor
 */
moaControllers.ViewSpectrumController = function ($scope, delayedSpectrum) {
    $scope.spectrum = delayedSpectrum;

    $scope.massSpec = [];



    /**
     * Decimal truncation routines
     */
    var truncateDecimal = function(s, length) {
        var regex = new RegExp("^\\s*(\\d+\\.\\d{"+ length +"})\\d*\\s*$");
        var m = s.match(regex);

        return m != null ? m[1] : s;
    };

    var truncateMass = function(mass) { return truncateDecimal(mass, 4);};
    var truncateRetentionTime = function(mass) { return truncateDecimal(mass, 1);};


    /*
     * Perform all initial data formatting and processing
     */
    (function(spectrum) {
        // Regular expression for truncating accurate masses
        var massRegex = /^\s*(\d+\.\d{4})\d*\s*$/;

        console.log(spectrum);


        //
        // Truncate metadata mass values
        //

        for (var i = 0; i < spectrum.metaData.length; i++) {
            var name = spectrum.metaData[i].name.toLowerCase();

            if (name.indexOf('mass') > -1 || name.indexOf('m/z') > -1) {
                spectrum.metaData[i].value = truncateMass(spectrum.metaData[i].value);
            } else if (name.indexOf('retention') > -1) {
                spectrum.metaData[i].value = truncateRetentionTime(spectrum.metaData[i].value);
            }
        }

        for (var i = 0; i < spectrum.biologicalCompound.metaData.length; i++) {
            var name = spectrum.biologicalCompound.metaData[i].name.toLowerCase();

            if (name.indexOf('mass') > -1 || name.indexOf('m/z') > -1) {
                spectrum.biologicalCompound.metaData[i].value = truncateMass(spectrum.biologicalCompound.metaData[i].value);
            }
        }

        for (var i = 0; i < spectrum.chemicalCompound.metaData.length; i++) {
            var name = spectrum.chemicalCompound.metaData[i].name.toLowerCase();

            if (name.indexOf('mass') > -1 || name.indexOf('m/z') > -1) {
                spectrum.chemicalCompound.metaData[i].value = truncateMass(spectrum.chemicalCompound.metaData[i].value);
            }
        }


        //
        // Create mass spectrum table
        //

        // Regular expression to extract ions
        var ionRegex = /([0-9]*\.?[0-9]+)+:([0-9]*\.?[0-9]+)/g;

        // Assemble our annotation matrix
        var meta = [];

        for (var i = 0; i < spectrum.metaData.length; i++) {
            if (spectrum.metaData[i].category === 'annotation') {
                meta.push(spectrum.metaData[i]);
            }
        }

        // Parse spectrum string to generate ion list
        var match;

        while ((match = ionRegex.exec(spectrum.spectrum)) != null) {
            // Find annotation
            var annotation;

            for (var i = 0; i < meta.length; i++) {
                if (meta[i].value === match[1]) {
                    annotation = meta[i].name;
                }
            }

            // Truncate decimal values of m/z
            match[1] = truncateMass(match[1]);

            // Store ion
            $scope.massSpec.push({ion: match[1], intensity: match[2], annotation: annotation});
        }
    })(delayedSpectrum);
};


/**
 * Required in order to load the spectrum before resolving the web page.
 * Loads spectrum from cache if it exists, otherwise get from rest api
 */
moaControllers.ViewSpectrumController.loadSpectrum = {
    delayedSpectrum: function(Spectrum, $route, SpectrumCache) {
        console.log('Spectrum')
        console.log(SpectrumCache.get('viewSpectrum'))
        if (SpectrumCache.get('viewSpectrum') === undefined) {
            return Spectrum.get(
                {id: $route.current.params.id},
                function (data) {
                },
                function (error) {
                    alert('failed to obtain spectrum: ' + error);
                }
            ).$promise
        } else {
            return SpectrumCache.get('viewSpectrum');
        }
    }
};