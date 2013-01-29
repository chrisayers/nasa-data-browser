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
var accordionOptions= {collapsible: true,
		active: false,
		heightStyle: "content"}

$(document).ready(setup);

function setup() { 
	if (jQuery.browser.mobile) { window.location.replace(appUrl+'/mobile.html'); }
	else { getTopics(); }
}
function getTopics() { 
	$.getJSON(topicsUrl, setTopics); 
}
function setTopics(data) {
	names= data.names;
	filters= data.filters;
	filterValues= data.filterValues;
	$.each(data.topics, addTopic);
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
function addTopic(index, topic) {
	var text= '<h3>'+topic+'</h3>';
	text+= '<div>';
	text+= filters[topic].map(addFilters).join('');
	text+= '</div>';
	$('#topics').append(text);
}
function addFilters(filter) {
	var text= '<div class="filterValues">';
	text+= '<strong id="'+filter+'">'+name(filter)+'</strong><br>';
	var pairs= filterValues[filter].map(function(fv) { return [filter, fv]; });
	text+= pairs.map(addFilterValues).join('');
	text+= '</div>';
	return text;
}
function addFilterValues(pair) {
	var filter= pair[0];
	var filterValue= pair[1];
	var text= '<input type="checkbox" value="'+[filter, filterValue]+'"/>';
	text += '<label for="'+filterValue+'">'+filterValue+'</label><br>';
	return text;
}
function name(obj) {
	if (obj in names) { return names[obj]; }
	else return obj;
}
function getVariables(parameter) {
	var items=$("input[type='checkbox']:checked").map(function () {
		return this.value;
	}).get();
	var X= {'parameter': parameter};
	$.each(items, function(i, item) { 
		var i= item.split(',');
		var theFilter= i[0];
		var theFilterValue= i[1];
		X[theFilter]= theFilterValue;
	});
	$.getJSON(variablesUrl, X, setVariables);
}
function filterVariables() {
	clearDetails();
	clearHighlights();
	showAllVariables();
	var allVars= $.map(variables, function(v,k) { return v['variable']; });
	var items=$("input[type='checkbox']:checked").map(function () {
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
	$(".variable").show();
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
function setVariables(data) {
	variables= data.variables;
	filterIndex= data.filterIndex;
	$('#variables').empty();
	$.each(variables, addVariable);
	activateCheckboxes();
	$('.variableLink').click(getDetails);
}
function addVariable(index, variable) {
	var uuid= variable.variable;
	var shortName= uuid;
	var paramName= '';
	if ('shortName' in variable) { shortName= variable.shortName; }
	if ('paramName' in variable) { paramName= variable.paramName; }
	else { units= '(none listed)'; }
	var text= '<div class="variable" id="'+uuid+'">';
	text+= '<div class="variableLink" uuid="'+uuid+'">';
	text+= '<a href="#">'+shortName+' ('+paramName+')</a></div>';
	text+= '</div>';
	$('#variables').append(text);
}
function activateCheckboxes() {
	$('.filterValues input').live('click', filterVariables);
}
function getDetails() {
	var params= {"variable": $(this).attr('uuid')};
	$.getJSON(detailsUrl, params, setDetails);
}
function setDetails(data) {
	clearHighlights();
	clearDetails();
	highlightVariable(data.variable);
	drawFacts('#facts', data.name, data.facts);
	$('.moredetail').click(getMoreDetails);
}
function clearDetails() {
	$('#name').empty();
	$('#facts').empty();
}
function drawFacts(divId, name, facts) {
	$(divId).empty();
        var text= '';
        if (facts.length === 0) { text+= '<div class="wordrap indented">No further information available</div>'; }
	else { 
	    var label= 'unknown';
	    if (name != '') { label= name; }
	    text+= '<div class="wordwrap indented"><strong>Name: </strong>'+label+'</div>';
            var klass= '';
            if (divId == '#facts') { klass= 'moredetail'; }
	    text+= '<div class="wordwrap indented"><strong>Facts:</strong>';
	    text+= facts.map(function(f) {
		var object= f['object'];
		var predicate= f['predicate'];
		var id= predicate+'-'+object;
		var txt= '<div class="wordwrap indented">';
		txt+= '<strong>'+predicate+': </strong>';
		if (divId == '#facts' && 'lit' in f) { 
			txt+= '<a href="#" uuid="'+id+'" class="'+klass+'">'+object+'</a>'; 
		}
		else { txt+= object; }
		txt+= '<div id="'+id+'"></div>';
		txt+= '</div>';
		return txt;
	    }).join('');
	}
        text+= '</div>';
	$(divId).append(text);
}
function getMoreDetails() {
	var params= {"variable": $(this).attr('uuid').split('-')[1]};
	var id= $(this).attr('uuid');
        if ($('#'+id).is(':empty')) { $.getJSON(detailsUrl, params, function(data) { setMoreDetails(id, data); }); }
        else { $('#'+id).empty(); }
}
function setMoreDetails(id, data) {
	drawFacts('#'+id, data.name, data.facts);
}


