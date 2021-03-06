import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/feeds/define-feed/module-name');

var directive = function () {
    return {
        restrict: "EA",
        bindToController: {
            stepIndex: '@'
        },
        scope: {},
        controllerAs: 'vm',
        require:['thinkbigDefineFeedAccessControl','^thinkbigStepper'],
        templateUrl: 'js/feed-mgr/feeds/define-feed/feed-details/define-feed-access-control.html',
        controller: "DefineFeedAccessControlController",
        link: function ($scope:any, element:any, attrs:any, controllers:any) {
            var thisController = controllers[0];
            var stepperController = controllers[1];
            thisController.stepperController = stepperController;
            thisController.totalSteps = stepperController.totalSteps;
        }
    };
};

export class DefineFeedAccessControlController {

    transformChip:any;
    securityGroupsEnabled:any;
    securityGroupChips:any;
    feedSecurityGroups:any;
    model:any;
    feedAccessControlForm:any;
    stepNumber:any;
    stepIndex:any;

    constructor($scope:any,FeedService:any, FeedSecurityGroups:any) {


        this.stepNumber = parseInt(this.stepIndex)+1

        /**
         * ref back to this controller
         * @type {DefineFeedAccessControlController}
         */
        var self = this;

        /**
         * The angular form
         * @type {{}}
         */
        this.feedAccessControlForm = {};

        /**
         * The feed model
         * @type {*}
         */
        this.model = FeedService.createFeedModel;

        /**
         * Service to access the Hadoop security groups
         */
        self.feedSecurityGroups = FeedSecurityGroups;

        /**
         * Hadoop security groups chips model
         * @type {{}}
         */
        self.securityGroupChips = {};
        self.securityGroupChips.selectedItem = null;
        self.securityGroupChips.searchText = null;

        /**
         * Flag to indicate if hadoop groups are enabled or not
         * @type {boolean}
         */
        self.securityGroupsEnabled = false;






        FeedSecurityGroups.isEnabled().then(function(isValid:any) {
            self.securityGroupsEnabled = isValid;
        });



        self.transformChip = function(chip:any) {
            // If it is an object, it's already a known chip
            if (angular.isObject(chip)) {
                return chip;
            }
            // Otherwise, create a new one
            return {name: chip}
        };



    }

}

angular.module(moduleName).controller("DefineFeedAccessControlController",["$scope","FeedService","FeedSecurityGroups", DefineFeedAccessControlController]);

angular.module(moduleName).directive("thinkbigDefineFeedAccessControl", directive);
