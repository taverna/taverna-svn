<script language="javascript">
		
		function enableAll() {
			var form=document.forms["configform"]
			form.smtpserver.disabled=false;
			form.fromemailaddress.disabled=false;
			form.smtpauthrequired.disabled=false;
			form.smtpusername.disabled=false;
			form.smtppassword.disabled=false;
			form.smtppasswordconfirm.disabled=false;
		}
		
		function validateDetails(form) {
			
		 enableAll();
			
		 if (form.allowemail.checked) {
		 	if (form.smtpserver.value==null || form.smtpserver.value.length<=0) {
		 		alert("You must provide an smtp server name");
		 		return false;
		 	}
		 	
		 	if (form.fromemailaddress.value==null || form.fromemailaddress.value.length==0) {
		 		alert("You must provide a senders email address");
		 		return false;
		 	}
		 	
		 	if (form.fromemailaddress.value.indexOf("@")<=-1) {
		 		alert("You must provide a valid email address");
		 		return false;
		 	}
		 }
		 
		 if (form.smtpauthrequired.checked) {
		 	if (form.smtpusername.value==null || form.smtpusername.value.length<=0) {
		 		alert("You must provide an smtp username");
		 		return false;
		 	}
		 	if (form.smtppassword.value != form.smtppasswordconfirm.value) {
		 		alert("The password and confirmation do not match");
		 		return false;
		 	}
		 }
		 return true;
		}
		
		function enableDisableElements() {
			var form=document.forms["configform"]
			if (form.allowemail.checked) {
				form.smtpserver.disabled=false;
				form.smtpauthrequired.disabled=false;
				form.fromemailaddress.disabled=false;
			}
			else {
				form.smtpserver.disabled=true;
				form.fromemailaddress.disabled=true;
				form.smtpauthrequired.disabled=true;
				form.smtpauthrequired.checked=false;
			}
			
			if (form.smtpauthrequired.checked) {
				form.smtpusername.disabled=false;
				form.smtppassword.disabled=false;
				form.smtppasswordconfirm.disabled=false;
			}
			else {
				form.smtpusername.disabled=true;
				form.smtppassword.disabled=true;
				form.smtppasswordconfirm.disabled=true;
			}
		}
</script>