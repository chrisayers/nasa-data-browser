var appUrl= "http://localhost:3000";
//var appUrl= "http://nasa-sleepydog.elasticbeanstalk.com";
var templatesUrl= appUrl+"/templates";
var parametersUrl= appUrl+"/parameters";
var variablesUrl= appUrl+"/variables";
var detailsUrl= appUrl+"/info";
var filterIndex= {}; // filters -> lists of variables
var productIndex= {}; // variables -> products
var productNames= {}; // products -> product names
var accordionOptions= {collapsible: true,
		active: false,
		heightStyle: "content"}
var getParametersContent;
var getVariablesContent;
var getComparisonContent;
var getInfoContent;

$(document).ready(setup);

function setup() { 
    if (jQuery.browser.mobile) { window.location.replace(appUrl+'/mobile.html'); }
    else { getTemplates('desktop'); }
}
function getTemplates(view) {
    $.getJSON(templatesUrl+"/"+view, setTemplates); 
}
function setTemplates(data) {
    getParametersContent= Handlebars.compile(data.parameters);
    getVariablesContent= Handlebars.compile(data.variables);
    getComparisonContent= Handlebars.compile(data.comparison);
    getInfoContent= Handlebars.compile(data.info);
    getParameters();
}
function getParameters() { 
    $.getJSON(parametersUrl, setParameters); 
}
function setParameters(data) {
    productIndex= data.hasProduct;
    productNames= data.hasProductName;
    $('#parameters').html(getParametersContent(data));
    $('#parameters').accordion(accordionOptions);
    $('#parameters h3').live('click', function () {
	$('.filterValues:checked').removeAttr('checked');
	var parameter= $(this).text();
	var opening= $(this).hasClass('ui-state-active');
	if (opening) { getVariables(parameter); }
	else { $('#variables').empty(); }
    });
}
function getVariables(parameter) {
	$.getJSON(variablesUrl+"/"+parameter, setVariables);
}
function addProductsToVar(item) {
    var products= [];
    if (item.uuid in productIndex) {
	var productList= productIndex[item.uuid];
	products= $.map(productList, 
			function(v) { return {'product': v, 'name':productNames[v][0]} });
    }
    item['products']= products
}
function setVariables(data) {
    filterIndex= data.filterIndex;
    var oldVars= data.variables;
    $.each(oldVars, function(i,v) { addProductsToVar(v); });
    var newData= {"variables": oldVars};
    $('#variables').html(getVariablesContent(newData));
    $('.filterValues input').live('click', filterVariables);
//    $('.variableLink').click(getDetails);
}
function filterVariables() {
    $(".variable").show();
    var allVars= $(".variable").map(function() { return $(this).attr("var"); });
    var items=$(".filterValues input[type='checkbox']:checked").map(function () {  
	return this.value;
    }).get();
    if (items.length > 0) {
	var X= [];
	$.each(items, function(j, item) { 
	    var i= item.split(',');
	    var theFilter= i[0].split("#")[1];
	    var theFilterValue= i[1];
	    X.push(theFilter+"#"+theFilterValue);
	});
	var indivFilters= [];
	$.each(X, function(i, v) { indivFilters.push(filterIndex[v]); });
	var compositeFilter= intersect_all(indivFilters);
	console.log(compositeFilter);
	$.each(allVars, function(i,v) { 
	    var inFilter= ($.inArray(v, compositeFilter) > -1)
	    if (!inFilter) { $('#varpicker-'+v).hide(); }
	});
    }
}
