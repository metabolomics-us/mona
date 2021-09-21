/**
 * Updated by nolanguzman on 10/31/2021
 * Component to render our Resources drop down menu
 */

import {Component} from '@angular/core';
import {faQuestionCircle, faCaretDown} from '@fortawesome/free-solid-svg-icons';

@Component({
    selector: 'resource-drop-down',
    templateUrl: '../../views/navbar/resDropdown.html'
})
export class ResourceDropDownComponent {
    faQuestionCircle = faQuestionCircle;
    faCaretDown = faCaretDown;
    constructor() {}
}
