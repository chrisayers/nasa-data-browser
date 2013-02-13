(ns nasa-data-browser.templates)

(def parameters-desktop "
{{#each parameters}} 
 <h3>{{parameter}}</h3> 
 <div> 
  {{#each filters}} 
    <div class='filterValues'> 
     <strong id='{{filter}}'>{{name}}</strong> 
     <br> 
     {{#each values}} 
       <input type='checkbox' value='{{../filter}},{{this}}'/> 
       <label for='{{this}}'>{{this}}</label> 
       <br> 
     {{/each}} 
    </div> 
  {{/each}} 
 </div> 
{{/each}} 
")

(def variables-desktop "
<table id='variables'>
  <tr>
   <td>Variable</td>
   <td>Parameter</td>
   <td>Products</td>
  </tr>
  {{#each variables}} 
   <tr class='variable' id='varpicker-{{variable}}' var='{{variable}}'>
    <td> 
     <input type='checkbox' value='{{variable}}'/> 
     <a uuid='{{variable}}' class='infolink' href='#'>{{variableName}}</a> 
    </td>
    <td>{{parameterName}}</td>
    <td>
     {{#each products}}
     <p>{{name}}</p>
     {{/each}}
    </td>
   </tr>
  {{/each}} 
</table>
")

(def comparison-desktop "
<table>
  <tr>
   <td>uuid</td>
   <td>name</td>
   {{#each relations}}
    <td>{{this}}</td>
   {{/each}}
  </tr>
 {{#each variables}}
  <tr>
   <td>{{variable}}</td>
   <td>{{name}}</td>
    {{#each quickFacts}}
     <td>{{value}}</td>
    {{/each}}
  </tr>
 {{/each}}
</table>
")

(def info-desktop "
<html>
<head><title>{{name}} Info</title></head>
<body>
 <h2 id='name'>{{name}}</h2>
 <br>
 <h2 id='facts'>Quick Facts:</h2>
  {{#each facts}}
  <p style='padding-left: 2em'><b>{{relation}}</b>: {{value}}</p>
  {{/each}}
 {{#if products}}
 <h2 id='facts'>Products:</h2>
 {{/if}}
  {{#each products}}
  <p style='padding-left: 2em'>{{name}}</p>
  {{/each}}
</body>
</html>
")

(defn get-mobile [] )

(defn get-desktop []
  {"parameters" parameters-desktop
   "variables" variables-desktop
   "comparison" comparison-desktop
   "info" info-desktop})

(defn get-data [view]
  (cond
   (= view "mobile") (get-mobile)
   :else (get-desktop) ))