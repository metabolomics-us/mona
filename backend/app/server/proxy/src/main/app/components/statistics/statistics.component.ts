/**
 * Created by Gert on 5/28/2014.
 */
import * as angular from 'angular';

class StatisticsController{
    private static $injector = ['$scope', 'statistics'];
    private $scope;
    private statistics;
    private data;

    constructor($scope, statistics){
        this.$scope = $scope;
        this.statistics = statistics;
    }

    $onInit = () => {
        this.data = this.statistics;
    }
}

let StatisticsComponent = {
    selector: "statistics",
    bindings: {},
    controller: StatisticsController
}

angular.module('moaClientApp')
    .component(StatisticsComponent.selector, StatisticsComponent);

