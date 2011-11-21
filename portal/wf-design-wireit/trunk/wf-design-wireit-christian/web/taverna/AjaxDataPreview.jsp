<%@ page import="net.sf.taverna.portal.FileServingServlet" %>

<script type="text/javascript">

/***********************************************
* Dynamic Ajax Content- © Dynamic Drive DHTML code library (www.dynamicdrive.com)
* This notice MUST stay intact for legal use
* Visit Dynamic Drive at http://www.dynamicdrive.com/ for full source code
***********************************************/

var bustcachevar=1; //bust potential caching of external pages after initial request? (1=yes, 0=no)
var loadedobjects="";
var rootdomain="http://"+window.location.hostname;
var bustcacheparameter="";

function ajaxpage(textarea_id, url, containerid){
    var page_request = false;
    if (window.XMLHttpRequest){ // if Mozilla, Safari etc
        page_request = new XMLHttpRequest();
    }
    else if (window.ActiveXObject){ // if IE
        try {
            page_request = new ActiveXObject("Msxml2.XMLHTTP");
        }
        catch (e){
            try{
                page_request = new ActiveXObject("Microsoft.XMLHTTP");
            }
            catch (e){}
            }
        }
    else{
        return false;
    }
    page_request.onreadystatechange=function(){
        loadpage(textarea_id, page_request, containerid, url);
    }
    if (bustcachevar){ //if bust caching of external page
        bustcacheparameter=(url.indexOf("?")!=-1)? "&"+new Date().getTime() : "?"+new Date().getTime();
    }
    page_request.open('GET', url+bustcacheparameter, true);
    page_request.send(null);
}

<% long MAX_PREVIEW_DATA_SIZE_IN_KB = FileServingServlet.MAX_PREVIEW_DATA_SIZE_IN_KB; %>

function loadpage(textarea_id, page_request, containerid, url){
    if (page_request.readyState == 4 && (page_request.status==200 || window.location.href.indexOf("http")==-1)){

        var mime_type = get_url_parameter_value(url, "<%= FileServingServlet.MIME_TYPE %>");
        var data_size_in_kb = get_url_parameter_value(url, "<%= FileServingServlet.DATA_SIZE_IN_KB %>");

        if (data_size_in_kb > <%=MAX_PREVIEW_DATA_SIZE_IN_KB%>){
            document.getElementById(containerid).innerHTML="The size of the data (~"+data_size_in_kb+" KB) is too big to preview.<br>"+
                "You may try to view the <a target=\"_blank\" href=\""+url+"\">data</a> in a separate browser window, or save the data (right-click on the link then choose 'Save Link As') and view it in an external application.";
            return;
        }

        if (typeof(mime_type) == "undefined"){
            document.getElementById(containerid).innerHTML="MIME type of the data value is undefined - cannot preview the value.<br>Try saving <a target=\"_blank\" href=\""+url+"\">the data value</a> and viewing it in an external application.";
        }
        else if (mime_type.indexOf("text/") === 0 || mime_type.indexOf("application/xml") === 0 || mime_type.indexOf("application/octet-stream") === 0){
            document.getElementById(containerid).innerHTML="<textarea id=\""+textarea_id+"\" readonly='true' style=\"width:100%; overflow:visible;\">"+
                page_request.responseText+"</textarea><br><br>View <a target=\"_blank\" href=\""+url+
                "\">the data</a> in a separate browser window or download it by right-clicking on the link and choosing 'Save Link As'.";
            adjustRows(document.getElementById(textarea_id));
        }
        else if (mime_type.indexOf("image/") === 0){
            document.getElementById(containerid).innerHTML="<a href=\""+url+"\" target=\"_blank\"><img src=\""+url+
                "&thumbnail=yes\" alt=\"Loading image preview ... \"/></a><br><br>View the <a target=\"_blank\" href=\""+url+
                "\">full image</a> in a separate browser window or download it by right-clicking on the link and choosing 'Save Link As'.";
        }
        else if (mime_type == "application/octet-stream"){
            document.getElementById(containerid).innerHTML="Cannot preview binary data. Try saving <a href=\""+url+"\">the data value</a> and viewing it in an external application.";
        }
        else{
            document.getElementById(containerid).innerHTML="Cannot preview data of type "+mime_type+". Try saving <a href=\""+url+"\">the data value</a> and viewing it in an external application.";
        }
    }
}

function loadobjs(){
    if (!document.getElementById){
        return;
    }
    for (i=0; i<arguments.length; i++){
        var file=arguments[i];
        var fileref="";
        if (loadedobjects.indexOf(file)==-1){ //Check to see if this object has not already been added to page before proceeding
            if (file.indexOf(".js")!=-1){ //If object is a js file
                fileref=document.createElement('script');
                fileref.setAttribute("type","text/javascript");
                fileref.setAttribute("src", file);
            }
            else if (file.indexOf(".css")!=-1){ //If object is a css file
                fileref=document.createElement("link");
                fileref.setAttribute("rel", "stylesheet");
                fileref.setAttribute("type", "text/css");
                fileref.setAttribute("href", file);
            }
        }
        if (fileref!=""){
            document.getElementsByTagName("head").item(0).appendChild(fileref);
            loadedobjects+=file+" "; //Remember this object as being already added to page
        }
    }
}

function get_url_parameter_value( url, parameter )
{
  parameter = parameter.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
  var regexS = "[\\?&]"+parameter+"=([^&#]*)";
  var regex = new RegExp( regexS );
  var results = regex.exec( url );
  if( results == null ){
    return "";
  }
  else{
    return results[1];
  }
}

<%--
    Adjust row number on the textarea based on the text size.
    Adapted from http://perplexed.co.uk/596_expanding_textarea_as_you_type.htm
--%>

function adjustRows(textArea){

    if (navigator.appName.indexOf("Microsoft Internet Explorer") == 0)
    {
        textArea.style.overflow = 'visible';
        return;
    }

    while (textArea.rows > 1 && textArea.scrollHeight < textArea.offsetHeight){
        textArea.rows--;
    }

    while (textArea.scrollHeight > textArea.offsetHeight){
        textArea.rows++;
    }

    textArea.rows++;
    return;
}

<%--
    Create thumbnail of the image.
    http://carso-owen.blogspot.com/2009/02/create-thumbnail-image-using-jquery-or.html
--%>
//$(document).ready(
    function autoImageResize(src, fixedSize) {
        var width = src.width;
        var height = src.height;
        var ratio = width / height;
        if (width > fixedSize) {
            src.width = fixedSize;
        }
        if (height > fixedSize) {
            var sizedwidth = fixedSize / ratio;
            var sizedheight = fixedSize / ratio;
            if (height > width) {
                if (height > sizedwidth) {
                    src.height = fixedSize
                }
                if (sizedwidth > fixedSize) {
                    src.width = src.width * ratio;
                }
                else {
                    src.height = src.height * ratio;
                }
            }
            else {
                src.width = fixedSize;
            }
        }
    }
//);
</script>
