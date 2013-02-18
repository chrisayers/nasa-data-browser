//var appUrl= "http://localhost:8080/nasa-agu";
var appUrl= "http://nasa-sleepydog.elasticbeanstalk.com";
var topicsUrl= appUrl+"/topics";
var variablesUrl= appUrl+"/variables";
var detailsUrl= appUrl+"/info";
var names= {};
var filters= {};
var filterValues= {};
var variables= {};
var filterIndex= {};

$(document).ready(getTopics);

function getTopics() {
    $.getJSON(topicsUrl, setTopics); 	
}
function setTopics(data) {
    names= data.names;
    filters= data.filters;
    filterValues= data.filterValues;
    $("#parameter-list").empty();
    $.each(data.topics, addTopic);
    $("#parameter-list").listview('refresh');
    $("#parameter-list li").live('click', function() {
	var topic= $(this).text();
	setFilters(topic.trim());
	clearCheckboxes();
//	clearDetails();
	getVariables(topic);
    });
}
function clearCheckboxes() {
	$('.filterValues').find(':checked').each(function() {
		$(this).removeAttr('checked');
	});
}
function addTopic(index, topic) {
    var text= '<li><a href="#filters">'+topic+'</a></li>';
    $("#parameter-list").append(text);
}
function setFilters(topic) {
    $("#filter-container").empty();
    var text= filters[topic].map(addFilter).join('');
    $('#filter-container').html(text).page();
    $('input[type="checkbox"]').checkboxradio();
    $('#filters').page();
}
function addFilter(filter) {
    var text= '<fieldset data-role="controlgroup">';
    text+= '<legend><strong>'+name(filter)+'</strong></legend>';
    var pairs= filterValues[filter].map(function(fv) { return [filter, fv]; });
    text+= pairs.map(addFilterValues).join('');
    text+='</fieldset><br>';
    return text;
}
function addFilterValues(pair) {
    var filter= pair[0];
    var filterValue= pair[1];
    var id= filter+'#'+filterValue;
    var text= '<input class="filter-value" type="checkbox" id="'+id+'" value="'+[filter,filterValue]+'"/>';
    text+= '<label for="'+id+'">'+filterValue+'</label>';
    return text;
}

function name(obj) {
	if (obj in names) { return names[obj]; }
	else return obj;
}

function getVariables(parameter) {
	var items=$(".ui-checkbox-on").map(function () {
		return this.value;
	}).get();
	var X= {'parameter': parameter.trim()};
	$.each(items, function(i, item) { 
		var i= item.split(',');
		var theFilter= i[0];
		var theFilterValue= i[1];
		X[theFilter]= theFilterValue;
	});
	$.getJSON(variablesUrl, X, setVariables);
}
function setVariables(data) {
    variables= data.variables;
    filterIndex= data.filterIndex;
    $('#variable-list').empty();
    $.each(variables, addVariable);
    activateCheckboxes();
    $('.variable').click(getDetails);
    $('#variable-list').listview('refresh');
    $('#variables').page('refresh');
}
function addVariable(index, variable) {
    var uuid= variable.variable;
    var shortName= uuid;
    if ('shortName' in variable) { shortName= variable.shortName; }
    var text= '<li class="li-variable" id="li-'+uuid+'">';
    text+= '<a href="#details" class="variable" id="'+uuid+'" uuid="'+uuid+'">'+shortName+'</a>';
    text+= '</li>';
    $('#variable-list').append(text);
}
function activateCheckboxes() {
	$('.filter-value').live('change', filterVariables);
}
function filterVariables() {
	clearDetails();
	clearHighlights();
	showAllVariables();
	var allVars= $.map(variables, function(v,k) { return v['variable']; });
	var items=$(".filter-value:checked").map(function () {
		return this.value;
	}).get();
	if (items.length == 0) { showAllVariables(); }
	else {
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
function showAllVariables() {
	$(".li-variable").show();
}
function hideVariable(variable) {
	$('#li-'+variable).hide();
}
function highlightVariable(variable) {
	$('#li-'+variable).css('background-color', 'yellow');
}
function clearHighlights() {
	$('.li-variable').css('background-color', 'inherit');
}
function intersect_all(lists) {
	if (lists.length == 0) return [];
	else if (lists.length == 1) return lists[0];
	var partialInt = lists[0];
	for (var i = 1; i < lists.length; i++) {
		partialInt = intersection(partialInt, lists[i]);
	}
	return partialInt;
}
function intersection(a, b) {
	var ai=0, bi=0;
	var result = new Array();
	while( ai < a.length && bi < b.length ) {
		if      (a[ai] < b[bi] ){ ai++; }
		else if (a[ai] > b[bi] ){ bi++; }
		else /* they're equal */ {
			result.push(a[ai]);
			ai++;
			bi++;
		}
	}
	return result;
}

function getDetails() {
	var params= {"variable": $(this).attr('uuid')};
	$.getJSON(detailsUrl, params, setDetails);
}
function setDetails(data) {
	clearHighlights();
	clearDetails();
	highlightVariable(data.variable);
	drawFacts(data.name, data.facts);
}
function clearDetails() {
	$('#facts').empty();
}
function drawFacts(label, facts) {
	$('#facts').empty();
	var text= '<div><strong>Name: </strong>'+label+'</div>';
        text+= '<br>';
	text+= '<strong>Facts: </strong>';
	if (facts.length == 0) { $('#facts').append('<div>No info available</div>'); }
	text+= facts.map(function(f) { 
		return ''+
		'<div class="wordwrap indented">'+
		'<strong>'+f['predicate']+': </strong>'+f['object']+'</div>';
	}).join('');
	$('#facts').append(text);
}
