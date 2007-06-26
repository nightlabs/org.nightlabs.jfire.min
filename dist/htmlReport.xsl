<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html" indent="yes"/>

<xsl:template match="JFireServerTestResult">
	<html>
		<head>
			<meta http-equiv="content-type" content="text/html; charset=UTF-8"></meta>
		</head>
		<body style="font-family:arial,sans-serif">
		<xsl:value-of select="@startTime"/>
	<xsl:apply-templates select="TestSuiteResult"/>
		</body>
	</html>
</xsl:template>

<xsl:template match="TestSuiteResult">
	
	<table cellpadding="4" cellspacing="5" style="border:1px solid #AAAAAA;font-family:arial,sans-serif">
	<xsl:variable name="suiteState" select="@suiteStatus"></xsl:variable> 
	
	<xsl:choose>
		<xsl:when test="$suiteState='SKIP'">
			<tr>
				<td colspan="4"  style="background-color:#faae4b;font-size:18px;">
					<b><xsl:value-of select="@suiteName"/>: </b>
					<small><xsl:value-of select="@suiteClass"/>
					<small><i> (<xsl:value-of select="@executionTime"/>ms)</i></small>
					<b><span style="float:right">skipped</span></b>
					</small>
				</td>
			</tr>
		</xsl:when>
		<xsl:otherwise> 
			<tr>
				<td colspan="4"  style="background-color:#72a1c5;font-size:18px;">
					<b><xsl:value-of select="@suiteName"/>: </b>
					<small><xsl:value-of select="@suiteClass"/>
					<small><i> (<xsl:value-of select="@executionTime"/>ms)</i></small>
					</small>
				</td>
			</tr>
		</xsl:otherwise>	
	</xsl:choose>
	<xsl:apply-templates select="TestCaseResult"/>
	</table>
</xsl:template>

<xsl:template match="TestCaseResult">
	<tr>
	<td colspan="3" style="border:1px solid #AAAAAA">
		<xsl:value-of select="@testCaseClass"/>
		<small><i> (<xsl:value-of select="@executionTime"/>ms)</i></small>
	</td>
	</tr>
	<xsl:apply-templates select="TestResult"/>
</xsl:template>
<xsl:template match="TestResult">
	<tr><td style="width:30px;"></td><td style="border:1px solid #AAAAAA;background-color:#DDDDDD">
	<xsl:value-of select="@testName"/> 
	<small><i> (<xsl:value-of select="@executionTime"/>ms)</i></small>
	</td>
	<xsl:variable name="isSuccess" select="@success"></xsl:variable> 
		<xsl:choose>
	  	<xsl:when test="$isSuccess='true'">
			<td style="border:1px solid #AAAAAA;background-color:green;color:fff">SUCCESS <br/>
			<xsl:apply-templates select="TestResultDetail"/>
			</td>
		</xsl:when>
		<xsl:otherwise>
			<td style="border:1px solid #AAAAAA;color:fff;background-color:red">FAILED <br/>
	 		<xsl:apply-templates select="TestResultDetail"/>
	 	</td>
    	</xsl:otherwise>
		</xsl:choose>
	</tr>	
</xsl:template>

<xsl:template match="TestResultDetail">
	<xsl:value-of select="@message"/> 
</xsl:template>


</xsl:stylesheet> 

