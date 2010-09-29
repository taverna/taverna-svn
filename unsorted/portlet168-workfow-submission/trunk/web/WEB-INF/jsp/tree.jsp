<%-- 
    Document   : tree.jsp
    Created on : Sep 27, 2010, 5:55:07 PM
    Author     : alex
--%>

<%--
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<portlet:defineObjects />
--%>

<% String contextPath = response.encodeURL(request.getContextPath()) ; %>

<!-- define global varibale containing a path to image visible from the script to be included below -->
<script type="text/javascript">
    var imagesPath = "<%=response.encodeURL(request.getContextPath() + "/images/")%>";
</script>

<link href="<%= contextPath %>/css/tree.css" rel="stylesheet" type="text/css" />
<script src="<%= contextPath %>/js/tree.js" language="javascript"></script>

<%--
<%@ include file="/WEB-INF/jsp/treeCSS.jsp" %>
<%@ include file="/WEB-INF/jsp/treeJavaScript.jsp" %>
--%>

        <br/>
        <script language="javascript">
            addNode("WickedOrange.com","http://www.wickedorange.com","mainFrame");

            addNode("Google","http://www.google.com","mainFrame");
            startParentNode("Search Engines");
                    addNode("Google","http://www.google.com","mainFrame");
                    addNode("Yahoo","http://www.yahoo.com","mainFrame");
            endParentNode();

            startParentNode("Dev Sites");
                    addNode("CodeProject","http://www.codeproject.com","mainFrame");
                    addNode("MSDN","http://www.msdn.com","mainFrame");
            startParentNode("Dev Sites");
                            addNode("CodeProject","http://www.codeproject.com","mainFrame");
                            addNode("MSDN","http://www.msdn.com","mainFrame");
                    startParentNode("Dev Sites");
                                    addNode("CodeProject","http://www.codeproject.com","mainFrame");
                                    addNode("MSDN","http://www.msdn.com","mainFrame");
                            endParentNode();
                    endParentNode();
            endParentNode();
        </script>
