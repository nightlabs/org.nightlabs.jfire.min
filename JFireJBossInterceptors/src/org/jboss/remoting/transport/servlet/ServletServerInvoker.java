package org.jboss.remoting.transport.servlet;

import org.jboss.remoting.InvocationRequest;
import org.jboss.remoting.InvocationResponse;
import org.jboss.remoting.InvokerLocator;
import org.jboss.remoting.Version;
import org.jboss.remoting.marshal.MarshalFactory;
import org.jboss.remoting.marshal.Marshaller;
import org.jboss.remoting.marshal.UnMarshaller;
import org.jboss.remoting.marshal.VersionedMarshaller;
import org.jboss.remoting.marshal.VersionedUnMarshaller;
import org.jboss.remoting.marshal.http.HTTPMarshaller;
import org.jboss.remoting.marshal.http.HTTPUnMarshaller;
import org.jboss.remoting.transport.http.HTTPMetadataConstants;
import org.jboss.remoting.transport.web.WebServerInvoker;
import org.jboss.remoting.transport.web.WebUtil;
import org.jboss.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

/**
 *
 *  this class overrides the standard Jboss's ServletServerInvoker in order to provide more flexibility 
 *  regarding the control of the the invocation propagation.
 *  
 * @author Fitas Amine - fitas [at] nightlabs [dot] de
 */
public class ServletServerInvoker extends WebServerInvoker implements ServletServerInvokerMBean
{
	private static final Logger log = Logger.getLogger(ServletServerInvoker.class);

	public ServletServerInvoker(InvokerLocator locator)
	{
		super(locator);
	}

	@SuppressWarnings("unchecked")
	public ServletServerInvoker(InvokerLocator locator, Map configuration)
	{
		super(locator, configuration);
	}

	protected String getDefaultDataType()
	{
		return HTTPMarshaller.DATATYPE;
	}

	public String getMBeanObjectName()
	{
		return "jboss.remoting:service=invoker,transport=servlet";
	}

	@SuppressWarnings("unchecked")
	public void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		Map metadata = new HashMap();

		Enumeration enumer = request.getHeaderNames();
		while(enumer.hasMoreElements())
		{
			Object obj = enumer.nextElement();
			String headerKey = (String) obj;
			String headerValue = request.getHeader(headerKey);
			metadata.put(headerKey, headerValue);
		}

		Map urlParams = request.getParameterMap();
		metadata.putAll(urlParams);

		String requestContentType = request.getContentType();


		try
		{
			Object invocationResponse = null;

			ServletInputStream inputStream = request.getInputStream();
			UnMarshaller unmarshaller = MarshalFactory.getUnMarshaller(HTTPUnMarshaller.DATATYPE, getSerializationType());
			Object obj = null;
			if (unmarshaller instanceof VersionedUnMarshaller)
				obj = ((VersionedUnMarshaller)unmarshaller).read(inputStream, metadata, Version.getDefaultVersion());
			else
				obj = unmarshaller.read(inputStream, metadata);
			inputStream.close();

			InvocationRequest invocationRequest = null;

			if(obj instanceof InvocationRequest)
			{
				invocationRequest = (InvocationRequest) obj;
			}
			else
			{
				if(WebUtil.isBinary(requestContentType))
				{
					invocationRequest = getInvocationRequest(metadata, obj);
				}
				else
				{
					invocationRequest = createNewInvocationRequest(metadata, obj);
				}
			}

			try
			{
				// call transport on the subclass, get the result to handback
				invocationResponse = invoke(invocationRequest);
			}
			catch(Throwable ex)
			{
				log.debug("Error thrown calling invoke on server invoker.", ex);
				invocationResponse = null;
				response.sendError(500, "Error processing invocation request.  " + ex.getMessage());
			}

			if(invocationResponse != null)
			{
				response.setContentType(requestContentType);
				int iContentLength = getContentLength(invocationResponse);
				response.setContentLength(iContentLength);
				ServletOutputStream outputStream = response.getOutputStream();
				Marshaller marshaller = MarshalFactory.getMarshaller(HTTPMarshaller.DATATYPE, getSerializationType());
				if (marshaller instanceof VersionedMarshaller)
					((VersionedMarshaller) marshaller).write(invocationResponse, outputStream, Version.getDefaultVersion());
				else
					marshaller.write(invocationResponse, outputStream);
				outputStream.close();
			}

		}
		catch(ClassNotFoundException e)
		{
			log.error("Error processing invocation request due to class not being found.", e);
			response.sendError(500, "Error processing invocation request due to class not being found.  " + e.getMessage());

		}

	}

	@SuppressWarnings("unchecked")
	public byte[] processRequest(HttpServletRequest request, byte[] requestByte,
			HttpServletResponse response)
	throws ServletException, IOException
	{
		byte[] retval = new byte[0];

		Map metadata = new HashMap();

		Enumeration enumer = request.getHeaderNames();
		while(enumer.hasMoreElements())
		{
			Object obj = enumer.nextElement();
			String headerKey = (String) obj;
			String headerValue = request.getHeader(headerKey);
			metadata.put(headerKey, headerValue);
		}

		Map urlParams = request.getParameterMap();
		metadata.putAll(urlParams);

		metadata.put(HTTPMetadataConstants.METHODTYPE, request.getMethod());
		metadata.put(HTTPMetadataConstants.PATH, request.getPathTranslated());

		String requestContentType = request.getContentType();


		try
		{
			Object invocationResponse = null;

			ServletInputStream inputStream = request.getInputStream();
			UnMarshaller unmarshaller = getUnMarshaller();
			Object obj = null;
			if (unmarshaller instanceof VersionedUnMarshaller)
				obj = ((VersionedUnMarshaller)unmarshaller).read(new ByteArrayInputStream(requestByte), metadata, Version.getDefaultVersion());
			else
				obj = unmarshaller.read(new ByteArrayInputStream(requestByte), metadata);
			inputStream.close();

			boolean isError = false;
			InvocationRequest invocationRequest = null;

			if(obj instanceof InvocationRequest)
			{
				invocationRequest = (InvocationRequest) obj;
			}
			else
			{
				if(WebUtil.isBinary(requestContentType))
				{
					invocationRequest = getInvocationRequest(metadata, obj);
				}
				else
				{
					invocationRequest = createNewInvocationRequest(metadata, obj);
				}
			}

			try
			{
				// call transport on the subclass, get the result to handback
				invocationResponse = invoke(invocationRequest);
			}
			catch(Throwable ex)
			{
				// returns always the exceptions
				log.debug("Error thrown calling invoke on server invoker.", ex);
				invocationResponse = ex;   
				String sessionId = invocationRequest.getSessionId();
				ServletThrowable st = new ServletThrowable(ex);
				invocationResponse = new InvocationResponse(sessionId, st, true, null);
			}

			//Start with response code of 204 (no content), then if is a return from handler, change to 200 (ok)
			int status = 204;
			if(invocationResponse != null)
			{
				if(isError)
				{
					response.sendError(500, "Error occurred processing invocation request. ");
				}
				else
				{
					status = 200;
				}
			}

			// extract response code/message if exists
			Map responseMap = invocationRequest.getReturnPayload();
			if(responseMap != null)
			{
				Integer handlerStatus = (Integer) responseMap.remove(HTTPMetadataConstants.RESPONSE_CODE);
				if(handlerStatus != null)
				{
					status = handlerStatus.intValue();
				}

				// add any response map headers
				Set entries = responseMap.entrySet();
				Iterator itr = entries.iterator();
				while(itr.hasNext())
				{
					Map.Entry entry = (Map.Entry)itr.next();
					response.addHeader(entry.getKey().toString(), entry.getValue().toString());
				}
			}



			// can't set message anymore as is depricated
			response.setStatus(status);

			if(invocationResponse != null)
			{
				String responseContentType = invocationResponse == null ? requestContentType : WebUtil.getContentType(invocationResponse);
				response.setContentType(responseContentType);
				//int iContentLength = getContentLength(invocationResponse);
				//response.setContentLength(iContentLength);
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				Marshaller marshaller = getMarshaller();
				if (marshaller instanceof VersionedMarshaller)
					((VersionedMarshaller) marshaller).write(invocationResponse, outputStream, Version.getDefaultVersion());
				else
					marshaller.write(invocationResponse, outputStream);
				retval = outputStream.toByteArray();
				response.setContentLength(retval.length);
			}

		}
		catch(ClassNotFoundException e)
		{
			log.error("Error processing invocation request due to class not being found.", e);
			response.sendError(500, "Error processing invocation request due to class not being found.  " + e.getMessage());
		}

		return retval;
	}

}