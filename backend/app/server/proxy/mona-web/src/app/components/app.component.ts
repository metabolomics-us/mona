import { Component } from '@angular/core';

@Component({
    selector: 'app-mona',
    template: `<div id="wrapper">
        <nav class="navbar navbar-expand-xl fixed-top navbar-dark">
            <!-- navbar dropdowns -->
            <title-header></title-header>
            <div class="collapse navbar-collapse" role="navigation">
                <ul class="nav navbar-nav">
                    <browse-drop-down></browse-drop-down>
                    <admin-drop-down></admin-drop-down>
                    <download-button></download-button>
                    <upload-button></upload-button>
                    <resource-drop-down></resource-drop-down>
                </ul>
                <authentication></authentication>
                <search-box></search-box>
            </div>
        </nav>
        <div id="page-wrapper">
            <div class="row">
                <div class="col-lg-12 top17">
                    <router-outlet></router-outlet>
                </div>
            </div>
        </div>
    </div>`
})
export class AppRootComponent {}
