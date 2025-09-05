/**
 * Updated by nolanguzman on 10/31/2021
 * provides top users scores
 */

import {Component, Input, OnInit} from '@angular/core';
import {Statistics} from '../../services/persistence/statistics.resource';
import {faTrophy, faSpinner} from '@fortawesome/free-solid-svg-icons';
import {faStar, faStarHalfAlt} from '@fortawesome/free-solid-svg-icons';
import {faStar as faStarEmpty } from '@fortawesome/free-regular-svg-icons';

@Component({
    selector: 'spectra-top-scores-for-users',
    templateUrl: '../../views/templates/scores/hallOfFame.html'
})
export class SpectraTopScoresForUsersComponent implements OnInit{
    @Input() limit;
    scores;
    faTrophy = faTrophy;
    faSpinner = faSpinner;
    faStar = faStar;
    faStarEmpty = faStarEmpty;
    faStarHalf = faStarHalfAlt;

    constructor( public statistics: Statistics) {}

    ngOnInit() {
        this.statistics.spectraTopScores().subscribe((data) => {
            this.scores = data;
            // Score adjustment, -0.45 from each score, commented out 8/21/25
            // this.scores.forEach((x) => {
            //     x.score -= 0.45;
            // });

            // ---===Submitters ordered by count instead of score (with score of at least 3.0)===---
            // Filter out submitters with scores less than 3.0
            const filtered = data.filter(x => x.score >= 3.0);

            // Sort by count
            filtered.sort((a, b) => b.count - a.count);

            // Assign the sorted array to scores
            this.scores = filtered;
        });
    }

    stars(submitter) {
      const result: (0 | 0.5 | 1)[] = [];
      let rounded = Math.round(submitter.score * 2) / 2; // round to nearest 0.5
      for (let i = 0; i < 5; i++) {
        if (rounded >= 1) {
          result.push(1);
        } else if (rounded === 0.5) {
          result.push(0.5);
        } else {
          result.push(0);
        }
        rounded -= 1;
      }
      return result;
    }
}
