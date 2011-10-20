/**
 *  jQuery Column Navigation Plugin
 *	
 *	version 1.1.0
 *	
 *	Written by Sam Clark
 *	http://sam.clark.name
 *	
 *
 *	!!! NOTICE !!!
 *	This library and related library requires jQuery 1.2.6 or higher
 *	http://www.jquery.com
 *
 *	This library requires the ScrollTo plugin for jQuery by Flesler
 *	http://plugins.jquery.com/project/ScrollTo
 *
 *	The MIT License
 *
 *	Copyright (c) 2008 Polaris Digital Limited
 *
 *	Permission is hereby granted, free of charge, to any person obtaining a copy
 *	of this software and associated documentation files (the "Software"), to deal
 *	in the Software without restriction, including without limitation the rights
 *	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *	copies of the Software, and to permit persons to whom the Software is
 *	furnished to do so, subject to the following conditions:
 *
 *	The above copyright notice and this permission notice shall be included in
 *	all copies or substantial portions of the Software.
 *
 *	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *	THE SOFTWARE.
 *
 *
 *	Quick Example
 *	=============================================================================
 *	
 *	The column navigation plugin is very quick an easy to use. It provides a very
 *	fast way to arrange and interface with large hierarchial sets of data in a
 *	familiar interface, especially for Mac OS X users.
 *	
 *	You require a unordered list currently (more to follow in later versions) with
 *	nested unordered lists. Each nesting will create a new level within the tree.
 *	
 *	HTML Example
 *	------------
 *	
 *	<html>
 *	<body>
 *	<div id="myTree">
 *		<ul>
 *			<div>	<!-- required to allow scrolling within each column -->
 *				<li>
 *					<a href="./">Homepage</a>
 *					<ul>
 *						<div>
 *							<li><a href="./contact">Contact</a></li>
 *							<li><a href="./tsandcs">Terms &amp; Conditions</a></li>
 *							<li><a href="./privacy">Privacy information</a></li>
 *						</div>
 *					</ul>
 *				</li>
 *				<li>
 *					<a href="./contents">Contents</a>
 *					<ul>
 *						<div>
 *							<li><a href="./page1/">Page 1</a></li>
 *							<li>
 *								<a href="./page2/">Page 2</a>
 *								<ul>
 *									<div>
 *									<li><a href="./page2.1/">Page 2.1</a></li>
 *									<li><a href="./page2.2/">Page 2.2</a></li>
 *									</div>
 *								</ul>
 *							</li>
 *							<li><a href="./page3/">Page 3</a></li>
 *						</div>
 *					</ul>
 *				</li>
 *			</div>
 *		</ul>
 *	</div>
 *	</body>
 *	</html>
 *	
 *	
 *	Javascript Example
 *	------------------
 *	
 *	$("div#myTree").columnNavigation();
 *	
 *	
 *	Options
 *	-------
 *	This plugin takes a large number of configuration options, all are defaulted for quick access.
 *	You can control the styling properties of almost every attribute of style and animation.
 *	
 *	All configuration items should be declared on initialisation.
 *	
 *	---
 *	
 *	$("div#myTree").conlumnNavigation({
 *		containerBackgroundColor	: "rgb(255,255,255)",
 *		columnFontFamily			: "Arial,sans-serif",
 *		columnScrollVelocity		: 400,
 *		callBackFunction			: function() {
 *			alert( $(linkObject).attr("href") );
 *		}
 *	});
 *	
 *	---
 *	
 *	The example above sets some attributes on the column navigation object.
 *	
 *	Notice the callBackFunction. You can attach an additional function to act.
 *	The callback function can be called on the dblClick event if you supply one, the
 *	'a' element is passed as the object to your handler.
 *
 **/

(function($){
	$.fn.columnNavigation = function( configuration )
	{
		// Check for incoming ul or ol element
		if( $(this).get(0).tagName != "UL" && $(this).get(0).tagName != "OL" )
		{
			alert( "FATAL ERROR: columnNavigation requires an UL or OL element\nYou supplied a : " + $(this).get(0).tagName );
			return false;
		}
				
		// Setup the selectors
		if( $(this).get(0).tagName == "UL" )
		{
			var selectorName = "ul";
		}
		else if( $(this).get(0).tagName == "OL" )
		{
			var selectorName = "ol";
		}
		
		// Wrap the submitted element in a new div
		$(this).wrap( document.createElement("div") );
		
		var wrapper = $(this).parent();
		
		
		// Setup the column navigation object with configuration settings
		// Overright existing settings where applicable
		configuration = $.extend({
			containerPosition:"relative",
			containerTop:"",
			containerLeft:"",
			containerPadding:"0",
			containerMargin:"0",
			containerWidth:"400px",
			containerHeight:"250px",
			containerBackgroundColor:"",
			containerBackgroundImage:"",
			containerBackgroundRepeat:"",
			containerBackgroundPosition:"",
			containerBorder:"1px solid rgb(178,178,178)",
			columnWidth:250,
			columnFontFamily:"'Helvetica Neue', ''HelveticaNeue', Helvetica, sans-serif",
			columnFontSize:"90%",
			columnSeperatorStyle:"1px solid rgb(220,220,220)",
			columnDeselectFontWeight:"normal",
			columnDeselectColor:"rgb(50,50,50)",
			columnDeselectBackgroundColor:"",
			columnDeselectBackgroundImage:"",
			columnDeselectBackgroundRepeat:"",
			columnDeselectBackgroundPosition:"",
			columnSelectFontWeight:"normal",
			columnSelectColor:"rgb(255,255,255)",
			columnSelectBackgroundColor:"rgb(27,115,213)",
			columnSelectBackgroundImage:"",
			columnSelectBackgroundRepeat:"",
			columnSelectBackgroundPosition:"",
			columnItemPadding:"3px 3px 5px 3px",
			columnScrollVelocity:200,
			callBackFunction:null
		}, configuration);
		
		// check callback is a function if set
		if( configuration.callBackFunction != null && jQuery.isFunction( configuration.callBackFunction ) == false )
		{
			alert( 'FATAL ERROR: columnNavigation.callBackFunction() is not a function!' );
			return false;
		}
				
		// Setup the container space using the settings
		$(wrapper).css({
			position:configuration.containerPosition,
			top:configuration.containerTop,
			left:configuration.containerLeft,
			padding:configuration.containerPadding,
			margin:configuration.containerMargin,
			width:configuration.containerWidth,
			height:configuration.containerHeight,
			backgroundColor:configuration.containerBackgroundColor,
			backgroundImage:configuration.containerBackgroundImage,
			backgroundPosition:configuration.containerBackgroundPosition,
			backgroundRepeat:configuration.containerBackgroundRepeat,
			border:configuration.containerBorder,
			overflowX:"auto",
			overflowY:"auto"
		});
		
		// LI element deselect state
		var liDeselect = {
			backgroundColor:configuration.columnDeselectBackgroundColor,
			backgroundImage:configuration.columnDeselectBackgroundImage,
			backgroundRepeat:configuration.columnDeselectBackgroundRepeat,
			backgroundPosition:configuration.columnDeselectBackgroundPosition
		};
		
		// LI element select state
		var liSelect = {
			backgroundColor:configuration.columnSelectBackgroundColor,
			backgroundImage:configuration.columnSelectBackgroundImage,
			backgroundRepeat:configuration.columnSelectBackgroundRepeat,
			backgroundPosition:configuration.columnSelectBackgroundPosition			
		};
		
		// A element deselect state
		var aDeselect = {
			color:configuration.columnDeselectColor,
			fontFamily:configuration.columnFontFamily,
			fontSize:configuration.columnFontSize,
			textDecoration:"none",
			fontWeight:"normal",
			outline:"none",
			width:"100%",
			display:"block"
		};
		
		// A element select state
		var aSelect = {
			color:configuration.columnSelectColor,
			textDecoration:"none"
		};
		
		// Discover the real container position
		var containerPosition = $(wrapper).find("ul:first").offset();
		var containerSize = $(wrapper).width();
				
		// Setup the column width as a string (for CSS)
		var columnWidth = configuration.columnWidth + "px";
		
		var myself = $(wrapper);

		// Hide and layout children beneath the first level
		$(wrapper).find(selectorName+":first").find("ul").css({
			left:columnWidth,
			top:"0px",
			position:"absolute"
		}).hide();

		// Style the columns
		$(wrapper).find(selectorName).css({
			position:"absolute",
			width:columnWidth,
			height:"100%",
			borderRight:configuration.columnSeperatorStyle,
			padding:"0",
			margin:"0"
		});
		
		// Create the additional required divs
		//$(wrapper).find(selectorName).wrapInner(document.createElement("div"));
		
		// Ensure each level can scroll within the container
		$(wrapper).find(selectorName+" div").css({
			height:"100%",
			overflowX:"visible",
			overflowY:"visible"
		});
				
		// Style the internals
		$(wrapper).find(selectorName+" li").css({
			listStyle:"none",
			padding:configuration.columnItemPadding,
			backgroundColor:configuration.columnDeselectBackgroundColor,
			backgroundImage:configuration.columnDeselectBackgroundImage,
			backgroundRepeat:configuration.columnDeselectBackgroundRepeat,
			backgroundPosition:configuration.columnDeselectBackgroundPosition
		});
		
		// Style the unselected links (this overrides specific CSS styles on the page)
		$(wrapper).find(selectorName+" a").css(
			aDeselect
			);		
		
		// Setup the onclick function for each link within the tree
		$('.nodehref').live('click', function(){
			
			// Discover where this element is on the page
			var licoords = $(this).parent().offset();			// li position
			
			// Hide lower levels
			$(this).parent().siblings().find(selectorName).hide();
			
			// Deselect other levels
			$(this).parent().siblings().css( liDeselect );						

			// Deselect other levels children
			$(this).parent().siblings().find("li").css( liDeselect );
			
			// Deselect other a links
			$(this).parent().siblings().find("a").css( aDeselect );
			
			// Show child menu
			$(this).parent().find(selectorName+":first").show();
			
			// Select this level
			$(this).parent().css( liSelect );
			
			// Highlight the text if required
			$(this).css( aSelect );
			
			// Add scrolling if required
			if( (licoords.left - containerPosition.left + ( ( configuration.columnWidth * 2 ) - 1 ) > containerSize ) )
			{	
				// Calculate differnce
				var difference = '+=' + (((licoords.left - containerPosition.left + ( ( configuration.columnWidth * 2 ) - 1) ) ) - containerSize );
				
				scrollToLocale( difference );
			}
			// If there is no callback function, use the existing link
			if( configuration.callBackFunction == null )
			{
				window.location = $(this).attr("href");
			}
			else
			{
			// Otherwise attach this link to a variable and send it to the callBackFunction for processing
				var linkObject = $(this);
				configuration.callBackFunction( linkObject );
			}

			return false;
		});
		
		// Double decides on task.
		$('.nodehref').live('dblclick', function(){
			
			// If there is no callback function, use the existing link
//			if( configuration.callBackFunction == null )
//			{
//				window.location = $(this).attr("href");
//			}
//			else
//			{
//			// Otherwise attach this link to a variable and send it to the callBackFunction for processing
//				var linkObject = $(this);
//				configuration.callBackFunction( linkObject );
//			}
		});
		
		// Scrolls the main view
		function scrollToLocale( difference )
		{
			myself.scrollTo( difference, configuration.columnScrollVelocity, {axis:'x'} );			
		}
	}
})
(jQuery);