var appUrl= "http://localhost:8080/data";
//var appUrl= "http://nasa-sleepydog.elasticbeanstalk.com/data";
var templatesUrl= appUrl+"/templates";
var parametersUrl= appUrl+"/parameters";
var variablesUrl= appUrl+"/variables";
var comparisonUrl= appUrl+"/comparison";
var infoUrl= appUrl+"/info";

function intersect_all(lists) {
    if (lists.length === 0) return [];
    else if (lists.length === 1) return lists[0];
    var partialInt = lists[0];
    for (var i = 0; i < lists.length; i++) {
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
