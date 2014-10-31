/**
 * Created by wohlgemuth on 7/10/14.
 */

/**
 * a service to build our specific query object to be executed against the Spectrum service, mostly required for the modal query dialog and so kinda special
 *
 */
app.service('SpectraQueryBuilderService', function ($rootScope, $log) {

    /**
     * provides us with the current query
     * @returns {*|$rootScope.spectraQuery}
     */
    this.getQuery = function () {
        return $rootScope.getSpectraQuery();
    };

    /**
     * prepares an empty query to avoid null pointer exceptions
     */
    this.prepareQuery = function () {
        return {
            compound: {},
            metadata: [],
            tags: []
        };
    };
    /**
     * updates a pre-compiled query with the given
     */
    this.updateQuery = function (query, metadata, tags, compiled) {

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
        for (var i = 0; i < tags.length; i++) {
            compiled.tags.push(tags[i]);
        }


        return compiled;
    };

    /**
     * compiles our dedicated query to execute it against another service
     * @param element
     */
    this.compileQuery = function (query, metadata, tags) {
        return this.updateQuery(query, metadata, tags, this.prepareQuery());
    };

    /**
     * adds a tag to the query
     * @param tag
     */
    this.addTagToQuery = function (tag) {
        var query = this.getQuery();

        query.tags.push(tag);

        $rootScope.setSpectraQuery(query);

    };

    /**
     * removes a tag from a query
     * @param tag
     */
    this.removeTagFromQuery = function (tag) {

        var query = this.getQuery();
        query.tags.splice(query.tags.indexOf(tag), 1);

        $rootScope.setSpectraQuery(query);

    };

    /**
     * adds furhter metadata to the query
     * @param metadata
     */
    this.addMetaDataToQuery = function (metadata){

        $log.info(metadata);
    };

    /**
     * removes metadata from teh query
     * @param metadata
     */
    this.removeMetaDataFromQuery = function (metadata){

    }
});