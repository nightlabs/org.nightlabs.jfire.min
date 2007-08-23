/**
 * 
 */
package org.nightlabs.jfire.base.login.splash;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.widgets.Display;

/**
 * @author Daniel Mazurek - daniel <at> nightlabs <dot> de
 *
 */
public class SplashProgressMonitor implements IProgressMonitor {

	private ProgressMonitorPart progressMonitorPart;
	
	/**
	 * 
	 */
	public SplashProgressMonitor(ProgressMonitorPart progressMonitorPart) {
		this.progressMonitorPart = progressMonitorPart;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#beginTask(java.lang.String, int)
	 */
	public void beginTask(final String name, final int totalWork) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				progressMonitorPart.beginTask(name, totalWork);
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#done()
	 */
	public void done() {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				progressMonitorPart.done();
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#internalWorked(double)
	 */
	public void internalWorked(final double work) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				progressMonitorPart.internalWorked(work);
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#isCanceled()
	 */
	public boolean isCanceled() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#setCanceled(boolean)
	 */
	public void setCanceled(final boolean value) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				progressMonitorPart.setCanceled(value);
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#setTaskName(java.lang.String)
	 */
	public void setTaskName(final String name) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				progressMonitorPart.setTaskName(name);
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#subTask(java.lang.String)
	 */
	public void subTask(final String name) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				progressMonitorPart.subTask(name);
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#worked(int)
	 */
	public void worked(final int work) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				progressMonitorPart.worked(work);
			}
		});
	}

	public ProgressMonitorPart getProgressMonitorPart() {
		return progressMonitorPart;
	}
}
