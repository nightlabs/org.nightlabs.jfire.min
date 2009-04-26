<%@ page language="java"%>

	<div id="welcomeblock">
	    <h1>Welcome!</h1>
	    
	    <p>
	    This seems to be the first time you are connecting to your new JFire server installation.
	    You should now set up the server by clicking the button below and follow the setup process.
	    <br/><br/>
		<form method="post">
			<input type="hidden" name="step" value="welcome"/>
			<input type="hidden" name="navigation" value="next"/>
			<input type="submit" value="Start configuration" class="wide"/>
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
		<form action="serverinitialize" onsubmit="showWaitBlock('Please wait while initializing server...')">
			<input type="hidden" name="navigation" value="finish"/>
			<input type="submit" value="Give me the demo system" class="wide"/>
		</form>
	    </p>
    </div>
    
