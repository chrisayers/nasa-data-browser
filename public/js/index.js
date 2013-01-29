var menuService= "http://localhost:3000/nasa-agu/hierarchy";
var contentService= "http://localhost:3000/nasa-agu/variables";

$(document).ready(setup);

function setup() { 
	getMenu();
	getContent('Radiation');
}

function getContent(topic) {
	var url= contentService+'?topic='+topic;
	$.getJSON(url, setContent); 
}

function setContent(data) {
	$('#right').empty();
	var text= '<table id=contentTable>';
	text+='<tr><th>Parameter</th><th>Variable</th></tr>';
	$.each(data, function(i,v) { 
		text+='<tr><td>'+v.parameter+'</td><td>'+v.variable+'</td></tr>';
	});
	text+= '</table>';
	$('#right').append(text);
}

function getMenu() { $.getJSON(menuService, setMenu); }

function setMenu(data) {
	$('#left').empty();
	var text= '<ul id=menu>';
	$.each(data.hierarchy['ScienceParameter'], function(i,v) { text= addMenuNode(data, v, text); });
	text+= '</ul>';
	$('#left').append(text);	
	$('#menu').menu();
}

function addMenuNode(data, parameter, text) {
	var children= data.hierarchy[parameter];
	text+= '<li><a href="#">'+parameter+'</a>';
	if (parameter in data.hierarchy) {
		text+= '<ul>';
		$.each(children, function(i,v) { text= addMenuNode(data, v, text); }); 
		text+= '</ul>';	
	}
	text+= '</li>';
	return text;
}
