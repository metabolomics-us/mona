app.filter('compoundsQuery', function() {
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

    function checkCompound(compound, scope) {
        // Name filter
        var names = Object.keys(scope.nameFilters);


        var matchedAny = names.some(function(element, index, array) {
            for(var i = 0; i < compound.names.length; i++)
                if(compound.names[i].name.indexOf(element) > -1)
                    return true;

            return false;
        });

        if(names.length > 0 && !matchedAny)
            return false;


        // Full InChIKey filter
        var inchikeys = Object.keys(scope.inchiFilters);

        if(inchikeys.length > 0 && inchikeys.indexOf(compound.inchiKey) == -1)
            return false;


        // Partial InChIKey filter
        inchikeys = Object.keys(scope.partialInchiFilters);

        matchedAny = inchikeys.some(function(element, index, array) {
            if(compound.inchiKey.indexOf(element) > -1)
                return true;
            return false;
        });

        if(inchikeys.length > 0 && !matchedAny)
            return false;


        return true;
    };

    return function(compounds, scope) {
        var out = [];

        for(var i = 0; i < compounds.length; i++)
            if(checkCompound(compounds[i], scope))
                out.push(compounds[i]);

        return out;
    }
});