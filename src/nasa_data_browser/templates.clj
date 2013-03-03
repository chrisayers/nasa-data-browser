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
      <div>
       <input type='checkbox' value='{{../filter}},{{value}}'/> 
       <label filt='{{../filter}},{{value}}' for='{{valueName}}'>{{valueName}}</label> 
       <br>
      </div> 
     {{/each}} 
    </div> 
  {{/each}} 
 </div> 
{{/each}} 
")

(def variables-desktop "
<table id='variables'>
 <thead>
  <tr>
   <td>Variable</td>
   <td>Parameter</td>
   <td>Products</td>
  </tr>
 </thead>
 <tbody>
  {{#each variables}} 
   <tr class='variable' id='varpicker-{{variable}}' var='{{variable}}'>
    <td> 
     <input type='checkbox' value='{{variable}}'/> 
     <a uuid='{{variable}}' class='infolink' href='#'>{{variableName}}</a> 
    </td>
    <td>{{parameterName}}</td>
    <td>
     {{#each products}}
     <a uuid='{{product}}' class='infoLink' href='#'>{{name}}</a>
     {{/each}}
    </td>
   </tr>
  {{/each}} 
 </tbody>
</table>
")

(def comparison-desktop "
<html>
<head>
 <title>Comparison</title>
 <link rel='stylesheet' type='text/css' href='http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/css/jquery.dataTables.css'>
<script type='text/javascript' charset='utf8' src='http://ajax.aspnetcdn.com/ajax/jQuery/jquery-1.8.2.min.js'></script>
<script type='text/javascript' charset='utf8' src='http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/jquery.dataTables.min.js'></script>
<script type='text/javascript' charset='utf-8'>
  $(document).ready(function() { $('#compTable').dataTable(); });
</script>
 
</head>
<body>
 <div>
  <table id='compTable'>
  <thead>
   <tr>
    {{#each relations}}
     <th>{{this}}</th>
    {{/each}}
   </tr>
  </thead>
  <tbody>
  {{#each variables}}
   <tr>
     {{#each quickFacts}}
      <td>{{value}}</td>
     {{/each}}
   </tr>
  {{/each}}
  </tbody>
  </table>
 </div>
</body>
</html>
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