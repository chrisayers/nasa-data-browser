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
 <thead>
  <tr>
   <th>Variable</th>
   <th>Parameter</th>
   <th>Products</th>
  </tr>
 </thead>
 <tbody>
  {{#each variables}} 
   <tr class='variable' id='varpicker-{{variable}}' var='{{variable}}'>
    <td> 
     <input type='checkbox' value='{{variable}}'/> 
     <a href='#'>{{variableName}}</a> 
    </td>
    <td>{{parameterName}}</td>
    <td>
     {{#each products}}
      <a uuid='{{product}}' href='#'>{{name}}</a>
     {{/each}}
    </td>
   </tr>
  {{/each}} 
 </tbody>
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