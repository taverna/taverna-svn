<%--
    JavaScript function that asks user to confirm job deletion.">
--%>

<%-- Add portlet taglibs to JSP --%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<portlet:defineObjects />

<%@ page import="net.sf.taverna.t2.portal.Constants" %>

<%-- Include various constants --%>
<%--<%@ include file="CommonConstants.jsp" %>--%>

<script type="text/javascript">
function confirm_deletion(confirmText)
{
    if (confirm(confirmText)){
        return true;
    }
    else{
       return false;
    }
}
</script>
