/**
 * Updated by nolanguzman on 10/31/2021
 * Component to render our Browse drop down menu
 */

import {SpectraQueryBuilderService} from '../../services/query/spectra-query-builder.service';
import {Component} from '@angular/core';
import {faCaretDown, faChartBar} from '@fortawesome/free-solid-svg-icons';

@Component({
    selector: 'browse-drop-down',
    templateUrl: '../../views/navbar/browseDropdown.html'
})
export class BrowseDropDownComponent {
    faCaretDown = faCaretDown;
    faChartBar = faChartBar;
    constructor(public spectraQueryBuilderService: SpectraQueryBuilderService) {}

    // Reset query when user click browse
    resetQuery() {
        this.spectraQueryBuilderService.prepareQuery();
    }
}
