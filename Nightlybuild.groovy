#!groovy

node('master')
{	
	def BuildSummary = '<p><h1>Nightly Build Summary</h1></p><br/><table>';
	ws(workspace)
	{	
		stage('Get Latest Source')
		{
		}

		stage('Increment Build number')
		{
		}

		// Build applications
		def BuildResults;

		stage('Build Apps')
		{
			BuildResults = build job: 'BuildApp1', propagate: false;    	BuildSummary = notify_email(BuildResults,BuildSummary); 
			BuildResults = build job: 'BuildApp2', propagate: false;  	BuildSummary = notify_email(BuildResults,BuildSummary); 
			BuildResults = build job: 'BuildApp3', propagate: false;   	BuildSummary = notify_email(BuildResults,BuildSummary); 
		}

		stage('Build Installers')
		{
			BuildResults = build job: 'Build Installers', propagate: false;	BuildSummary = notify_email(BuildResults,BuildSummary);
		}

		sendBuildSummary(BuildSummary);
	}
}

def String notify_email(BR, BS)
{
	def to = emailextrecipients([[$class: 'CulpritsRecipientProvider']]);
	String currentResult = BR.result;
	String previousResult = "FAILURE";
	if (BR.getPreviousBuild() != null)
    	previousResult = BR.getPreviousBuild().result;
	String buildName = BR.getFullProjectName();
	String subject = "$buildName : $currentResult";
	String buildURL = BR.getAbsoluteUrl();
	String buildTime = BR.getDurationString();
    	String body = 	"""
                	<p>Hi,</p>
              
			  	Build Result: $buildName = $currentResult<br />
				Time Taken: $buildTime <br/>
                	<p>Details at <a href="$buildURL">$buildURL</a><br />
		  	"""
	BS = BS + """<tr><td>$buildName</td><td>$buildTime</td><td><a href="$buildURL""" + """console">$currentResult</a></td></tr>"""

	String log = BR.rawBuild.getLog(30).join('\n')
	if (currentResult != 'SUCCESS') {
		body = body + """
				<h2>Last lines of output</h2>
				<pre>$log</pre>
			      """
	}

	//End salutation
	body = body + 	"""
			<p>Yours Truly,</p>
			<p>Jenkins</p>
		      	"""
	if (to != null && !to.isEmpty()) 
	{
		// Email on any failures, and on first success.
		if (currentResult != 'SUCCESS') 
		{
			mail to: to, subject: subject, body: body, mimeType: "text/html"
			echo 'Sent email notification - FAILURE to culprits'
		}

    }  
	return BS; 
}

def sendBuildSummary(BS)
{
    def to = emailextrecipients([[$class: 'DevelopersRecipientProvider']]);
	String subject = "Nightly Build Summary";
	String body = BS;
	//End salutation
	String buildTime = "Unknown";
	if (currentBuild != null)
		buildTime = currentBuild.getDurationString();

	body = body + 	"""</table>
			<p>Total Build Time = $buildTime</p>
	
			<p>Yours Truly,</p>
			
			<p>Jenkins</p>
			
			"""
	if (to != null && !to.isEmpty()) 
	{
			mail to: to, subject: subject, body: body, mimeType: "text/html"
			echo 'Sent Nightly Build Summary';
	}
}
