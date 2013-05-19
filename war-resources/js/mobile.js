var parameters= {};
var filterIndex= {}; // filters -> lists of variables
var getParametersContent;
var getFiltersContent;
var getVariablesContent;
var getInfoContent;

$(document).ready(setup);

function setup() { 
    getTemplates('mobile');
}
function getTemplates(view) {
    $.getJSON(templatesUrl+"/"+view, setTemplates); 
}
function setTemplates(data) {
    getParametersContent= Handlebars.compile(data.parameters);
    getFiltersContent= Handlebars.compile(data.filters);
    getVariablesContent= Handlebars.compile(data.variables);
    getInfoContent= Handlebars.compile(data.info);
    getParameters();
}
function getParameters() {
    $.getJSON(parametersUrl, setParameters);
}
function setParameters(data) {
    parameters= data.parameters;
    $("#parameter-list").html(getParametersContent(data));
    $("#parameter-list").listview('refresh');
    $("#parameter-list li").live('click', function() {
	var param= $(this).text().trim();
	setFilters(param);
	clearCheckboxes();
	getVariables(param);
    });
}
function clearCheckboxes() {
	$('.filterValues').find(':checked').each(function() {
		$(this).removeAttr('checked');
	});
}
function setFilters(param) {
    var parameter= $.grep(parameters, function(e,i) { return e.parameter === param; })[0];
    $("#filter-container").html(getFiltersContent(parameter));
    $('input[type="checkbox"]').checkboxradio();
    $('#filters').page();
}

function getVariables(parameter) {
    $.getJSON(variablesUrl+"/"+parameter, setVariables);
}
function setVariables(data) {
    filterIndex= data.filterIndex;
    $('#variable-list').html(getVariablesContent(data)).trigger('create');
    activateCheckboxes();
    $('.variable').click(getInfo);
    updateFilterCountsMobile(filterIndex);
    $('#variable-list').listview('refresh');
}
function activateCheckboxes() {
    $('.filter-value').live('change', filterVariables);
}
function filterVariables() {
    $('#facts').empty();
    clearHighlights();
    showAllVariables();
    var allVars= $(".variable").map(function() { return $(this).attr('var'); });
    var items=$(".filter-value:checked").map(function () {
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
    updateFilterCountsMobile(filterIndex);
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
function getInfo() {
    $.getJSON(infoUrl+"/"+$(this).attr('var'), setInfo);
}
function setInfo(data) {
    clearHighlights();
    highlightVariable(data.variable);
    $('#facts').html(getInfoContent(data));
}
