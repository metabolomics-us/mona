/**
 * Created by wohlgemuth on 10/16/14.
 */

import {NGXLogger} from "ngx-logger";
import {ErrorHandleComponent} from "./error-handle.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {Component, Inject, Input} from "@angular/core";
import {downgradeComponent} from "@angular/upgrade/static";
import * as angular from 'angular';

@Component({
    selector: 'displayCompoundInfo',
    templateUrl: '../../views/compounds/displayCompound.html'
})
export class DisplayCompoundComponent{
    @Input() private compound;
    private pictureId;
    private chemId;
    private classifications;
    private showClassyFireInfo;
    private metaData: any;

    constructor(@Inject([NgbModal, NGXLogger]) private modalService: NgbModal, private logger: NGXLogger){}

    $onInit = () => {
        //calculate some unique id for the compound picture
        this.pictureId = Math.floor(Math.random() * 100000);
        this.chemId = Math.floor(Math.random() * 100000);
        this.showClassyFireInfo = true;

        // Build compound classification tree
        setTimeout(() => {
            this.classifications = [];

            if (this.compound.hasOwnProperty('classification')) {
                // Get high order classifications
                let classes = ['kingdom', 'superclass', 'class', 'subclass']
                    .map((value) => {
                        let filteredData = this.compound.classification.filter((x) => {
                            return x.name === value;
                        });
                        return filteredData.length > 0 ? filteredData[0] : null;
                    }).filter((x) => {
                        return x != null;
                    });

                // Get intermediate classifications
                let intermediate_parents = this.compound.classification
                    .filter((x) => {
                        return x.name.indexOf('direct parent level') === 0;
                    })
                    .map((x, i) => {
                        x.name = 'intermediate parent ' + (i + 1);
                        return x;
                    });

                classes = classes.concat(intermediate_parents);

                // Get parent classes
                let direct_parent = this.compound.classification.filter((x) => {
                    return x.name === 'direct parent';
                });
                // var alternate_parents = $scope.compound.classification.filter(function (x) {
                //     return x.name === 'alternative parent';
                // });

                // var parents = direct_parent.concat(alternate_parents);

                // Build tree
                if (classes.length > 0) {
                    for (let i = classes.length - 1; i >= 0; i--) {
                        if (i === classes.length - 1) {
                            classes[i].nodes = direct_parent;
                        } else {
                            classes[i].nodes = [classes[i + 1]];
                        }
                    }

                    this.classifications.push(classes[0]);
                }
            }
        });
    }

    /**
     * Emulate the downloading of a file given its contents and name
     * @param data
     * @param filetype
     * @param mimetype
     */
    downloadData = (data, filetype, mimetype) => {
        // Identify and sanitize filename
        let inchikeys = this.compound.metaData.filter((x) => {
            return x.name === 'InChIKey';
        });

        let filename = (inchikeys.length > 0) ? inchikeys[0].value :
            angular.isDefined(this.compound.inchiKey) ? this.compound.inchiKey : this.compound.names[0].name;

        filename = filename.replace(/[^a-z0-9\-]/gi, '_');

        // Emulate download
        let hiddenElement = document.createElement('a');

        hiddenElement.href = 'data:'+ mimetype +',' + encodeURI(data);
        hiddenElement.target = '_blank';
        hiddenElement.download = filename +'.'+ filetype;

        document.body.appendChild(hiddenElement);
        hiddenElement.click();
        document.body.removeChild(hiddenElement);
    };

    downloadAsMOL = () => {
        let modalRef;
        if (angular.isDefined(this.compound.molFile)) {
            this.downloadData(this.compound.molFile, 'mol', 'chemical/x-mdl-molfile');
        } else {
            modalRef = this.modalService.open(ErrorHandleComponent);
        }
    };

    downloadAsJSON = () => {
        this.downloadData(JSON.stringify(this.compound), 'json', 'application/json');
    };



}

angular.module('moaClientApp')
    .directive('displayCompoundInfo', downgradeComponent({
        component: DisplayCompoundComponent,
        inputs: ['compound']
    }));


