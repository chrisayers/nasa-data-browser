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
    $('#compare').hide();
    if (jQuery.browser.mobile) { window.location.replace(webUrl+'/mobile.html'); }
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
    $("#compare").click(getComparison);
    getParameters();
}
function getParameters() { 
    $.getJSON(parametersUrl, setParameters); 
}
function setParameters(data) {
    productIndex= data.hasProduct;
    productNames= data.hasProductName;
    $('#parameters').html(getParametersContent(data));
    $('#parameters').unbind();
    $('#parameters').accordion(accordionOptions);
    $('#parameters h3').live('click', function () {
	$(".filterValues input[type='checkbox']").attr('checked', false);
	$('#vars').empty(); 
	var parameter= $(this).text();
	var opening= $(this).hasClass('ui-state-active');
	if (opening) { 
	    getVariables(parameter); 
	}
	else { 
	    $('#compare').hide();
	}
    });
}
function getVariables(parameter) {
	$.getJSON(variablesUrl+"/"+parameter, setVariables);
}
function addProductsToItem(item) {
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
    $.each(oldVars, function(i,v) { addProductsToItem(v); });
    var newData= {"variables": oldVars};
    $('#compare').show();
    $('#vars').html(getVariablesContent(newData));
    $('.filterValues input').unbind().live('click', filterVariables);
    $('a.infolink').unbind().click(infoLinkClick);
    updateFilterCounts(filterIndex);
}
function filterVariables() {
    $(".variable input[type='checkbox']").attr('checked', false);
    $(".variable").show();
    var allVars= $(".variable").map(function() { return $(this).attr("var"); });
    var items=$(".filterValues input[type='checkbox']:checked")
	.map(function () { return this.value; }).get();
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
	$.each(allVars, function(i,v) { 
	    var inFilter= ($.inArray(v, compositeFilter) > -1)
	    if (!inFilter) { $('#varpicker-'+v).hide(); }
	});
    }
    updateFilterCounts(filterIndex);
    stripe('#variables');
}
function getComparison() {
    var selectedVars= $(".variable input[type='checkbox']:checked")
	.map(function () { return this.value; }).get();
    if (selectedVars.length > 0) {
	$.getJSON(comparisonUrl, {"vars": selectedVars}, setComparison); 
    }
}
function setComparison(data) {
    var w= window.open('', 'comparison', 'width=600, height=400');
    w.document.open();
    w.document.write(getComparisonContent(data));
    w.document.close();
    return false;
}
function infoLinkClick(e) {
    e.preventDefault();
    getInfo($(this).attr('uuid'));
}
function getInfo(item) {
    $.getJSON(infoUrl+"/"+item, setInfo);
}
function setInfo(data) {
    var w =  window.open('','','width=450,height=500');
    w.document.open();    
    if (data.uuid in productIndex) { addProductsToItem(data); }
    w.document.write(getInfoContent(data));
    w.document.close();
    return false;
}
