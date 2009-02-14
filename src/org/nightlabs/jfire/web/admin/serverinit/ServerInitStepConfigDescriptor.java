package org.nightlabs.jfire.web.admin.serverinit;

import java.util.List;

import org.nightlabs.jfire.config.ConfigModule;

/**
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public class ServerInitStepConfigDescriptor
{
	private String id;
	private ConfigModule configModule;
	private List<ServerInitStepConfigFieldDescriptor> fields;
}
