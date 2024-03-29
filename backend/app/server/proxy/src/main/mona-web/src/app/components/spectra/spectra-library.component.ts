/**
 * Created by sajjan on 3/2/16.
 * Updated by nolanguzman on 10/31/2021
 */

import {Component, Input, AfterViewInit, ElementRef} from '@angular/core';
import {SpectrumModel} from "../../mocks/spectrum.model";
import {Library} from "../../mocks/library.model";

@Component({
    selector: 'display-library-reference',
    template: '<div #libraryBinder></div>'
})
export class SpectraLibraryComponent implements AfterViewInit {
    @Input() spectrum: SpectrumModel;
    libraryString;

    constructor( public elementRef: ElementRef) {}

    ngAfterViewInit() {
        // Empty string if no library object exists
        if (!this.spectrum.library || !this.spectrum.library.description || this.spectrum.library.description === '') {
            this.libraryString = '';
            return;
        }

        // Base library string
        this.libraryString = 'Originally submitted to the ';

        const library: Library = this.spectrum.library;

        // Handle a provided library link
        if (typeof library.link !== 'undefined' && library.link !== '') {
            // Link to library but no identifier
            if (typeof library.library === 'undefined') {
                this.libraryString += '<a href="' + library.link + '" target="_blank">' +
                    library.description + ' </a>';
            }

            // Link to library and identifier and link placeholder for identifier
            else if (typeof library.library !== 'undefined' && library.link.indexOf('%s') > -1) {
                const link = library.link.replace('%s', library.library);

                this.libraryString += this.spectrum.library.description + ' as <a href="' + link + '" target="_blank">' +
                    library.library + '</a>';
            }

            // Link to library and identifier but no link placeholder for identifier
            else {
                this.libraryString += '<a href="' + library.link + '" target="_blank">' + library.description +
                    '</a> as ' + library.library;
            }
        }

        // With no library link
        else {
            this.libraryString += library.description;
        }
        this.elementRef.nativeElement.innerHTML = this.libraryString;
    }
}
