<script language="javascript">
function alphaNumeric(string) {
		var numbers="1234567890";
		var upper="ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		var lower=upper.toLowerCase();
		
		for (i=0;i<string.length;i++) {
			var c=string.charAt(i);
			if (!(numbers.indexOf(c)>-1 || upper.indexOf(c)>-1 || lower.indexOf(c)>-1)) return false;
		}
		return true;
	}
	
	function validateName(name) {
		if (name==null || name.length==0) {
			alert("You must provide a name");
			return false;
		}
		
		if (!alphaNumeric(name)) {
			alert("The username must contain only alpha numeric characters and no spaces");
			return false;
		}
		
		if (name.length<5) {
			alert("The username must be at least 5 characters long");
			return false;
		}
		
		if (name.length>64) {
			alert("The username must be no longer than 64 characters long");
			return false;
		}
		
		return true;
	}
	
	function validatePasswords(password, confirm) {
		if (password.length <5 ) {
			alert("The password must be at least 5 characters long");
			return false;
		}
		if (password != confirm) {
			alert("The password and confirmation password do not match");
			return false;
		}
		return true;
	}
	
	function validateEmail(email) {
		if (email.indexOf("@")<=-1) {
			alert("You must provide a valid email address");
			return false;
		}
		return true;
	}	
</script>