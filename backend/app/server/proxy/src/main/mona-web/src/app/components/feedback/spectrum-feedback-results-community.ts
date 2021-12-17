import {Component, Input, OnChanges, OnInit} from "@angular/core";
import {faAngleDown, faAngleRight} from "@fortawesome/free-solid-svg-icons";
import {Feedback} from "../../services/persistence/feedback.resource";
import * as d3 from 'd3';
import 'nvd3';

@Component({
  selector: 'spectrum-feedback-results-community',
  templateUrl: '../../views/templates/feedback/spectrumFeedbackResultsCommunity.html'
})

export class SpectrumFeedbackResultsCommunity implements OnInit {
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
        yDomain: [0,1],
        showLabels: true
      }
    }
    this.createChart();
  }

  createChart() {
    this.feedbackResults = [];
    if (this.currentFeedback.length !== 0) {
      let noisy_count = 0;
      let clean_count = 0;
      let total_count = 0;
      this.currentFeedback.forEach((x: any) => {
        total_count++;
        if (x.value === 'noisy') {
          noisy_count++;
        } else if (x.value === 'clean') {
          clean_count++;
        }
      });
      this.totalReviews = total_count;
      this.feedbackResults = [{
        key: 'Community Spectrum Feedback Ratings',
        values: [
          {
            "label": "Noisy",
            "value": (noisy_count/total_count)
          },
          {
            "label": "Clean",
            "value": (clean_count/total_count)
          }]
      }];
    }
  }
}
