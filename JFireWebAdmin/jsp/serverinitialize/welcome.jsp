<%@ page language="java"%>

	<div id="welcomeblock">
	    <h1>Welcome!</h1>
	    
	    <p>
	    This seems to be the first time you are connecting to your new JFire server installation.
	    You should now set up the server by clicking the button below and follow the setup process.
	    <br/><br/>
		<form method="post">
			<input type="hidden" name="step" value="welcome"/>
			<button name="navigation" value="next" class="wide" >Start configuration</button>
		</form>
	    </p>
	    
	    <br/><br/><br/>
	
	    <p>
	    In case you just want to set up a demo system and don't care about the configuration details,
	    click the button below to initialise JFire using the default values.
	    <br/><br/>
	    With the JFireChezFrancois module being installed, the demo server will host one organisation
	    where you can login using the following coordinates:
	    <ul>
	    	<li>organisationID: chezfrancois.jfire.org</li>
	    	<li>userID: francois</li>
	    	<li>password: test</li>
	    </ul>
	    <b>NOT FOR PRODUCTIVE SYSTEMS!</b>
	    <br/><br/>
		<form action="serverinitialize" onsubmit="getElementById('welcomeblock').style.display='none'; getElementById('waitblock').style.display='block';">
			<button name="navigation" value="finish" class="wide" >Give me the demo system</button>
		</form>
	    </p>
    </div>
    
