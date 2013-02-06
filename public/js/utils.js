var parametersTemplate= " \
{{#each parameters}} \
 <h3>{{parameter}}</h3> \
 <div> \
  {{#each filters}} \
    <div class='filterValues'> \
     <strong id='{{filter}}'>{{name}}</strong> \
     <br> \
     {{#each values}} \
       <input type='checkbox' value='{{../filter}},{{this}}'/> \
       <label for='{{this}}'>{{this}}</label> \
       <br> \
     {{/each}} \
    </div> \
  {{/each}} \
 </div> \
{{/each}} \
";
var variablesTemplate=" \
{{#each variables}} \
 <div class='variable' id='{{uuid}}'> \
  <div class='variableLink' uuid='{{uuid}}'> \
   <a href='#'>{{shortName}} ({{paramName}})</a> \
  </div> \
 </div> \
{{/each}} \
";
var noInfoTemplate= " \
  <div class='wordrap indented'>No further information available</div> \
";
var infoTemplate= " \
 <div class='wordwrap indented'> \
  <strong>Name: </strong> {{name}} </div> \
 <div class='wordwrap indented'> \
  <strong>Facts: </strong>{{label}} \
   {{#each facts}} \
    <div class='wordwrap indented'><strong>{{predicate}}</strong> \
     {{#if lit}} \
      <a href='#' uuid='{{predicate}}-{{object}}' \
                  class='moredetail'>{{object}}</a> \
     {{else}} \
      {{object}} \
     {{/if}} \
      <div id='{{predicate}}-{{object}}'></div> \
    </div> \
   {{/each}} \
 </div> \
";
var moreInfoTemplate= " \
 <div class='wordwrap indented'> \
  <strong>Name: </strong>{{name}} \
 </div> \
 <div class='wordwrap indented'> \
  <strong>Facts: </strong>{{label}} \
   {{#each facts}} \
    <div class='wordwrap indented'> \
     <strong>{{predicate}}</strong> \
     {{object}} \
     <div id='{{predicate}}--{{object}}'></div> \
    </div> \
   {{/each}} \
 </div> \
";

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
