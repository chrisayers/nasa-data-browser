var webUrl= "http://localhost:8080";
//var webUrl= "http://nasa-sleepydog.elasticbeanstalk.com";
var appUrl= webUrl+"/data";
var templatesUrl= appUrl+"/templates";
var parametersUrl= appUrl+"/parameters";
var variablesUrl= appUrl+"/variables";
var comparisonUrl= appUrl+"/comparison";
var infoUrl= appUrl+"/info";

function intersect_all(lists) {
    if (lists.length === 0) return [];
    else if (lists.length === 1) return lists[0];
    var partialInt = lists[0];
    if (typeof partialInt === 'undefined') return [];
    for (var i = 0; i < lists.length; i++) {
	if (typeof lists[i] === 'undefined') return [];
	partialInt = intersection(partialInt, lists[i]);
    }
    return partialInt;
}
function intersection(a, b) {
    a.sort();
    b.sort();
    var ai=0, bi=0;
    var result = [];
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
function stripe(tableid) {
    $(tableid+' tr').removeClass('org alt');
    $(tableid+' tr:visible:even').addClass('org');
    $(tableid+' tr:visible:odd').addClass('alt');
}
function updateFilterCounts(filtIndex) {
    var checked=$(".filterValues input[type='checkbox']:checked")
	.map(function () { return this.value; }).get();

    $('#parameters div.ui-accordion-content-active div label').each(function(i) {
	var filt= processFiltValue($(this).attr('filt'));
	var num;
	if (checked.length > 0) {

	    var X= [];
	    $.each(checked, function(j, item) { X.push(processFiltValue(item)); });
	    X.push(filt);
	    var indivFilters= [];
	    $.each(X, function(i, v) { 
		if (v in filterIndex) { indivFilters.push(filterIndex[v]); }
		else { indivFilters.push([]); }
	    });
	    num= intersect_all(indivFilters).length;
	}
	else { 
	    if (!(filt in filtIndex)) { num= 0; }
	    else { num= filtIndex[filt].length; }
	}
	if (num > 0) {
	    var result= $(this).attr('for')+' ('+num+')';
	    $(this).text(result).parent().show();
	}
	else {
	    $(this).parent().slideUp();
	}	
    });
}
function updateFilterCountsMobile(filtIndex) {
    var checked=$("input.filter-value[type='checkbox']:checked")
	.map(function () { return this.value; }).get();    
    $('#filter-container fieldset div.ui-checkbox label').each(function(i) {
	var filt= processFiltValue($(this).attr('filt'));
	var num;
	if (checked.length > 0) {
	    var X= [];
	    $.each(checked, function(j, item) { X.push(processFiltValue(item)); });
	    X.push(filt);
	    var indivFilters= [];
	    $.each(X, function(i, v) { indivFilters.push(filterIndex[v]); });
	    num= intersect_all(indivFilters).length;
	}
	else { num= filtIndex[filt].length; }
	if (num > 0) {
	    var result= $(this).attr('valueName')+' ('+num+')';
	    $(this).find('span.ui-btn-inner span.ui-btn-text').text(result);
	    $(this).parent().show();
	}
	else {
	    $(this).parent().hide();
	}
    });
}
function processFiltValue(item) {
    var i= item.split(',');
    var theFilter= i[0].split("#")[1];
    var theFilterValue= i[1];
    return theFilter+'#'+theFilterValue;
}
