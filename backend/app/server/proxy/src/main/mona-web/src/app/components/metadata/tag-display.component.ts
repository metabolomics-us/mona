/**
 * Updated by nolanguzman on 10/31/2021
 * a directive to display our tags and keep track of selections/deselections
 */
import {TagService} from '../../services/persistence/tag.resource';
import {NGXLogger} from 'ngx-logger';
import {Component, Input, OnInit} from '@angular/core';
import {faFlask} from '@fortawesome/free-solid-svg-icons';

@Component({
    selector: 'tag-display',
    templateUrl: '../../views/templates/query/tagDisplay.html'
})
export class TagDisplayComponent implements OnInit{
    maxTagsCount;
    @Input() tags;
    faFlask = faFlask;

    constructor( public logger: NGXLogger,   public tagService: TagService) {}

    ngOnInit() {
        setTimeout(() => {
            this.maxTagsCount = 0;

            if (typeof this.tags === 'undefined') {
                this.tags = [];

                this.tagService.query().subscribe(
                    (data: any) => {
                        this.tags = data;

                        for (let i = 0; i < data.length; i++) {
                            if (data[i].count > this.maxTagsCount) {
                              this.maxTagsCount = data[i].count;
                            }
                        }
                    },
                    (error) => {
                        this.logger.error('Tag pull failed: ' + error);
                    }
                );
            } else {
                for (let i = 0; i < this.tags.length; i++) {
                    if (this.tags[i].count > this.maxTagsCount) {
                        this.maxTagsCount = this.tags[i].count;
                    }
                }
            }
        });
    }

    tagClass(tag) {
        const tagClass = [];

        // Button color based on selection
        if (tag.selected === '+') {
            tagClass.push('btn-success');
        } else if (tag.selected === '-') {
            tagClass.push('btn-danger');
        } else {
            tagClass.push('btn-default');
        }

        // Button size based on count
        if (this.maxTagsCount > 0) {
            if (tag.count / this.maxTagsCount < 0.25) {
                tagClass.push('btn-xs');
            } else if (tag.count / this.maxTagsCount < 0.5) {
                tagClass.push('btn-sm');
            } else if (tag.count / this.maxTagsCount > 0.75) {
                tagClass.push('btn-lg');
            }
        }

        return tagClass;
    }

}
