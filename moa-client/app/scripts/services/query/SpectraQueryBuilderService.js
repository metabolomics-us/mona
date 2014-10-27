/**
 * Created by wohlgemuth on 7/10/14.
 */

/**
 * a service to build our specific query object to be executed against the Spectrum service
 */
app.service('SpectraQueryBuilderService', function () {
    /**
     * updates a pre-compiled query with the given
     */
    this.updateQuery = function (query, metadata, tags, compiled) {
        // Build query object
        if(typeof compiled == 'undefined')
            compiled = {};

        if(!compiled.hasOwnProperty('compound'))
            compiled.compound = {};

        if(!compiled.hasOwnProperty('metadata'))
            compiled.metadata = [];

        if(!compiled.hasOwnProperty('tags'))
            compiled.tags = [];


        // Get all metadata in a single dictionary
        var meta = {};
        Object.keys(metadata).forEach(function (element, index, array) {
            for (var i = 0; i < metadata[element].length; i++) {
                meta[metadata[element][i].name] = metadata[element][i];
            }
        });


        // Handle all query components
        Object.keys(query).forEach(function (element, index, array) {
            if (element === "nameFilter" && query[element]) {
                compiled.compound.name = {like: query[element]};
            }

            else if (element === "inchiFilter" && query[element]) {
                if (/^([A-Z]{14}-[A-Z]{10}-[A-Z,0-9])+$/.test(query[element])) {
                    compiled.compound.inchiKey = {eq: query[element]};
                } else {
                    compiled.compound.inchiKey = {like: '%' + query[element] + '%'};
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
                            compiled.metadata.push({name: element, value: {between: [min, max]}});
                        } else
                            compiled.metadata.push({name: element, value: {eq: parseFloat(query[element])}});
                    } else {
                        compiled.metadata.push({name: element, value: {eq: query[element]}});
                    }
                }
            }
        });


        // Add all tags to query
        for(var i = 0; i < tags.length; i++) {
            compiled.tags.push(tags[i]);
        }


        return compiled;
    };

    /**
     * compiles our dedicated query to execute it against another service
     * @param element
     */
    this.compileQuery = function (query, metadata, tags) {
        return this.updateQuery(query, metadata, tags, {});
    };
});