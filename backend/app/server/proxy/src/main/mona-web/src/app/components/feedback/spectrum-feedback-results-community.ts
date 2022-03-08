import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {faAngleDown, faAngleRight} from '@fortawesome/free-solid-svg-icons';
import {Feedback} from '../../services/persistence/feedback.resource';
import * as d3 from 'd3';
import 'nvd3';

@Component({
  selector: 'spectrum-feedback-results-community',
  templateUrl: '../../views/templates/feedback/spectrumFeedbackResultsCommunity.html'
})

export class SpectrumFeedbackResultsCommunityComponent implements OnInit, OnChanges {
  @Input() spectrumID;
  @Input() currentFeedback;
  feedbackResults;
  feedbackResultsChartOptions;
  totalReviews;
  faAngleDown = faAngleDown;
  faAngleRight = faAngleRight;

  constructor(public feedback: Feedback) {}

  ngOnInit() {
    this.totalReviews = 0;
    this.feedbackResults = [];
    this.feedbackResultsChartOptions = {
      chart: {
        type: 'discreteBarChart',
        preserveAspectRatio: 'xMinYMin meet',
        height: 500,
        duration: 1000,
        margin: {
          left: 90
        },
        x: (d) => {
          return d.label;
        },
        y: (d) => {
          return d.value;
        },
        valueFormat: (d) => {
          return d3.format(',.4f')(d);
        },
        xAxis: {
          axisLabel: 'Feedback'
        },
        yAxis: {
          axisLabel: 'Percent of Users',
          tickFormat: (d) => {
            return d3.format('%')(d);
          },
          showMaxMin: false

        },
        yDomain: [0, 1],
        showLabels: true
      }
    };
    this.createChart();
  }
  ngOnChanges(changes: SimpleChanges) {
    this.createChart();
  }

  createChart() {
    this.feedbackResults = [];
    if (this.currentFeedback.length !== 0) {
      let noisyCount = 0;
      let cleanCount = 0;
      let totalCount = 0;
      this.currentFeedback.forEach((x: any) => {
        totalCount++;
        if (x.value === 'noisy') {
          noisyCount++;
        } else if (x.value === 'clean') {
          cleanCount++;
        }
      });
      this.totalReviews = totalCount;
      this.feedbackResults = [{
        key: 'Community Spectrum Feedback Ratings',
        values: [
          {
            label: 'Noisy',
            value: (noisyCount / totalCount)
          },
          {
            label: 'Clean',
            value: (cleanCount / totalCount)
          }]
      }];
    }
  }
}
