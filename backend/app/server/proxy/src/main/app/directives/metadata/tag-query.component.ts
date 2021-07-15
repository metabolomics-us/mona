/**
 * Executes a tag query
 */

import {SpectraQueryBuilderService} from "../../services/query/spectra-query-builder.service";
import {Inject, Input} from "@angular/core";
import {Component} from "@angular/core";
import {downgradeComponent} from "@angular/upgrade/static";
import * as angular from 'angular';

@Component({
    selector: 'tag-query',
    templateUrl: '../../views/templates/query/tagQuery.html'
})
export class TagQueryComponent {
    @Input() private ruleBased;
    @Input() private type;
    @Input() private tag;

    constructor(@Inject(SpectraQueryBuilderService) private spectraQueryBuilderService) {}

    /**
     * Create a new query based on the selected tag value
     */
    newQuery = () => {
        this.spectraQueryBuilderService.prepareQuery();
        this.addToQuery();
    };

    /**
     * Add selected tag value to the current query
     */
    addToQuery = () => {
        if (typeof this.type !== 'undefined' && this.type == 'compound') {
            this.spectraQueryBuilderService.addCompoundTagToQuery(this.tag.text, undefined);
        } else {
            this.spectraQueryBuilderService.addTagToQuery(this.tag.text, undefined);
        }

        this.spectraQueryBuilderService.executeQuery();
    };

}

angular.module('moaClientApp')
    .directive('tagQuery', downgradeComponent({
        component: TagQueryComponent,
        inputs: ['ruleBased', 'type', 'tag']
    }));
