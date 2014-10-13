/**
 * Created by wohlgemuth on 7/10/14.
 */

/**
 * a service to build our specific query object to be executed against the Spectrum service
 */
app.service('SpectraQueryBuilderService', function ($log) {
    /**
     * updates a pre-compiled query with the given
     */
    this.updateQuery = function (query, metadata, tags, compiledQuery) {

    };

    /**
     * compiles our dedicated query to execute it against another service
     * @param element
     */
    this.compileQuery = function (query, metadata, tags) {
        var compiled = {};

        // Build individual criteria
        var compound = {};
        var metaData = [];


        // Get all metadata in a single dictionary
        var meta = {};
        Object.keys(metadata).forEach(function (element, index, array) {
            for (var i = 0; i < metadata[element].length; i++) {
                meta[metadata[element][i].name] = metadata[element][i];
            }
        });


        Object.keys(query).forEach(function (element, index, array) {
            if (element === "nameFilter" && query[element]) {
                compound.name = {like: query[element]};
            }

            else if (element === "inchiFilter" && query[element]) {
                if (/^([A-Z]{14}-[A-Z]{10}-[A-Z,0-9])+$/.test(query[element])) {
                    compound.inchiKey = {eq: query[element]};
                }
                else {
                    compound.inchiKey = {like: '%' + query[element] + '%'};
                }
            }

            // Ignore tolerance values
            else if (element.indexOf("_tolerance", element.length - 10) !== -1) {
                //nothing to see here
            }
            else {
                if (query[element]) {
                    if (meta[element].type === "double") {
                        if ((element + "_tolerance") in query && query[element + "_tolerance"]) {
                            var min = parseFloat(query[element]) - parseFloat(query[element + "_tolerance"]);
                            var max = parseFloat(query[element]) + parseFloat(query[element + "_tolerance"]);
                            metaData.push({name: element, value: {between: [min, max]}});
                        } else
                            metaData.push({name: element, value: {eq: parseFloat(query[element])}});
                    } else {
                        metaData.push({name: element, value: {eq: query[element]}});
                    }
                }
            }
        });

        // Build query
        if (compound) {
            compiled.compound = compound;
        }
        if (metaData) {
            compiled.metadata = metaData;
        }
        if (tags.length) {
            compiled.tags = tags;
        }

        return compiled;
    }
});