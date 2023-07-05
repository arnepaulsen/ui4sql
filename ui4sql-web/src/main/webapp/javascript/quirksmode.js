/**
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */

var bugRiddenCrashPronePieceOfJunk = (
	navigator.userAgent.indexOf('MSIE 5') != -1
	&&
	navigator.userAgent.indexOf('Mac') != -1
);

var W3CDOM = (!bugRiddenCrashPronePieceOfJunk && 
	document.getElementsByTagName && document.createElement);

if ((top != self.parent) && !self.disabled)
	top.location.href = '/index.html';

if (top.navi && top.navi.setNav) top.navi.setNav(location.href,'currentPage');
top.setNav = location.href;


window.onload = initialize; 

/* Why no window.onload = function () {} ? Because NN3 doesn't support the function
	constructor and gives an error message. This site must be accessible to NN3 */

function initialize () {

	alert("initialize");
	

	
/* Hide nifty stuff from old browsers */

	if (W3CDOM)
	
	{

/* Go through all links. If any has a type="popup" write the popup function into its onclick
   Any external link gets a target='ppk'. 
   Any link with a hreflang attribute gets an extra note with its value.   
   */
	var langspan = document.createElement('span');
	langspan.className = 'smaller lang';
	
	var x = document.getElementsByTagName('a');
	for (var i=0;i<x.length;i++)
	{
		if (x[i].getAttribute('type') == 'popup')
		{
			x[i].onclick = function () {
				return pop(this.href)
			}
			x[i].innerHTML += '<span class="smaller"> (popup)</span>';
		}
		if (x[i].className == 'external')
			x[i].target = 'ppk';
		if (x[i].className == 'outoforder')
		{
			x[i].title = 'OUT OF ORDER';
			x[i].target = 'ppk';
			x[i].onclick = function ()
			{
				return confirm('This link is out of order. Continue anyway?');
			}
		}
		var hreflang = x[i].getAttribute('hreflang');
		if (hreflang)
		{
			var newspan = langspan.cloneNode(true);
			newspan.appendChild(document.createTextNode(' (lang=' + hreflang + ')'));
			x[i].parentNode.insertBefore(newspan,x[i].nextSibling);
			
		}
	}
	
/* Go through all link tags and create a navigation bar from their data */

	var y = document.getElementsByTagName('link');
	var links = '';
	for (var i=0;i<y.length;i++)
	{
		if (y[i].getAttribute('rel') == 'stylesheet') continue;
		if (y[i].getAttribute('rel') == 'alternate') continue;
		links +=  ' <a href="' + y[i].getAttribute('href') + '">' + y[i].getAttribute('rel') + '</a>';
	}
	links += '<br>';
	links += '<a href="/home.shtml">home</a>';
	links += '<a href="/sitemap.html">sitemap</a>';
	links += '<a href="/contact.html">contact</a>';
	links += '<a href="/about/copyright.html">copyright</a>';
	
/* Write navigation bar and last modifier information into p id="header".
   If the page is not in my frameset, add note and link to that effect. */
	
	if (document.getElementById('header'))
	{
		document.getElementById('header').innerHTML = lastMod(); //+ links;
		if (!top.quirksMode)
			document.getElementById('header').innerHTML += '<br>This page is supposed to be in my <a href="/index.html?' + location.pathname + '" target="_top">frameset</a>.'; 
	}

/* Write message and navigation bar into div id="footer" */

	if (document.getElementById('footer'))
	{
		var valid = document.getElementById('validation');
		if (valid)
		{
			valid = valid.cloneNode(true);
			valid.onmouseover = function () {
				this.style.color = '#333333';
			}
			valid.onmouseout = function () {
				this.style.color = '';
			}
			
		}
		document.getElementById('footer').innerHTML = links;
		if (valid)
			document.getElementById('footer').appendChild(valid);
	}

/* Add breadcrumb of current page to logo frame */
		
	if (top.logo && top.logo.addBreadCrumb)
		top.logo.addBreadCrumb(document.title,location.href);

/* Add IE version to page title */

	var browser = '';
	
	if (document.all)
	{
		detect = navigator.userAgent.toLowerCase();

		if (checkItLocal('msie')) 
		{
			browser = "IE "
			browser += detect.substr(placeOfDetect + thestring.length,3);
			document.title = browser + ' - ' + document.title;
		}
	}

/* Create a table of contents */

	if (browser != 'IE 5.0') // IE 5.0 Win hides all floats and most long code examples when we run this script
		createTOC();

//	doCompatScore();

/* End hide. This is for all browsers 
 If the page has an init() function, execute it */

	}

	
	if (self.init) self.init();
}

window.onunload = remove;

function remove () {
	if (top.navi && top.navi.setNav) top.navi.setNav(location.href,'');
	top.setNav = '';
	if (self.exit) self.exit();
}

// Table of Contents

function createTOC()
{
	if (top.bugRiddenCrashPronePieceOfJunk) return;
	var x = document.body.childNodes;
	var y = document.createElement('div');
	y.id = 'toc';
	var a = y.appendChild(document.createElement('span'));
	a.onclick = showhideTOC;
	a.className = 'contentheader';
	a.innerHTML = 'Contents';
	var z = y.appendChild(document.createElement('div'));
	z.onclick = showhideTOC;
	var toBeTOCced = new Array();
	for (var i=0;i<x.length;i++)
	{
		var test = x[i].nodeName.indexOf('H')+1;
		if (test && x[i].nodeName.substring(1) < 5)
		{
			toBeTOCced.push(x[i])
		}
	}
	
	if (toBeTOCced.length < 2) return;
	
	var tmp = document.createElement('a');
	tmp.appendChild(document.createTextNode('Explanation'));
	tmp.title = 'How the TOC script works';
	tmp.href = '/dom/toc.html';
	tmp.className = 'explanation';
	z.appendChild(tmp);

	for (var i=0;i<toBeTOCced.length;i++)
	{
		var tmp = document.createElement('a');
		tmp.innerHTML = toBeTOCced[i].innerHTML;
		tmp.href = '#link' + i;
		tmp.className = 'page';
		z.appendChild(tmp);
		if (toBeTOCced[i].nodeName == 'H4')
			tmp.className += ' indent';
		var tmp2 = document.createElement('a');
		tmp2.id = 'link' + i;
		if (toBeTOCced[i].nodeName == 'H2')
		{
			tmp.innerHTML = 'Top';
			tmp.href = '#top';
			tmp2.id = 'top';
		}
		toBeTOCced[i].parentNode.insertBefore(tmp2,toBeTOCced[i]);
	}
	document.body.insertBefore(y,document.body.childNodes[2]);
}

var TOCstate = 'none';

function showhideTOC()
{
	TOCstate = (TOCstate == 'none') ? 'block' : 'none';
	document.getElementById('toc').lastChild.style.display = TOCstate;
	
}

// Compatibility score

var compatPoints = new Object();
compatPoints['yes'] = 5;
compatPoints['almost'] = 4;
compatPoints['incomplete'] = 3;
compatPoints['alternative'] = -4;
compatPoints['incorrect'] = -2;
compatPoints['buggy'] = -3;
compatPoints['crash'] = -5;

function doCompatScore()
{
	var x = document.getElementsByTagName('table');
	for (var i=0;i<x.length;i++)
	{
		if (x[i].className != 'compatibility') continue;
		var y = x[i].getElementsByTagName('tr');
		for (var j=0;j<y.length;j++)
		{
			var compatScore = 0;
			var counter = 0;
			var z = y[j].getElementsByTagName('td');
			for (var k=0;k<z.length;k++)
			{
				var ind = z[k].className.indexOf('comp ')+1;
				if (!ind) continue;
				counter++;
				var factor = 1;
				if (counter < 3) 
					factor = 2;
				var classification = z[k].className.substring(ind+4);
				if (compatPoints[classification])
				{
					compatScore += compatPoints[classification]*factor
				}
			}
			if (counter)
			{
				var sp = document.createElement('span');
				var assessment = '';
				if (compatScore > 35)
					assessment = 'good';
				else if (compatScore > 25)
					assessment = 'ok';
				else if (compatScore > 10)
					assessment = 'problems';
				else if (compatScore > 0)
					assessment = 'bad';
				else
					assessment = 'terrible';
				sp.className = 'compat ' + assessment;
				sp.appendChild(document.createTextNode('Overall:'));
				sp.appendChild(document.createElement('br'));
				sp.appendChild(document.createTextNode(assessment + ' (' + compatScore + ')'));
				z[0].appendChild(sp);
			}
		}
	}
}


// Last modified

function lastMod()
{
	var x = new Date (document.lastModified);
	Modif = new Date(x.toGMTString());
	Year = takeYear(Modif);
	Month = Modif.getMonth();
	Day = Modif.getDate();
	Mod = (Date.UTC(Year,Month,Day,0,0,0))/86400000;
	x = new Date();
	today = new Date(x.toGMTString());
	Year2 = takeYear(today);
	Month2 = today.getMonth();
	Day2 = today.getDate();
	now = (Date.UTC(Year2,Month2,Day2,0,0,0))/86400000;
	daysago = now - Mod;
	if (daysago < 0) return '';
	unit = 'days';
	if (daysago > 730)
	{
		daysago = Math.floor(daysago/365);
		unit = 'years';
	}
	else if (daysago > 60)
	{
		daysago = Math.floor(daysago/30);
		unit = 'months';
	}
	else if (daysago > 14)
	{
		daysago = Math.floor(daysago/7);
		unit = 'weeks'
	}
	var towrite = 'Page last changed ';
	if (daysago == 0) towrite += 'today';
	else if (daysago == 1) towrite += 'yesterday';
	else towrite += daysago + ' ' + unit + ' ago';
	return towrite;
}


function takeYear(theDate)
{
	var x = theDate.getYear();
	var y = x % 100;
	y += (y < 38) ? 2000 : 1900;
	return y;
}

// Popup

var popUp = null;

function pop(url)
{
	if (popUp && !popUp.closed)
		popUp.location.href = url;
	else
		popUp = window.open(url,'popUp','height=500,width=700,scrollbars=yes,resizable=yes,toolbar=yes,location=yes');
	popUp.focus();
	return false;
}

// Cookies

function createCookie(name,value,days)
{
	if (days)
	{
		var date = new Date();
		date.setTime(date.getTime()+(days*24*60*60*1000));
		var expires = "; expires="+date.toGMTString();
	}
	else var expires = "";
	document.cookie = name+"="+value+expires+"; path=/";
}

function readCookie(name)
{
	var nameEQ = name + "=";
	var ca = document.cookie.split(';');
	for(var i=0;i < ca.length;i++)
	{
		var c = ca[i];
		while (c.charAt(0)==' ') c = c.substring(1,c.length);
		if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
	}
	return null;
}

function eraseCookie(name)
{
	createCookie(name,"",-1);
}

function checkItLocal(string)
{
	placeOfDetect = detect.indexOf(string) + 1;
	thestring = string;
	return placeOfDetect;
}


// push and shift for IE5

function Array_push() {
	var A_p = 0
	for (A_p = 0; A_p < arguments.length; A_p++) {
		this[this.length] = arguments[A_p]
	}
	return this.length
}

if (typeof Array.prototype.push == "undefined") {
	Array.prototype.push = Array_push
}

function Array_shift() {
	var A_s = 0
	var response = this[0]
	for (A_s = 0; A_s < this.length-1; A_s++) {
		this[A_s] = this[A_s + 1]
	}
	this.length--
	return response
}

if (typeof Array.prototype.shift == "undefined") {
	Array.prototype.shift = Array_shift
}