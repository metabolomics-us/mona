/**
 * simple directive to help populating the type ahead views on focis
 */
import {Directive, Renderer2, ElementRef, Inject, OnInit, HostListener, Component} from "@angular/core";
import * as angular from 'angular';
import {downgradeComponent} from "@angular/upgrade/static";

@Component({
    selector: 'typeahead-focus',
    template: ''
})
export class TypeaheadFocusComponent {
    constructor(@Inject(ElementRef) private elementRef: ElementRef, @Inject(Renderer2) private render: Renderer2) {}

    @HostListener('click', ['$event.target'])
    onClick(btn) {
        let viewValue = this.elementRef.nativeElement.value;

        if(this.elementRef.nativeElement.value === ' ') {
            this.elementRef.nativeElement.value = null;
        }

        this.elementRef.nativeElement.value = ' ';
        this.elementRef.nativeElement.value = (viewValue || ' ');
    }

    emptyOrMatch = (actual, expected) => {
        if (expected === ' ') {
            return true;
        }
        return actual.indexOf(expected) > -1;
    };
}

angular.module('moaClientApp')
    .directive('typeaheadFocus', downgradeComponent({
        component: TypeaheadFocusComponent
    }));



