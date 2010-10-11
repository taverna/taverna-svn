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

function ajaxpage(url, containerid){
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
        loadpage(page_request, containerid, url);
    }
    if (bustcachevar){ //if bust caching of external page
        bustcacheparameter=(url.indexOf("?")!=-1)? "&"+new Date().getTime() : "?"+new Date().getTime();
    }
    page_request.open('GET', url+bustcacheparameter, true);
    page_request.send(null);
}

function loadpage(page_request, containerid, url){
    if (page_request.readyState == 4 && (page_request.status==200 || window.location.href.indexOf("http")==-1)){

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
</script>
