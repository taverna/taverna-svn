<%-- 
    Document   : CommonJavaScript
    Created on : Aug 20, 2010, 10:43:06 AM
    Author     : Alex Nenadic
--%>

<%-- 
    JavaScript function that validates that all input fields in a form have been provided.
    Use with the <form> tag, e.g. <form action="..." onSubmit="validateForm()">
--%>

<script type="text/javascript">
function validateForm()
{
    var validate = true;
    var inputElements;
    var alertText = "";

    alert(" java script: ");

    // workflow inputs form
    var workflowInputsForm;

    // all forms on a page
    var workflowInputsForms = document.getElementsByTagName("form");
    alert("form number" + workflowInputsForms.length);

    for (var i = 0; i < workflowInputsForms.length; i++) {
        alert(" java scrip: " + workflowInputsForms[i].name);
        if (workflowInputsForms[i].name == "workflow_inputs_form"){
            workflowInputsForm = workflowInputsForms[i];
            break;
        }
    }

    if (workflowInputsForm != null) {
        inputElements = workflowInputsForm.getElementsByTagName("input");
        for (var i = 0; i < inputElements.length; i++) {
            if (inputElements[i].value == null || inputElements[i].value == ''){
                validate = false;
                alertText = alertText + 'You have not provided a value for input ' + inputElements[i].name + '\n';
            }
        }
    }
   
    if (!validate){
       alert(alertText);
    }
    return validate;
}

function validateForm1(){
     alert("Bla bla");
     return false;
}
</script>
