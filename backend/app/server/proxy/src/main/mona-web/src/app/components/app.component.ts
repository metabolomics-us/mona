import { Component, OnInit } from '@angular/core';
import {Router, NavigationEnd} from '@angular/router';

@Component({
    selector: 'app-mona',
    template: `<div id="wrapper">
        <nav class="navbar navbar-expand-xl fixed-top navbar-dark">
            <!-- navbar dropdowns -->
            <title-header></title-header>
            <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
              <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" role="navigation" id="navbarSupportedContent">
                <ul class="nav navbar-nav mr-auto">
                    <browse-drop-down class="nav-item"></browse-drop-down>
                    <admin-drop-down class="nav-item"></admin-drop-down>
                    <download-button class="nav-item"></download-button>
                    <upload-button class="nav-item"></upload-button>
                    <resource-drop-down class="nav-item"></resource-drop-down>
                </ul>
                <div class="form-inline my-2 my-lg-0">
                  <search-box class="nav-item"></search-box>
                </div>
                <div class="form-inline my-2 my-lg-0">
                  <authentication class="nav-item"></authentication>
                </div>
            </div>
        </nav>
        <p></p>
        <div id="page-wrapper">
            <div class="row">
                <div class="col-lg-12 top17">
                    <router-outlet></router-outlet>
                </div>
            </div>
        </div>
    </div>`
})
export class AppRootComponent implements OnInit{
  constructor(private router: Router) {}

  ngOnInit() {
    // scroll to top of page on new page entry
    this.router.events.subscribe((event) => {
      if (!(event instanceof NavigationEnd)) {
        return;
      }
      window.scrollTo(0, 0);
    });
  }
}
