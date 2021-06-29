/**
 * This factory will parse the query Modal object and returns an RSQL Query String
 * TODO DELETE
 */

import * as angular from 'angular';
import {Inject} from "@angular/core";
import {NGXLogger} from "ngx-logger";
import {downgradeInjectable} from "@angular/upgrade/static";
import {QueryStringHelper} from "./query-string-helper.service";
import {QueryCacheService} from "../cache/query-cache.service";

export class QueryStringBuilder{
    private defaultQuery;
    private queryStr;
    private operand;
    private compiled;
    private service;

    constructor(@Inject(QueryStringHelper) private qStrHelper: QueryStringHelper,
                @Inject(QueryCacheService) private queryCache: QueryCacheService, @Inject(NGXLogger) private logger: NGXLogger) {
        this.defaultQuery = '/rest/spectra';
        this.service = {
            buildQuery: this.buildQuery(),
            buildAdvanceQuery: this.buildAdvanceQuery(),
            updateQuery: this.updateQuery()

        };
    }


    /**
     * updates on the fly queries submitted by users
     */
     updateQuery = () => {
        // options: compound, metadata, tags
        let query = this.queryCache.getSpectraQuery(undefined);
        let compiled = [];
        let queryStr: any;

        // updates compound metadata
        if (angular.isDefined(query.compound.metadata) && query.compound.metadata.length > 0) {
            queryStr = this.qStrHelper.buildMetaString(query.compound.metadata, true);
            compiled.push(queryStr);
        }

        // updates metadata
        if (angular.isDefined(query.metadata) && query.metadata.length > 0) {
            queryStr = this.qStrHelper.buildMetaString(query.metadata, false);
            compiled.push('and', queryStr);
        }

        this.saveQuery();
    }

    /**
     * builds a queryString when user submit keywordFilter form
     * @return rsql query string
     */
     buildQuery = () => {
        let query = this.queryCache.getSpectraQuery(undefined);
        let compiled = [];
        let queryStr: any;

        this.validateOperands(query);

        // build compound string
        if (angular.isDefined(query.compound) && query.compound.length > 0) {
            compiled.push(this.qStrHelper.buildCompoundString(query.compound));
        }

        // build compound metadata string
        let operand = query.operand.shift();
        if (angular.isDefined(query.compoundDa)) {
            // queryStr = qStrHelper.buildMeasurementString(query.compoundDa);
            let leftOffset = query.compoundDa.mass - query.compoundDa.tolerance,
                rightOffset = query.compoundDa.mass + query.compoundDa.tolerance;

            queryStr =  "compound.metaData=q='name==\"total exact mass\" and "+
                "value>=" + leftOffset + " and value<=" + rightOffset + "'";
            compiled.push(operand, queryStr);
        }

        // add formula
        operand = query.operand.shift();
        if (angular.isDefined(query.formula)) {
            queryStr = "compound.metaData=q='name==\"molecular formula\" and value=match=\".*" + query.formula + ".*\"'";
            compiled.push(operand, queryStr);
        }

        // add classification
        operand = query.operand.shift();
        if (angular.isDefined(query.classification)) {
            queryStr = "compound.classification=q='value=match=\".*" + query.classification + ".*\"'";
            compiled.push(operand, queryStr);
        }

        //build metadata filter string from search page
        if (angular.isDefined(query.groupMeta)) {
            queryStr = this.qStrHelper.addMetaFilterQueryString(query.groupMeta);
            if (queryStr !== '') {
                compiled.push('and', queryStr);
            }
        }

        this.saveQuery();
    }


    /**
     * builds a queryString when user submit advancedSearch form
     * @return rsql query string
     */
    buildAdvanceQuery = () => {
        let query = this.queryCache.getSpectraQuery(undefined);
        let compiled = [];
        let operand: any;
        let queryStr: any;

        // compound name, inchiKey and class
        operand = query.operand.compound.shift();
        if (angular.isDefined(query.compound) && query.compound.length > 0) {
            queryStr = this.qStrHelper.buildCompoundString(query.compound);

            // add user's selected operand
            let re = /or compound.classification/;
            let newStr = operand.concat(' compound.classification');
            queryStr = queryStr.replace(re, newStr);

            compiled.push(queryStr);
        }

        // add compound metadata
        operand = query.operand.compound.shift();
        if (angular.isDefined(query.compound.metadata) && query.compound.metadata.length > 0) {
            queryStr = this.qStrHelper.buildMetaString(query.compound.metadata, true);
            compiled.push(operand, queryStr);
        }

        operand = query.operand.compound.shift();
        if (angular.isDefined(query.compoundDa) && query.compoundDa.length > 0) {
            queryStr = this.qStrHelper.buildMeasurementString(query.compoundDa);
            compiled.push(operand, queryStr);
        }

        // add metadata query
        if (angular.isDefined(query.metadata) && query.metadata.length > 0) {
            queryStr = this.qStrHelper.buildMetaString(query.metadata, false);
            compiled.push('and', queryStr);
        }

        // add metadata measurement
        operand = query.operand.metadata.pop();
        if (angular.isDefined(query.metadataDa) && query.metadataDa.length > 0) {

        }


        this.saveQuery();
    }

    saveQuery() {
        // remove any leading operators
        let compiled: any;
        if (compiled[0] === 'and' || compiled[0] === 'or') {
            compiled.shift();
        }

        compiled = compiled.length > 0 ? compiled.join(' ') : this.defaultQuery;
        this.queryCache.setSpectraQueryString(compiled);
    }

    validateOperands(query) {
        if(!angular.isDefined(query.operand)) {
            throw new TypeError('query.operand property is undefined');
        }
    }

    buildTagsQueryString(tagQuery) {
        let queryString = "";
        for (let i = 0, l = tagQuery.length; i < l; i++) {
            if (i > 0) {
                queryString += ' and ';
            }
            queryString += "tags=q='name.eq==" + tagQuery[i].name.eq + '\"\'';

        }
        return queryString;
    }


}

angular.module('moaClientApp')
    .factory("queryStringBuilder", downgradeInjectable(QueryStringBuilder));
