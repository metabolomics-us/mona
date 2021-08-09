/*
 * Component to render our Browse drop down menu
 */

import {SpectraQueryBuilderService} from "../../services/query/spectra-query-builder.service";
import {Component} from "@angular/core";

@Component({
    selector: 'browse-drop-down',
    templateUrl: '../../views/navbar/browseDropdown.html'
})
export class BrowseDropDownComponent {

    constructor(public spectraQueryBuilderService: SpectraQueryBuilderService) {}

    // Reset query when user click browse
    resetQuery = () => {
        this.spectraQueryBuilderService.prepareQuery();
    }
}
