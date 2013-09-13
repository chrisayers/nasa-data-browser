(ns nasa-data-browser.templates)

(def parameters-mobile "
   {{#each parameters}}
     <li><a href='#filters'>{{parameter}}</a></li>
   {{/each}}
")

(def filters-mobile "
   {{#each filters}}
     <fieldset data-role='controlgroup'>
       <legend><strong>{{name}}</strong></legend>
       {{#each values}}
         <input class='filter-value' type='checkbox' 
                id='{{../filter}}#{{value}}' 
                value='{{../filter}},{{value}}'/>
         <label for='{{../filter}}#{{value}}' 
                valueName='{{valueName}}'
                filt='{{../filter}},{{value}}'>{{valueName}}</label>
       {{/each}}
       </fieldset><br>
    {{/each}}
")

(def variables-mobile "
   {{#each variables}}
     <li class='li-variable' id='li-{{variable}}'>
     <a href='#details' class='variable' var='{{variable}}'>{{variableName}}</a>
    </li>
   {{/each}}
")

(def info-mobile "
   <div><strong>Name: </strong>{{name}}</div><br>
   <strong>Facts: </strong>
   {{#each facts}}
     <div class='wordwrap indented'>
       <strong>{{relation}}: </strong>{{value}}
     </div>
   {{/each}}
")

(defn get-mobile [] 
  {"parameters" parameters-mobile
   "filters" filters-mobile
   "variables" variables-mobile
   "info" info-mobile})

(defn get-data [view]
  (cond
   (= view "mobile") (get-mobile)))