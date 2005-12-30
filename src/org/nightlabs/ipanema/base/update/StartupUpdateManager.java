package org.nightlabs.ipanema.base.update;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.update.configuration.IConfiguredSite;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.ISite;
import org.eclipse.update.core.ISiteFeatureReference;
import org.eclipse.update.core.SiteManager;
import org.eclipse.update.standalone.DisableCommand;
import org.eclipse.update.standalone.InstallCommand;
import org.eclipse.update.standalone.UninstallCommand;

import org.nightlabs.ipanema.base.JFireBasePlugin;
import org.nightlabs.ipanema.base.login.LoginConfigModule;
import org.nightlabs.rcp.splash.SplashDialog;
import org.nightlabs.rcp.splash.SplashScreen;
import org.nightlabs.rcp.splash.SplashDialog.SplashDialogResult;

/**
 * @author Niklas Schiffler <nick@nightlabs.de>
 */

public class StartupUpdateManager implements IProgressMonitor, Runnable
{
  private double worked;
  private String taskName;
  
  public final static int MODE_INSTALL  = 0;
  public final static int MODE_UPDATE   = 0;
  
  private LoginConfigModule lcm;
  private boolean doRestart;
  
  public StartupUpdateManager(LoginConfigModule lcm)
  {
    this.lcm = lcm;
  }
  
  
  public void run()
  {
    SplashScreen.setSplashMessage(JFireBasePlugin.getResourceString("update.message.checkingForUpdates"));
    try
    {
      IConfiguredSite[] localSites =
        SiteManager.getLocalSite().getCurrentConfiguration().getConfiguredSites();
      ISite localSite = localSites[(localSites.length - 1)].getSite();
      ISiteFeatureReference[] localFeatures = localSite.getRawFeatureReferences();
      
      ISite site = SiteManager.getSite(new URL("http://hermes.nightlabs.de/~nick/ipanema2/update-devel"), false, null);
      
      
      ISiteFeatureReference[] remoteFeatures = site.getFeatureReferences();
      for(int i = 0; i < remoteFeatures.length; i++)
      {
        boolean doUpdate = true;
        boolean doInstall = true;
        IFeature localFeature = null;
        for(int j = 0; j < localFeatures.length; j++)
        {
          localFeature = localFeatures[j].getFeature(null);
          if(remoteFeatures[i].getVersionedIdentifier().getIdentifier().equals(
              localFeature.getVersionedIdentifier().getIdentifier()))
          {
            doInstall = false;
            if(remoteFeatures[i].getVersionedIdentifier().getVersion().equals(
                localFeature.getVersionedIdentifier().getVersion()))
            {
              doUpdate = false;
            }
            break;
          }
        }
        if(doInstall)
        {
          IStatus stat = SiteManager.getLocalSite().getFeatureStatus(remoteFeatures[i].getFeature(this));
          boolean ja = installFeature(remoteFeatures[i], localSite);
          doRestart = true;
        }
        else
        if(doUpdate)
        {
          disableFeature(localFeature, localSite);
          uninstallFeature(localFeature, localSite);
          boolean ja = installFeature(remoteFeatures[i], localSite);
          doRestart = true;
        }
      }
      if(doRestart)
      {
        SplashDialog.showSplashDialog(JFireBasePlugin.getResourceString("update.message.pleaseRestart"), new SplashDialog.DialogListener(){
          public boolean finish(SplashDialogResult dialogResult) 
          {
            return true;
          }
       });

      }
    }
    catch(CoreException e)
    {
      e.printStackTrace();
    }
    catch(MalformedURLException e)
    {
      e.printStackTrace();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    finally
    {
      SplashScreen.resetSplashPanel();
    }
  }
  
  private boolean installFeature(ISiteFeatureReference remoteFeature, ISite localSite)
    throws Exception
  {
//    SplashPlugin.addSplashMessage(JFireBasePlugin.getResourceString("update.message.installingFeature") + remoteFeature.getName());

    InstallCommand cmd;
    try
    {
      cmd = new InstallCommand(
            remoteFeature.getVersionedIdentifier().getIdentifier(),
            remoteFeature.getVersionedIdentifier().getVersion().toString(),
            remoteFeature.getSite().getURL().toString(),
//          updateSiteAddress,
            localSite.getURL().getFile(),
            "false");
      return cmd.run(this);
    }
    catch(CoreException e)
    {
      throw e;
    }
  }

  private boolean disableFeature(IFeature localFeature, ISite localSite)
    throws Exception
  {
    DisableCommand cmd = new DisableCommand(
        localFeature.getVersionedIdentifier().getIdentifier(),
        localFeature.getVersionedIdentifier().getVersion().toString(),
        localSite.getURL().getFile(),
        "false");
    return cmd.run();
  }

  private boolean uninstallFeature(IFeature localFeature, ISite localSite)
    throws Exception
    {
      UninstallCommand cmd = new UninstallCommand(
        localFeature.getVersionedIdentifier().getIdentifier(),
        localFeature.getVersionedIdentifier().getVersion().toString(),
        localSite.getURL().getFile(),
        "false");
    return cmd.run();
    }

  public void beginTask(String name, int totalWork)
  {
    worked = 0;
    SplashScreen.setProgressMinMax(0, totalWork * 1000);
  }

  public void done()
  {
  }

  public void internalWorked(double work)
  {
    worked += work;
    SplashScreen.setProgressValue((int)(worked * 1000));
//    try
//    {
//      Thread.sleep(1000);
//    }
//    catch(InterruptedException e)
//    {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }
  }

  public boolean isCanceled()
  {
    return false;
  }

  public void setCanceled(boolean value)
  {
  }

  public void setTaskName(String name)
  {
    taskName = name;
  }

  public void subTask(String name)
  {
    if(name != null && !("".equals(name)))
      SplashScreen.setSplashMessage(name);
    else
    if(taskName != null && !("".equals(taskName)))
      SplashScreen.setSplashMessage(taskName);
        
//    try
//    {
//      Thread.sleep(1000);
//    }
//    catch(InterruptedException e)
//    {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }
//    System.out.println("Subtask: " + name);
  }

  public void worked(int work)
  {
//    System.out.println("Worked: " + work);
  }


  public boolean doRestart()
  {
    return doRestart;
  }
  
}
