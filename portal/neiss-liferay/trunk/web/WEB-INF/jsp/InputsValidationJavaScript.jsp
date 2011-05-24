<%-- 
    JavaScript function that validates that all input fields in a form have been provided.
    Use with the <form> tag, e.g. <form action="..." onSubmit="validateForm()">
--%>

<%-- Add portlet taglibs to JSP --%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<portlet:defineObjects />

<%@ page import="net.sf.taverna.t2.portal.Constants" %>

<script type="text/javascript">
function validateForm(workflowInputsForm)
{
    // Namespace of the portlet invoking this code
    var namespace = "<%= renderResponse.getNamespace() %>";

    var inputPortNames = new Array();

    var trElements = workflowInputsForm.getElementsByTagName("tr");
    var inputPortNames = new Array();
    // Find all the input port names
    var j=0;
    for (var i = 0; i < trElements.length; i++) {
        if (typeof(trElements[i].getAttribute("inputPortName")) != "undefined" &&
            trElements[i].getAttribute("inputPortName") != null){
            var inputPortName = trElements[i].getAttribute("<%= Constants.INPUT_PORT_NAME_ATTRIBUTE %>");
            //alert("Found input port: " + inputPortName);
            inputPortNames.push(inputPortName);
        }
    }

    if (inputPortNames.length == 0){
        return true;
    }

    //alert(inputPortNames.length);

    // For each input port - check if either an input textarea field
    // or a file input field have a value. If not - warn the user.
    var alertText = "";
    var isFormValid = true;
    for (var i = 0; i < inputPortNames.length; i++){

        var inputPortName = inputPortNames[i];
        var textareaElementName = namespace + inputPortName + "<%= Constants.WORKFLOW_INPUT_CONTENT_SUFFIX %>";
        var fileElementName = namespace + inputPortName + "<%= Constants.WORKFLOW_INPUT_FILE_SUFFIX %>";

        //alert("Validating input port " + inputPortName);

        var textareaElements = workflowInputsForm.getElementsByTagName("textarea");
        var fileElements = workflowInputsForm.getElementsByTagName("input");

        var found = false;
        for(var j = 0; j < textareaElements.length; j++){
           // alert("Found textarea element; name: " + textareaElements[j].name + " value: " +textareaElements[j].value);
            if (textareaElements[j].name == textareaElementName &&
                textareaElements[j].value != ""){
                found = true;
               // alert("Found: true");
                break;
            }
        }

        if (!found){
           for(var k = 0; k < fileElements.length; k++){
               // alert("Found input element; name:" + fileElements[k].name + " value: " +fileElements[k].value);
                if (fileElements[k].type == "file" &&
                    fileElements[k].name == fileElementName &&
                    fileElements[k].value != ""){
                    found = true;
                //    alert("Found: true");
                    break;
                }
            }
        }

        if (!found){
            alertText += "You have not provided a value for input '" + inputPortName + "'.\n";
            isFormValid = false;
        }

       // alert("Finished validation of input port " + inputPortName);
    }

    if (! isFormValid){
        alertText += "\nAre you sure you want to continue to run the workflow with some of the input fields empty?";
        if (confirm(alertText)){
            return true;
        }
        else{
           return false;
        }
    }
    else{
        return true;
    }

}

function validateFileUploadField(){
    // Find the file upload field in the form
    var fileNameField = document.getElementsByName("<portlet:namespace/><%= Constants.WORKFLOW_UPLOAD_FORM_FILE%>")[0];
    if (fileNameField == null || fileNameField.value == ""){
        alert("You have to select a file to upload.");
        return false;
    }
    else{
        return true;
    }
}

</script>