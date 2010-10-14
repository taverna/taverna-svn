<%--
    JavaScript function that asks user to confirm job deletion.">
--%>

<%@ page import="net.sf.taverna.t2.portal.Constants" %>

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
