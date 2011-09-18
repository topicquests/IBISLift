/**
 * Is overridden in Conversation.scala
 */
handleSelectedNode = function() {};


/**
 * Must deal with two issues:
 * 1- loading a map
 * 2- loading a selected node
 * @param href
 * @return
 */
function handleSelection(href) {
//	alert(href);
	var url = "http://localhost:8080/conversation/show/"+href;
	alert(url);
	$.get(url, function(data) {
//		alert(data);
	if (data) {
	  var il = "<img src='"+data.icon+"'/> <b>"+data.label+"</b>";
	  var urx = "";
	  try { 
		  urx = data.source;
		  if (!urx)
			  urx = "";
		  //alert(urx);
	  } catch (E){}
	  if (urx !== null && urx !== "" && urx !== 'undefined')
		  il += "<br /><a href='"+urx+"'>"+urx+"</a><br />";
  	//	alert($('#imglabel').html());
	  $('#imglabel').html(il);
	  $('#tabs-1').html(data.details);
	  $('#tabs-6').html(data.debug);
	}}
	);
	
	//$('imglabel') for image and node label
	//$('tabs-1') for details
	//$('tabs-2') for edit
	//$('tabs-3') for respond
	//$('tabs-4') for tags
	//$('tabs-5') for connections
}
/**
function loadTree() {
	var hx = $('#TreeRaw').html();
	 $('#TreeRaw').html("");
	 var json = JSON.parse(hx);
	 var html = "";
	 html = buildTree(json.node,"");
	//alert(json.toString());
	$('#myTree').prepend(html);
	setTree();
}

function buildTree(nx, html) {
	//alert(html);
	html += "<li><a href='"+nx.id+"'><img src='"+nx.smallImage+"'/>"+nx.label+"</a>";
	var kids = nx.children;
	//alert (kids);
	if (kids) {
		var len = kids.length;
		html += "<ul>";
		for (var i=0;i<len;i++)
			html += buildTree(kids[i].node,"");
		html += "</ul>";
	}
	html += "</li>";
	return html;
}
function setTree() {
	  $("ul#myTree").columnNavigation({

		    containerPosition:"relative",

		    containerWidth:"900px",

		    containerHeight:"210px",

		    containerBackgroundColor:"rgb(255,255,255)",

		    containerFontColor:"rgb(50,50,50)",

		    columnWidth:300,

		    columnFontFamily:"'Helvetica Neue', 'HelveticaNeue', Helvetica, sans-serif",

		    columnFontSize:"90%",

		    columnSeperatorStyle:"1px solid rgb(220,220,220)",

		    columnDeselectFontWeight:"normal",

		    columnDeselectColor:"rgb(50,50,50)",

		    columnSelectFontWeight:"normal",

		    columnSelectColor:"rgb(255,255,255)",

		    columnSelectBackgroundColor:"rgb(27,115,213)",

		    //columnSelectBackgroundImage:"url('Includes/list-selected-background.jpg')",

			    //columnSelectBackgroundRepeat:"repeat-x",

		    columnSelectBackgroundPosition:"top",

		    columnItemPadding:"3px 3px 5px 3px",

		    columnScrollVelocity:50,

//    columnFontFamily                        : "Arial,sans-serif",

//    columnScrollVelocity                    : 400,

  callBackFunction                        : function(f) {

          handleSelection( f.attr("href") );

  }

});
}
*/