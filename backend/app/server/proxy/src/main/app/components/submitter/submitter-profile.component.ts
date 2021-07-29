/**
 * Created by sajjan on 4/24/15.
 */
import {AuthenticationService} from "../../services/authentication.service";
import {SpectraQueryBuilderService} from "../../services/query/spectra-query-builder.service";
import {Component, Inject, OnInit} from "@angular/core";
import {downgradeComponent} from "@angular/upgrade/static";
import {User} from "../../mocks/user.model";
import * as angular from 'angular';

@Component({
    selector: 'submitter-profile',
    templateUrl: '../../views/submitters/profile.html'
})
export class SubmitterProfileComponent implements OnInit{
    private user: User;

    constructor(@Inject(AuthenticationService) private authenticationService: AuthenticationService,
                @Inject(SpectraQueryBuilderService) private spectraQueryBuilderService: SpectraQueryBuilderService) {}

    ngOnInit() {
        //this.$scope.$on('auth:login-success', this.setUserData);
        this.authenticationService.isAuthenticated.subscribe((data) => {
            this.setUserData();
        });

        this.setUserData();
    }

     setUserData = () => {
         this.user = this.authenticationService.getCurrentUser();
     }

    /**
     * Executes a new query based on username
     */
    queryUserSpectra = () => {
        this.spectraQueryBuilderService.prepareQuery();
        this.spectraQueryBuilderService.addUserToQuery(this.user.emailAddress);
        this.spectraQueryBuilderService.executeQuery();
    };

}

angular.module('moaClientApp')
    .directive('submitterProfile', downgradeComponent({
        component: SubmitterProfileComponent
    }));

