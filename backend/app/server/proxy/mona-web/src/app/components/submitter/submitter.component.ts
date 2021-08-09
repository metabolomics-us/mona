/**
 * Created by Gert on 5/28/2014.
 */

import {Submitter} from "../../services/persistence/submitter.resource";
import {AuthenticationService} from "../../services/authentication.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Component, OnInit} from "@angular/core";
import {first} from "rxjs/operators";
import {SubmitterModalComponent} from "./submitter-modal.component";

@Component({
    selector: 'submitter',
    templateUrl: '../../views/submitters/list.html'
})
export class SubmitterComponent implements OnInit{
    public submitters;
    public listSubmitter;

    constructor( public submitter: Submitter,  public modal: NgbModal,
                 public auth: AuthenticationService){}

    ngOnInit() {
        /**
         * contains all local objects
         * @type {Array}
         */
        this.submitters = [];

        /**
         * list all our submitters in the system
         */
        this.listSubmitter = this.list();
    }

    /**
     * deletes our submitter from the system
     * @param submitterId
     */
    remove = (index) => {
        let submitterToRemove = this.submitters[index];

        this.submitter.delete({id: submitterToRemove.id}).pipe(first()).subscribe(
            (data) => {
                //remove it from the scope and update our table
                this.submitters.splice(index, 1);
            },
            (errors) => {
                alert('Error Has Occurred while removing submitter');
            }
        );
    };

    /**
     * displays our dialog to create a new submitter
     */
    displayCreateDialog = () => {
        let modalInstance = this.modal.open(SubmitterModalComponent);
        modalInstance.componentInstance.new = true;

        //retrieve the result from the dialog and save it
        modalInstance.result.then((submitter) => {
            //push our object to the scope now so that our table can show it
            this.submitters.push(submitter);
        })
    };

    /**
     * displays the edit dialog for the select submitter
     * @param index
     */
    displayEditDialog = (index) => {
        let modalInstance = this.modal.open(SubmitterModalComponent, {
            size: 'lg',
            backdrop: 'static',
        });
        modalInstance.componentInstance.new = false;

        //retrieve the result from the dialog and save it
        modalInstance.result.then((submitter) => {
            //need to figure out how to resolve this one
        });
    };

    /**
     * helper function
     */
     list() {
        this.submitter.get().pipe(first()).subscribe((data) => {
            this.submitters = data;
        }, (error) => {
            alert('failed: ' + error);
        });
    }
}
