package org.nightlabs.jfire.installer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;


/**
 * Found at http://forum.java.sun.com/thread.jspa?messageID=2233419&tstart=0
 *
 * Note, that this is *not* used for the installation itself anymore since we now
 * use a patched one-jar. It is only used by the {@link WebController}...
 */
public class ClassPathHacker
{
	public static void addFile(File f) throws IOException
	{
		addURL(f.toURI().toURL());
	}

	public static void addURL(URL u) throws IOException
	{
		URLClassLoader sysloader;
		try {
			sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		} catch(ClassCastException e) {
			throw new RuntimeException(String.format(Messages.getString("ClassPathHacker.errClassLoader"), ClassLoader //$NON-NLS-1$
					.getSystemClassLoader().getClass().getName()), e);
		}
		Class<?> sysclass = URLClassLoader.class;
		try {
			Method method = sysclass.getDeclaredMethod("addURL", new Class[] { URL.class }); //$NON-NLS-1$
			method.setAccessible(true);
			method.invoke(sysloader, new Object[] { u });
		} catch (Throwable t) {
			throw new IllegalStateException(Messages.getString("ClassPathHacker.errClassPathAdd")); //$NON-NLS-1$
		}
	}
}
