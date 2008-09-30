<script language="javascript">

		function enableDisableElements() {
			var form=document.forms["edituser"]
			
			if (form.updatepassword.checked) {
				form.newpassword.disabled=false;
				form.newconfirm.disabled=false;
			}
			else {
				form.newpassword.disabled=true;
				form.newconfirm.disabled=true;
			}
		}
		
</script>