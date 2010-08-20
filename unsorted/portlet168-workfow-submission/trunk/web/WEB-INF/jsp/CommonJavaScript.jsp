<%-- 
    Document   : CommonJavaScript
    Created on : Aug 20, 2010, 10:43:06 AM
    Author     : Alex Nenadic
--%>

<%-- 
    JavaScript function that validates that all input fields in a form have been provided.
    Use with the <form> tag, e.g. <form action="..." onSubmit="validateForm()">
--%>

<%-- Add portlet taglibs to JSP --%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<portlet:defineObjects />

<%-- Include various constants --%>
<%@ include file="CommonConstants.jsp" %>

<script type="text/javascript">
function validateForm()
{
    var isFormValid = true;
    var alertText = "";

    // Namespace of the portlet invoking this code
    var namespace = '<%= renderResponse.getNamespace() %>';

    // workflow inputs form
    var workflowInputsForm = document.getElementsByName(namespace + "<%= WORKFLOW_INPUTS_FORM %>")[0];

    if (typeof(workflowInputsForm) != "undefined" && workflowInputsForm != null) {

        var inputElements = workflowInputsForm.getElementsByTagName("input");
        for (var i = 0; i < inputElements.length; i++) {
            //alert("Found input: " + inputElements[i].name + " with value: " + inputElements[i].value);
            if (inputElements[i].type == "text" && inputElements[i].value == ""){
                isFormValid = false;
                var inputElementName = inputElements[i].name;
                    // Get rid of the namespace prefix
                    inputElementName = inputElementName.substr(namespace.length, inputElementName.length);
                    alertText += "You have not provided a value for input '" + inputElementName + "'.\n";
                }
            }

        var textAreaElements = workflowInputsForm.getElementsByTagName("textarea");
        if (typeof(textAreaElements) != "undefined"){
            for (var i = 0; i < textAreaElements.length; i++) {
                //alert("Found input: " + textAreaElements[i].name + " with value: " + textAreaElements[i].value);
                if (textAreaElements[i].value == ""){
                    isFormValid = false;
                    var textAreaElementName = textAreaElements[i].name;
                    // Get rid of the namespace prefix
                    textAreaElementName = textAreaElementName.substr(namespace.length, textAreaElementName.length);
                    alertText += "You have not provided a value for input '" + textAreaElementName + "'.\n";
                }
            }
        }
    }

    if (isFormValid == false){
       alertText += "\nAre you sure you want to continue to run the workflow with some of the input fields empty?";
       if (confirm(alertText)){
            return true;
       }
       else{
           return false;
       }
    }
}

</script>
