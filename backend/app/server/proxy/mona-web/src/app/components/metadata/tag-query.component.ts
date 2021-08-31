/**
 * Executes a tag query
 */

import {SpectraQueryBuilderService} from '../../services/query/spectra-query-builder.service';
import {Input} from '@angular/core';
import {Component} from '@angular/core';

@Component({
    selector: 'tag-query',
    templateUrl: '../../views/templates/query/tagQuery.html'
})
export class TagQueryComponent {
    @Input() public ruleBased;
    @Input() public type;
    @Input() public tag;

    constructor( public spectraQueryBuilderService: SpectraQueryBuilderService) {}

    /**
     * Create a new query based on the selected tag value
     */
    newQuery = () => {
        this.spectraQueryBuilderService.prepareQuery();
        this.addToQuery();
    }

    /**
     * Add selected tag value to the current query
     */
    addToQuery = () => {
        if (typeof this.type !== 'undefined' && this.type === 'compound') {
            this.spectraQueryBuilderService.addCompoundTagToQuery(this.tag.text, undefined);
        } else {
            this.spectraQueryBuilderService.addTagToQuery(this.tag.text, undefined);
        }

        this.spectraQueryBuilderService.executeQuery();
    }

}
