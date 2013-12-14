<!-- pwdMeter styles and scripts -->
<link type="text/css" href="/css/pwdmeter.css" media="screen" rel="stylesheet" />
<!--[if lt IE 7]>
	<link type="text/css" href="/css/ie.css" media="screen" rel="stylesheet" />
<![endif]-->
<!-- style overrides for when using the password meter -->
<style type="text/css">
	//styles specific to the password-checker can go here
	
	@-moz-document url-prefix() {
	    //Firefox-specific password-checker overrides can go here
	}
</style>
<script type="text/javascript" src="/js/pwdmeter.js" language="javascript"></script>
<script>
	window.verifyPassword = function() {
		if (! window._passwordCheckScore || window._passwordCheckScore < 40) {
			alert("That password is not strong enough!  Your password must score 'Good' or higher in order to be accepted.  Please try again.");
			return false;
		}
		
		return true;
	};
</script>