package org.nightlabs.jfire.serverupdate.base.config;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Node;

public class Directory
{
	public Directory(Node node) {
		Node dirNode = node.getAttributes().getNamedItem("dir");
		String dirString = dirNode.getTextContent();
		try {
			this.url = new File(dirString).toURI().toURL();
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("update-config classpath-dir is invalid : " + dirString);
		}

		Node recursiveNode = node.getAttributes().getNamedItem("recursive");
		String recursiveString = recursiveNode.getTextContent();
		this.recursive = Boolean.parseBoolean(recursiveString);
	}

	public Directory(URL url, boolean recursive) {
		this.url = url;
		this.recursive = recursive;
	}

	private URL url;
	private boolean recursive;

	public URL getURL() {
		return url;
	}
	public boolean isRecursive() {
		return recursive;
	}
}
