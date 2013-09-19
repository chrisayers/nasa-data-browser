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
var currentParameter;
var compareHead= "<html><head>"+
"<title>Comparison</title>"+
"<link rel='stylesheet' type='text/css' href='http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/css/jquery.dataTables.css'>"+
"<script type='text/javascript' charset='utf8' src='http://ajax.aspnetcdn.com/ajax/jQuery/jquery-1.8.2.min.js'></script>"+
"<script type='text/javascript' charset='utf8' src='http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/jquery.dataTables.min.js'></script>"+
"<script type='text/javascript' charset='utf-8'>"+
"$(document).ready(function() { $('#compTable').dataTable(); });"+
"</script>"+
"<body>";
var compareTail= "</body></html>";

$(document).ready(setup);

function setup() { 
    $('#search-btn').click(keywordSearch);
    $('#clear-btn').click(function() { 
	$('#search-box').val('');
	getVariables(); });
    $('#search-form').hide();
    $('#compare').hide();
    if (jQuery.browser.mobile) { window.location.replace(webUrl+'/mobile.html'); }
    else { getTemplates('desktop'); }
}
function getTemplates(view) {
    $.getJSON(templatesUrl+"/"+view, setTemplates); 
}
function setTemplates(data) {
    var paramsTemplate= $('#params-template').html();
    var varsTemplate= $('#vars-template').html();
    var compareTemplate= $('#compare-template').html();
    var infoTemplate= $('#info-template').html();
    getParametersContent= Handlebars.compile(paramsTemplate);
    getVariablesContent= Handlebars.compile(varsTemplate);
    getComparisonContent= Handlebars.compile(compareTemplate);
    getInfoContent= Handlebars.compile(infoTemplate);
    $("#compare").click(getComparison);
    getParameters();
    getProducts();
}
function getProducts() {
    $.getJSON(productsUrl, setProducts);
}
function setProducts(data) {
    productIndex= data.hasProduct;
    productNames= data.hasProductName;
}
function getParameters() { 
    $.getJSON(parametersUrl, setParameters); 
}
function setParameters(data) {    
    $('#parameters').html(getParametersContent(data));
    $('#parameters').unbind();
    $('#parameters').accordion(accordionOptions);
    $('#parameters h3').live('click', function () {
	$('#vars').empty(); 
	currentParameter= $(this).text();
	var opening= $(this).hasClass('ui-state-active');
	if (opening) { getVariables(); }
	else { 
	    $('#compare').hide();
	    $('#search-form').hide();
	}
    });
}
function getVariables() {
    $(".filterValues input[type='checkbox']").attr('checked', false);
    $('#vars').empty(); 
    $.getJSON(variablesUrl+"?parameter="+currentParameter, setVariableNames);
}
function keywordSearch() {
    $(".filterValues input[type='checkbox']").attr('checked', false);
    $('#vars').empty(); 
    var keyword= $('#search-box').val(); 
    $.getJSON(variablesUrl+"?parameter="+currentParameter+"&keyword="+keyword, setVariables);
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
function setVariableNames(data) {
    filterIndex= data.filterIndex;
    $('#compare').show();
    $('#vars').html(getVariablesContent(data));
    activateVarTableAccordion();
    $('.filterValues input').unbind().live('click', filterVariables);
    $('a.infolink').unbind().click(infoLinkClick);
    updateFilterCounts(filterIndex);
    $('#search-form').show();
    $('#search-box').val('');
}
function activateVarTableAccordion() {
    $("#variables-table > tbody > tr:not(.variableName)").hide();
    $("#variables-table tr:first-child").show();
    $("#variables-table tr.variableName").click(function(){
	$(this).next().fadeToggle();
    });
}
function filterVariables() {
    $(".variable input[type='checkbox']").attr('checked', false);
    $(".variableName").show();
    $(".variable").show();
    var allVars= $(".variableName").map(function() { return $(this).attr("var"); });
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
	    var inFilter= ($.inArray(v, compositeFilter) > -1);
	    try { if (!inFilter) { $('#varpicker-'+v).hide(); } }
	    catch(e) { document.getElementById("varpicker-"+v).style.display="none"; }
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
    w.document.write(compareHead+getComparisonContent(data)+compareTail);
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
