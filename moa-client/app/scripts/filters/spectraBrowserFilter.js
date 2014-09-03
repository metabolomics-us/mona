app.filter('spectraQuery', function() {
    function intersect(arr1, arr2) {
        var r = [], o = {}, l = arr2.length, i, v;

        for (i = 0; i < l; i++)
            o[arr2[i]] = true;

        l = arr1.length;

        for (i = 0; i < l; i++) {
            v = arr1[i];
            if (v in o)
                r.push(v);
        }
        return r;
    }

    function checkSpectrum(spectrum, scope) {
        // Name filter
        var names = Object.keys(scope.nameFilters);

        var matchedAny = names.some(function(element, index, array) {
            for(var i = 0; i < spectrum.biologicalCompound.names.length; i++)
                if(spectrum.biologicalCompound.names[i].name.indexOf(element) > -1)
                    return true;

            for(var i = 0; i < spectrum.chemicalCompound.names.length; i++)
                if(spectrum.chemicalCompound.names[i].name.indexOf(element) > -1)
                    return true;

            return false;
        });

        if(names.length > 0 && !matchedAny)
            return false;


        // Full InChIKey filter
        var inchikeys = Object.keys(scope.inchiFilters);

        if(inchikeys.length > 0 &&
            inchikeys.indexOf(spectrum.biologicalCompound.inchiKey) == -1 &&
            inchikeys.indexOf(spectrum.chemicalCompound.inchiKey) == -1)
            return false;


        // Partial InChIKey filter
        inchikeys = Object.keys(scope.partialInchiFilters);

        matchedAny = inchikeys.some(function(element, index, array) {
            if(spectrum.biologicalCompound.inchiKey.indexOf(element) > -1 ||
                spectrum.chemicalCompound.inchiKey.indexOf(element) > -1)
                return true;
            return false;
        });

        if(inchikeys.length > 0 && !matchedAny)
            return false;


        // Tags filter
        var tags = spectrum.tags.map(function(tag) { return tag.text; });
        var selected_tags = scope.tagsSelection.map(function(tag) { return tag.text; });

        if(intersect(tags, selected_tags).length != selected_tags.length)
            return false;

        return true;
    };

    return function(spectra, scope) {
        if(spectra == null) {
            return [];
        } else {
            var out = [];

            for (var i = 0; i < spectra.length; i++)
                if (checkSpectrum(spectra[i], scope))
                    out.push(spectra[i]);

            return out;
        }
    }
});