<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<%-- Uncomment below lines to add portlet taglibs to jsp
<%@ page import="javax.portlet.*"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>

<portlet:defineObjects />
<%PortletPreferences prefs = renderRequest.getPreferences();%> 
--%>

<script type="text/javascript">

/***********************************************
* Dynamic Ajax Content- © Dynamic Drive DHTML code library (www.dynamicdrive.com)
* This notice MUST stay intact for legal use
* Visit Dynamic Drive at http://www.dynamicdrive.com/ for full source code
***********************************************/

var bustcachevar=1 //bust potential caching of external pages after initial request? (1=yes, 0=no)
var loadedobjects=""
var rootdomain="http://"+window.location.hostname
var bustcacheparameter=""

function ajaxpage(url, containerid){
var page_request = false
if (window.XMLHttpRequest) // if Mozilla, Safari etc
page_request = new XMLHttpRequest()
else if (window.ActiveXObject){ // if IE
try {
page_request = new ActiveXObject("Msxml2.XMLHTTP")
}
catch (e){
try{
page_request = new ActiveXObject("Microsoft.XMLHTTP")
}
catch (e){}
}
}
else
return false
page_request.onreadystatechange=function(){
    loadpage(page_request, containerid, url);
}
if (bustcachevar) //if bust caching of external page
bustcacheparameter=(url.indexOf("?")!=-1)? "&"+new Date().getTime() : "?"+new Date().getTime()
page_request.open('GET', url+bustcacheparameter, true)
page_request.send(null)
}

function loadpage(page_request, containerid, url){
    if (page_request.readyState == 4 && (page_request.status==200 || window.location.href.indexOf("http")==-1)){

       //document.getElementById(containerid).innerHTML="<object type=\""+mime_type+"\" data=\""+url+"\"></object><br>You can also <a target=\"_blank\" href=\""+url+"\">view the data in a separate window</a>.<br>To download the value, right click on the link and choose 'Save Link As'.";

        var mime_type = get_url_parameter_value(url, "mime_type");
        if (typeof(mime_type) == "undefined"){
            document.getElementById(containerid).innerHTML="MIME type of the data value is undefined - cannot preview the value.<br>Try saving <a target=\"_blank\" href=\""+url+"\">the data value</a> and viewing it in an external application.";
        }
        else if (mime_type.indexOf("text/") === 0){
            document.getElementById(containerid).innerHTML="<pre>"+page_request.responseText+"</pre><br><br>View <a target=\"_blank\" href=\""+url+"\">the data</a> in a separate window or download it by right-clicking on the link and choosing 'Save Link As'.";
        }
        else if (mime_type.indexOf("image/") === 0){
            document.getElementById(containerid).innerHTML="<img src=\""+url+"\" alt=\"If you see this text - you are trying to view image of type "+mime_type+" which your browser cannot display properly.\"/><br><br>View <a target=\"_blank\" href=\""+url+"\">the image</a> in a separate window or download it by right-clicking on the link and choosing 'Save Link As'.";
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
        var file=arguments[i]
        var fileref=""
if (loadedobjects.indexOf(file)==-1){ //Check to see if this object has not already been added to page before proceeding
if (file.indexOf(".js")!=-1){ //If object is a js file
fileref=document.createElement('script')
fileref.setAttribute("type","text/javascript");
fileref.setAttribute("src", file);
}
else if (file.indexOf(".css")!=-1){ //If object is a css file
fileref=document.createElement("link")
fileref.setAttribute("rel", "stylesheet");
fileref.setAttribute("type", "text/css");
fileref.setAttribute("href", file);
}
}
if (fileref!=""){
document.getElementsByTagName("head").item(0).appendChild(fileref)
loadedobjects+=file+" " //Remember this object as being already added to page
}
}
}

function get_url_parameter_value( url, parameter )
{
  parameter = parameter.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
  var regexS = "[\\?&]"+parameter+"=([^&#]*)";
  var regex = new RegExp( regexS );
  var results = regex.exec( url );
  if( results == null )
    return "";
  else
    return results[1];
}

</script>

<b>
    AjaxTestPortlet - VIEW MODE<br>
    <% String url = "/T2WorkflowsInPortals/FileServingServlet?data_file_path=%2FUsers%2Falex%2FDesktop%2Fportal-results%2FT2WorkflowSubmissionJobs%2Fanonymous%2F7fb90669-8c08-4718-9461-a1615c287e77%2Foutputs%2Fout%2FList%2FValue1&mime_type=text%2Fplain" ;%>
    <a href="javascript:ajaxpage(rootdomain + '<%=url%>', 'contentarea')" onclick="ajaxpage(rootdomain +'<%=url%>', 'contentarea');">test</a>
    <div id="contentarea"></div>
    <br>
    <a href="javascript:ajaxpage('http://localhost:8080/T2WorkflowsInPortals/images/delete.png', 'contentarea2');">image-fixed-test</a>
    <div id="contentarea2"></div>
    <br>
    <a href="javascript:ajaxpage('http://localhost:8080/T2WorkflowsInPortals/FileServingServlet?data_file_path=%2FUsers%2Falex%2FDesktop%2Fportal-results%2FT2WorkflowSubmissionJobs%2Fanonymous%2Fd914c233-310b-42c7-a01e-b6115c4732a3%2Foutputs%2Fout%2FList%2FValue1&mime_type=text%2Fplain', 'contentarea3');">text-test</a>
    <div id="contentarea3"></div>
    <br>
    <a href="javascript:ajaxpage('http://localhost:8080/T2WorkflowsInPortals/FileServingServlet?data_file_path=%2FUsers%2Falex%2FDesktop%2Fportal-results%2FT2WorkflowSubmissionJobs%2Fanonymous%2F095ee6cb-4a3e-4b50-877a-f95d471c0643%2Foutputs%2Fgif%2FValue&mime_type=image%2Fgif', 'contentarea4');">image-gif-test</a>
    <div id="contentarea4"></div>
    <br>
    <a href="javascript:ajaxpage('http://localhost:8080/T2WorkflowsInPortals/FileServingServlet?data_file_path=%2FUsers%2Falex%2FDesktop%2Fportal-results%2FT2WorkflowSubmissionJobs%2Fanonymous%2F095ee6cb-4a3e-4b50-877a-f95d471c0643%2Foutputs%2Ftiff%2FValue&mime_type=image%2Ftiff', 'contentarea5');">image-tiff-test</a>
    <div id="contentarea5"></div>
</b>