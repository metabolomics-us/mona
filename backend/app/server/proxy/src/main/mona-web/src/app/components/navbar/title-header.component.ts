/**
 * Updated by nolanguzman on 10/31/2021
 * Component to render Header for our navbar
 */

import {Component} from '@angular/core';
import {environment} from '../../../environments/environment';

@Component({
    selector: 'title-header',
    templateUrl: '../../views/navbar/titleHeader.html'
})
export class TitleHeaderComponent {
    env = environment;
    constructor() {}
}
