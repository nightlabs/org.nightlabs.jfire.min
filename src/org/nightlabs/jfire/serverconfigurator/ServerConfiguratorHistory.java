package org.nightlabs.jfire.serverconfigurator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.nightlabs.db.TypeConverter;
import org.nightlabs.util.IOUtil;

public class ServerConfiguratorHistory
{
	public static enum ServerConfiguratorAction
	{
		doConfigureServer,
		undoConfigureServer
	}

	public static class Item {
		protected Item() { }
		public Item(Date timestamp, String serverConfiguratorClassName, ServerConfiguratorAction serverConfiguratorAction) {
			this.timestamp = timestamp;
			this.serverConfiguratorClassName = serverConfiguratorClassName;
			this.serverConfiguratorAction = serverConfiguratorAction;
		}

		private Date timestamp;
		private String serverConfiguratorClassName;
		private ServerConfiguratorAction serverConfiguratorAction;

		public Date getTimestamp() {
			return timestamp;
		}
		protected void setTimestamp(Date timestamp) {
			this.timestamp = timestamp;
		}
		public String getServerConfiguratorClassName() {
			return serverConfiguratorClassName;
		}
		protected void setServerConfiguratorClassName(String serverConfiguratorClassName) {
			this.serverConfiguratorClassName = serverConfiguratorClassName;
		}
		public ServerConfiguratorAction getServerConfiguratorAction() {
			return serverConfiguratorAction;
		}
		protected void setServerConfiguratorAction(ServerConfiguratorAction serverConfiguratorAction) {
			this.serverConfiguratorAction = serverConfiguratorAction;
		}
	}

	private List<Item> items = new ArrayList<Item>();

	private File historyFile;

	public ServerConfiguratorHistory(java.io.File historyFile) throws IOException
	{
		if (historyFile == null)
			throw new IllegalArgumentException("historyFile == null");

		this.historyFile = historyFile;
		read();
	}

	public void read()
	throws IOException
	{
		items.clear();
		if (historyFile.exists()) {
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(historyFile), IOUtil.CHARSET_UTF_8));
			try {
				Pattern fieldSplitPattern = Pattern.compile("\t");
				String line;
				long lineNumber = 0;
				while (null != (line = r.readLine())) {
					++lineNumber;
					String trimmedLine = line.trim();
					if (trimmedLine.startsWith("#"))
						continue;

					if (trimmedLine.equals(""))
						continue;

					String[] fields = fieldSplitPattern.split(line);
					if (fields.length < 3)
						throw new IOException("Line " + lineNumber + " misses at least one field! File: " + historyFile.getAbsolutePath());

					Item item = new Item();
					try {
						item.setTimestamp(TypeConverter.DATEFORMAT.parse(fields[0]));
					} catch (ParseException e) {
						throw new IOException("Line " + lineNumber + " contains invalid data in timestamp field! File: " + historyFile.getAbsolutePath(), e);
					}
					try {
						item.setServerConfiguratorClassName(fields[1]);
						item.setServerConfiguratorAction(ServerConfiguratorAction.valueOf(fields[2]));
					} catch (Exception e) {
						throw new IOException("Line " + lineNumber + " contains invalid data! File: " + historyFile.getAbsolutePath(), e);
					}
					items.add(item);
				}
			} finally {
				r.close();
			}
		}
	}

	public List<Item> getItems() {
		return Collections.unmodifiableList(items);
	}

	public void write()
	throws IOException
	{
		// we only write the last 1000 records
		File newHistoryFile = new File(historyFile.getParentFile(), ServerConfigurator.class.getName() + '-' + Long.toString(System.currentTimeMillis(), 36) + ".tmp");

		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newHistoryFile), IOUtil.CHARSET_UTF_8));
		try {
			for (Item item : items) {
				w.write(
						TypeConverter.DATEFORMAT.format(item.getTimestamp()) + '\t' +
						item.getServerConfiguratorClassName() + '\t' +
						item.getServerConfiguratorAction().name() + '\n'
				);
			}
		} finally {
			w.close();
		}

		if (historyFile.exists()) {
			if (!historyFile.delete())
				throw new IOException("Could not delete old history file: " + historyFile.getAbsolutePath());
		}
		newHistoryFile.renameTo(historyFile);
	}

	/**
	 * Get the last item or <code>null</code> if the history is empty.
	 *
	 * @return the last item or <code>null</code>.
	 */
	public Item getLastItem()
	{
		if (items.isEmpty())
			return null;

		return items.get(items.size() - 1);
	}

	public void addItem(Item item)
	{
		items.add(item);
		while (items.size() > 1000)
			items.remove(0);
	}
}
