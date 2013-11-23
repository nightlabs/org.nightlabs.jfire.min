
	<mbean code="org.jboss.invocation.jrmp.server.JRMPInvoker"
	       name="jboss:service=invoker,type=jrmp,socketType=CompressionSocketFactory">
		<attribute name="RMIObjectPort">24445</attribute>
		<attribute name="ServerAddress">${jboss.bind.address}</attribute>
		<attribute name="RMIClientSocketFactory">org.nightlabs.rmissl.socket.SSLCompressionRMIClientSocketFactory</attribute>
		<attribute name="RMIServerSocketFactory">org.nightlabs.rmissl.socket.SSLCompressionRMIServerSocketFactory</attribute>
	</mbean>
