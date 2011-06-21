package org.nightlabs.jfire.serverupdate.launcher.config;

import java.io.File;

import org.w3c.dom.Node;

public class Directory
{
	public Directory(Node node) {
		Node dirNode = node.getAttributes().getNamedItem("dir");
		String dirString = dirNode.getTextContent();
		this.file = new File(dirString);

		Node recursiveNode = node.getAttributes().getNamedItem("recursive");
		String recursiveString = recursiveNode.getTextContent();
		this.recursive = Boolean.parseBoolean(recursiveString);
	}

	public Directory(File file, boolean recursive) {
		this.file = file;
		this.recursive = recursive;
	}

	private File file;
	private boolean recursive;

	public File getFile() {
		return file;
	}
	public boolean isRecursive() {
		return recursive;
	}
}
