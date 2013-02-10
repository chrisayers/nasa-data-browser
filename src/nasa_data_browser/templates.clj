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
<table id='variables>
 <tr>
  <td>Variable</td>
  <td>Parameter</td>
  <td>Product</td>
 </tr>
{{#each variables}} 
 <tr>
  <td> 
   <input type='checkbox' value='{{uuid}}'/> 
   <a href='#'>{{variable}}</a> </td>
  <td> {{parameter}} </td>
  <td> {{products}} </td>
 </tr>
{{/each}} 
</table>
")

(def comparison-desktop "
<table id='comparison>
 <tr>
  {{#each relations}}
   <td>{{this}}</td>
  {{/each}}
 </tr>
 {{#each variables}}
  <tr>
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
  {"parameters": parameters-desktop
   "variables": variables-desktop
   "comparison": comparison-desktop
   "info": info-desktop})

(defn get-data [view]
  (cond
   (== view 'mobile') (get-mobile)
   :else (get-desktop) ))