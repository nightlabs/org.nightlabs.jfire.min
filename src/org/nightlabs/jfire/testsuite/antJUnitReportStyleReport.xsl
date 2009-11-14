<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="xml" indent="yes"/>

<xsl:template match="/">
	<testsuites>
	<xsl:apply-templates select="JFireServerTestResult"/>
	</testsuites>
</xsl:template>

<xsl:template match="JFireServerTestResult">
	<xsl:apply-templates select="TestSuiteResult"/>
</xsl:template>

<xsl:template match="TestSuiteResult">
	<xsl:variable name="failures" select="count(TestCaseResult/TestResult[(@success='false')])"/>
	<xsl:variable name="testSuiteTime" select="format-number(@executionTime div 1000, '#.####')"/>
	<xsl:variable name="timestamp" select="@startTime"/>
	<testsuite errors="0" failures="{$failures}" hostname="n/a" name="{@suiteName}" tests="1" time="{$testSuiteTime}" timestamp="{$timestamp}">
	<xsl:for-each select="TestCaseResult">
		<xsl:variable name="testCaseClass" select="@testCaseClass"/>
		<xsl:for-each select="TestResult">
			<xsl:variable name="testCaseTime" select="format-number(@executionTime div 1000, '#.####')"/>
	    	<testcase classname="{$testCaseClass}" name="{@testName}" time="{$testCaseTime}">
	    		<xsl:if test="@success='false'">
	    			<xsl:variable name="message" select="TestResultDetail/@message"/>
	    			<failure type="{$message}">
	    				<xsl:value-of select="TestResultDetail"/>
	    			</failure>
	    		</xsl:if>
	    	</testcase>
	    </xsl:for-each>
	</xsl:for-each>
	</testsuite>
</xsl:template>

</xsl:stylesheet> 

