import * as angular from 'angular';
import "bootstrap-multiselect/dist/js/bootstrap-multiselect";

    /*
     * Examples:
     *   https://gist.github.com/homer2/9df015bf110d0c3f36ae5ce752580580 ***
     *   http://stackoverflow.com/a/21631193
     *   http://www.razum.si/blog/20160710-UsingBootstrapMultiselectWithAngular
     */
    angular.module('moaClientApp')
        .directive('multiSelect', function () {
            return {
                restrict: 'A',

                link: function (scope, element, attrs) {
                    // @ts-ignore
                    // @ts-ignore
                    let options = {
                        onChange: (optionElement, checked) => {
                            if (optionElement != null) {
                                $(optionElement).removeProp('selected');
                            }
                            if (checked) {
                                $(optionElement).prop('selected', 'selected');
                            }
                            element.change();
                        },
                        enableHTML: JSON,
                        buttonClass: null,
                        inheritClass: JSON,
                        buttonWidth: null,
                        buttonContainer: null,
                        dropRight: JSON,
                        dropUp: JSON,
                        selectedClass: null,
                        maxHeight: null,
                        includeSelectAllOption: JSON,
                        includeSelectAllIfMoreThan: null,
                        selectAllText: null,
                        selectAllValue: null,
                        selectAllName: JSON,
                        selectAllNumber: JSON,
                        selectAllJustVisible: JSON,
                        enableFiltering: JSON,
                        enablecaseinsensitivefiltering: JSON,
                        enableFullValueFiltering: JSON,
                        enableClickableOptGroups: JSON,
                        enableCollapsibleOptGroups: JSON,
                        filterPlaceholder: null,
                        filterBehavior: null,
                        includeFilterClearBtn: JSON,
                        preventInputChangeEvent: JSON,
                        nonSelectedText: null,
                        nSelectedText: null,
                        allSelectedText: null,
                        numberDisplayed: null,
                        disableIfEmpty: JSON,
                        disabledText: null,
                        delimiterText: null
                    };

                    //attrs are lowercased by Angular, but options must match casing of bootstrap-multiselect
                    if (attrs.enablehtml) options.enableHTML = JSON.parse(attrs.enablehtml); //default:  false
                    if (attrs.buttonclass) options.buttonClass = attrs.buttonclass; //default:  'btn btn-default'
                    if (attrs.inheritclass) options.inheritClass = JSON.parse(attrs.inheritclass); //default:  false
                    if (attrs.buttonwidth) options.buttonWidth = attrs.buttonwidth; //default:  'auto'
                    if (attrs.buttoncontainer) options.buttonContainer = attrs.buttoncontainer; //default:  '<div class="btn-group" />'
                    if (attrs.dropright) options.dropRight = JSON.parse(attrs.dropright); //default:  false
                    if (attrs.dropup) options.dropUp = JSON.parse(attrs.dropup); //default:  false
                    if (attrs.selectedclass) options.selectedClass = attrs.selectedclass; //default:  'active'
                    if (attrs.maxheight) options.maxHeight = attrs.maxheight; //default:  false,  // Maximum height of the dropdown menu. If maximum height is exceeded a scrollbar will be displayed.
                    if (attrs.includeselectalloption) options.includeSelectAllOption = JSON.parse(attrs.includeselectalloption); //default:  false
                    if (attrs.includeselectallifmorethan) options.includeSelectAllIfMoreThan = attrs.includeselectallifmorethan; //default:  0
                    if (attrs.selectalltext) options.selectAllText = attrs.selectalltext; //default:  ' Select all'
                    if (attrs.selectallvalue) options.selectAllValue = attrs.selectallvalue; //default:  'multiselect-all'
                    if (attrs.selectallname) options.selectAllName = JSON.parse(attrs.selectallname); //default:  false
                    if (attrs.selectallnumber) options.selectAllNumber = JSON.parse(attrs.selectallnumber); //default:  true
                    if (attrs.selectalljustvisible) options.selectAllJustVisible = JSON.parse(attrs.selectalljustvisible); //default:  true
                    if (attrs.enablefiltering) options.enableFiltering = JSON.parse(attrs.enablefiltering); //default:  false
                    if (attrs.enablecaseinsensitivefiltering) options.enablecaseinsensitivefiltering = JSON.parse(attrs.enableCaseInsensitiveFiltering); //default:  false
                    if (attrs.enablefullvaluefiltering) options.enableFullValueFiltering = JSON.parse(attrs.enablefullvaluefiltering); //default:  false
                    if (attrs.enableclickableoptgroups) options.enableClickableOptGroups = JSON.parse(attrs.enableclickableoptgroups); //default:  false
                    if (attrs.enablecollapsibleoptgroups) options.enableCollapsibleOptGroups = JSON.parse(attrs.enablecollapsibleoptgroups); //default:  false
                    if (attrs.filterplaceholder) options.filterPlaceholder = attrs.filterplaceholder; //default:  'Search'
                    if (attrs.filterbehavior) options.filterBehavior = attrs.filterbehavior; //default:  'text', // possible options: 'text', 'value', 'both'
                    if (attrs.includefilterclearbtn) options.includeFilterClearBtn = JSON.parse(attrs.includefilterclearbtn); //default:  true
                    if (attrs.preventinputchangeevent) options.preventInputChangeEvent = JSON.parse(attrs.preventinputchangeevent); //default:  false
                    if (attrs.nonselectedtext) options.nonSelectedText = attrs.nonselectedtext; //default:  'None selected'
                    if (attrs.nselectedtext) options.nSelectedText = attrs.nselectedtext; //default:  'selected'
                    if (attrs.allselectedtext) options.allSelectedText = attrs.allselectedtext; //default:  'All selected'
                    if (attrs.numberdisplayed) options.numberDisplayed = attrs.numberdisplayed; //default:  3
                    if (attrs.disableifempty) options.disableIfEmpty = JSON.parse(attrs.disableifempty); //default:  false
                    if (attrs.disabledtext) options.disabledText = attrs.disabledtext; //default:  ''
                    if (attrs.delimitertext) options.delimiterText = attrs.delimitertext; //default:  ', '

                    /*if (attrs.buttontext) {
                        options.buttonText = function(options, select) {
                            return attrs.buttontext;
                        }
                    }*/

                    element.multiselect(options);

                    // Watch for any changes to the length of our select element
                    scope.$watch(() => {
                        //debugger;
                        return element[0].length;
                    }, () => {
                        scope.$applyAsync(element.multiselect('rebuild'));
                    });

                    // Watch for any changes from outside the directive and refresh
                    scope.$watch(attrs.ngModel, () => {
                        element.multiselect('refresh');
                    });
                }
            };
        });
