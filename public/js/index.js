var appUrl= "http://localhost:3000";
//var appUrl= "http://nasa-sleepydog.elasticbeanstalk.com";
var topicsUrl= appUrl+"/topics";
var variablesUrl= appUrl+"/variables";
var detailsUrl= appUrl+"/info";
var filterIndex= {};
var accordionOptions= {collapsible: true,
		active: false,
		heightStyle: "content"}
var getTopicsContent;
var getVariablesContent;
var getInfoContent;
var getMoreInfoContent;
var getNoInfoContent;

$(document).ready(setup);

function setup() { 
    getTopicsContent= Handlebars.compile(topicsTemplate);
    getVariablesContent= Handlebars.compile(variablesTemplate);
    getInfoContent= Handlebars.compile(infoTemplate);
    getMoreInfoContent= Handlebars.compile(moreInfoTemplate);
    getNoInfoContent= Handlebars.compile(noInfoTemplate);
    if (jQuery.browser.mobile) { window.location.replace(appUrl+'/mobile.html'); }
    else { getTopics(); }
}
function getTopics() { 
	$.getJSON(topicsUrl, setTopics); 
}
function setTopics(data) {
    $('#topics').html(getTopicsContent(data));
    $('#topics').accordion(accordionOptions);
    $('#topics h3').live('click', function () {
	clearCheckboxes();
	clearDetails();
	var parameter= $(this).text();
	var opening= $(this).hasClass('ui-state-active');
	if (opening) { getVariables(parameter); }
	else { $('#variables').empty(); }
    });
}
function clearCheckboxes() {
    $('.filterValues').find(':checked').each(function() {
	$(this).removeAttr('checked');
    });
}
function setVariables(data) {
    filterIndex= data.filterIndex;
    $('#variables').empty();
    $('#variables').html(getVariablesContent(data));
    activateCheckboxes();
    $('.variableLink').click(getDetails);
}
function activateCheckboxes() {
	$('.filterValues input').live('click', filterVariables);
}
function getVariables(parameter) {
	$.getJSON(variablesUrl, {'parameter': parameter}, setVariables);
}
function filterVariables() {
    clearDetails();
    clearHighlights();
    $(".variable").show();
    var allVars= $(".variable").map(function() { return $(this).attr("id"); });
    var items=$("input[type='checkbox']:checked").map(function () {  
	return this.value;
    }).get();
    if (items.length > 0) {
	var X= [];
	$.each(items, function(i, item) { 
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
	    if (!inFilter) { hideVariable(v); }
	});
    }
}

function hideVariable(variable) {
	$('#'+variable).hide();
}
function highlightVariable(variable) {
	$('#'+variable).css('background-color', 'yellow');
}
function clearHighlights() {
	$('.variable').css('background-color', 'inherit');
}
function getDetails() {
	var params= {"variable": $(this).attr('uuid')};
	$.getJSON(detailsUrl, params, setDetails);
}
function setDetails(data) {
    clearHighlights();
    clearDetails();
    highlightVariable(data.variable);
    $('#facts').empty();
    if (data.facts.length === 0) { $('#facts').html(getNoInfoContent(data)); }
    else { $('#facts').html(getInfoContent(data)); }
    $('.moredetail').click(getMoreDetails);
}
function clearDetails() {
	$('#name').empty();
	$('#facts').empty();
}
function getMoreDetails() {
    var params= {"variable": $(this).attr('uuid').split('-')[1]};
    var id= $(this).attr('uuid');
    if ($('#'+id).is(':empty')) { 
	$.getJSON(detailsUrl, 
		  params, 
		  function(data) { setMoreDetails(id, data); }); }
    else { $('#'+id).empty(); }
}
function setMoreDetails(id, data) {
    if (data.facts.length === 0) { $('#'+id).html(getNoInfoContent(data)); }
    else { $('#'+id).html(getMoreInfoContent(data)); }
}


