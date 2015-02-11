/**
 * displays our spectra
 * @param $scope
 * @param $modalInstance
 * @param spectrum
 * @param massSpec
 * @constructor
 */
moaControllers.ViewSpectrumController = function ($scope, $location, $log, delayedSpectrum, CookieService, SpectraQueryBuilderService) {
    /**
     * Mass spectrum obtained from cache if it exists, otherwise from REST api
     */
    $scope.spectrum = delayedSpectrum;

    $scope.massSpec = [];

    /**
     * status of our accordion
     * @type {{isBiologicalCompoundOpen: boolean, isChemicalCompoundOpen: boolean, isDerivatizedCompoundOpen: boolean}}
     */
    $scope.accordionStatus = {
        isBiologicalCompoundOpen: CookieService.getBooleanValue("DisplaySpectraisBiologicalCompoundOpen", true),
        isChemicalCompoundOpen: CookieService.getBooleanValue("DisplaySpectraisChemicalCompoundOpen", false),
        isDerivatizedCompoundOpen: CookieService.getBooleanValue("DisplaySpectraisDerivatizedCompoundOpen", false),
        isSpectraOpen: CookieService.getBooleanValue("DisplaySpectraisSpectraOpen", true),
        isIonTableOpen: CookieService.getBooleanValue("DisplaySpectraisIonTableOpen", false)
    };

    /**
     * watch the accordion status and updates related cookies
     */
    $scope.$watch("accordionStatus", function(newVal) {
        angular.forEach($scope.accordionStatus, function(value, key) {
            CookieService.update("DisplaySpectra"+ key, value);
        });
    },true);


    /**
     * Sort order for the ion table - default m/z ascending
     */
    $scope.ionTableSort = 'ion';
    $scope.ionTableSortReverse = false;

    $scope.sortIonTable = function(column) {
        if (column == 'ion') {
            $scope.ionTableSortReverse = ($scope.ionTableSort == '+ion') ? !$scope.ionTableSortReverse : false;
            $scope.ionTableSort = '+ion';
        }

        else if (column == 'intensity') {
            $scope.ionTableSortReverse = ($scope.ionTableSort == '-intensity') ? !$scope.ionTableSortReverse : false;
            $scope.ionTableSort = '-intensity';
        }

        else if (column == 'annotation') {
            $scope.ionTableSortReverse = ($scope.ionTableSort == '-annotation') ? !$scope.ionTableSortReverse : false;
            $scope.ionTableSort = '-annotation';
        }
    };


    /**
     * Perform all initial data formatting and processing
     */
    (function () {
        // Regular expression for truncating accurate masses
        var massRegex = /^\s*(\d+\.\d{4})\d*\s*$/;

        /**
         * Decimal truncation routines
         */
        var truncateDecimal = function (s, length) {
            var regex = new RegExp("^\\s*(\\d+\\.\\d{" + length + "})\\d*\\s*$");
            var m = s.match(regex);

            return m != null ? m[1] : s;
        };

        /**
         * Truncate the
         */
        var truncateMass = function (mass) {
            return truncateDecimal(mass, 4);
        };

        var truncateRetentionTime = function (mass) {
            return truncateDecimal(mass, 1);
        };


        //
        // Truncate metadata mass values
        //

        for (var i = 0; i < delayedSpectrum.metaData.length; i++) {
            var name = delayedSpectrum.metaData[i].name.toLowerCase();

            if (name.indexOf('mass') > -1 || name.indexOf('m/z') > -1) {
                delayedSpectrum.metaData[i].value = truncateMass(delayedSpectrum.metaData[i].value);
            } else if (name.indexOf('retention') > -1) {
                delayedSpectrum.metaData[i].value = truncateRetentionTime(delayedSpectrum.metaData[i].value);
            }
        }

        for (var i = 0; i < delayedSpectrum.biologicalCompound.metaData.length; i++) {
            var name = delayedSpectrum.biologicalCompound.metaData[i].name.toLowerCase();

            if (name.indexOf('mass') > -1 || name.indexOf('m/z') > -1) {
                delayedSpectrum.biologicalCompound.metaData[i].value = truncateMass(delayedSpectrum.biologicalCompound.metaData[i].value);
            }
        }

        for (var i = 0; i < delayedSpectrum.chemicalCompound.metaData.length; i++) {
            var name = delayedSpectrum.chemicalCompound.metaData[i].name.toLowerCase();

            if (name.indexOf('mass') > -1 || name.indexOf('m/z') > -1) {
                delayedSpectrum.chemicalCompound.metaData[i].value = truncateMass(delayedSpectrum.chemicalCompound.metaData[i].value);
            }
        }


        //
        // Create mass spectrum table
        //

        // Regular expression to extract ions
        var ionRegex = /([0-9]*\.?[0-9]+)+:([0-9]*\.?[0-9]+)/g;

        // Assemble our annotation matrix
        var meta = [];

        for (var i = 0; i < delayedSpectrum.metaData.length; i++) {
            if (delayedSpectrum.metaData[i].category === 'annotation') {
                meta.push(delayedSpectrum.metaData[i]);
            }
        }

        // Parse spectrum string to generate ion list
        var match;

        while ((match = ionRegex.exec(delayedSpectrum.spectrum)) != null) {
            // Find annotation
            var annotation = '';
            var computed = false;

            for (var i = 0; i < meta.length; i++) {
                if (meta[i].value === match[1]) {
                    annotation = meta[i].name;
                    computed = meta[i].computed;
                }
            }

            // Truncate decimal values of m/z
            match[1] = truncateMass(match[1]);

            // Store ion
            $scope.massSpec.push({
                ion: parseFloat(match[1]),
                intensity:parseFloat(match[2]),
                annotation: annotation,
                computed: computed
            });
        }
    })();
};


/**
 * Required in order to load the spectrum before resolving the web page.
 * Loads spectrum from cache if it exists, otherwise get from rest api
 */
moaControllers.ViewSpectrumController.loadSpectrum = {
    delayedSpectrum: function (Spectrum, $route, SpectrumCache) {
        // If a spectrum is not cached or the id requested does not match the
        // cached spectrum, request it from the REST api
        if (!SpectrumCache.hasSpectrum() || SpectrumCache.getSpectrum().id != $route.current.params.id) {
            return Spectrum.get(
                {id: $route.current.params.id},
                function (data) {
                },
                function (error) {
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
};