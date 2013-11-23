package org.nightlabs.jfire.web.admin.serverinit;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class PresetsBean
{
	private String presets;

	public PresetsBean()
	{
		presets = "derby";
	}
	
	/**
	 * Get the presets.
	 * @return the presets
	 */
	public String getPresets()
	{
		return presets;
	}

	/**
	 * Set the presets.
	 * @param presets the presets to set
	 */
	public void setPresets(String presets)
	{
		this.presets = presets;
	}
}
