package com.openshift.internal.client;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openshift.client.HttpMethod;
import com.openshift.client.IHttpClient;
import com.openshift.client.InvalidCredentialsOpenShiftException;
import com.openshift.client.Message;
import com.openshift.client.NotFoundOpenShiftException;
import com.openshift.client.OpenShiftEndpointException;
import com.openshift.client.OpenShiftException;
import com.openshift.client.OpenShiftRequestException;
import com.openshift.client.OpenShiftTimeoutException;
import com.openshift.internal.client.httpclient.HttpClientException;
import com.openshift.internal.client.httpclient.NotFoundException;
import com.openshift.internal.client.httpclient.UnauthorizedException;
import com.openshift.internal.client.response.Link;
import com.openshift.internal.client.response.LinkParameter;
import com.openshift.internal.client.response.LinkParameterType;
import com.openshift.internal.client.response.ResourceDTOFactory;
import com.openshift.internal.client.response.RestResponse;
import com.openshift.internal.client.utils.StringUtils;
import com.openshift.internal.client.utils.UrlUtils;
import java.util.HashMap;
import java.util.Map;
import com.openshift.internal.client.httpclient.EncodingException;
import com.openshift.internal.client.httpclient.IMediaType;
/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/




/**
 * @author André Dietisheim
 */
public class RestService implements IRestService {

	private static final Logger LOGGER = LoggerFactory.getLogger(RestService.class);

	private static final String HTTP = "http";
	
	private static final String SERVICE_PATH = "/broker/rest/";
	private static final char SLASH = '/';

	private String baseUrl;
	private IHttpClient client;

	public RestService(String baseUrl, String clientId, IHttpClient client) {
		this(baseUrl, clientId, new RestServiceProperties(), client);
	}

	RestService(String baseUrl, String clientId, RestServiceProperties properties, IHttpClient client) {
		this(baseUrl, clientId, null,  properties, client);
	}

	RestService(String baseUrl, String clientId, String protocolVersion, RestServiceProperties properties, IHttpClient client) {
		this.baseUrl = UrlUtils.ensureStartsWithHttps(baseUrl);
		this.client = client;
		setupClient(properties.getUseragent(clientId), protocolVersion, client);
	}

	private void setupClient(String userAgent, String protocolVersion, IHttpClient client) {
		if (StringUtils.isEmpty(protocolVersion)) {
			protocolVersion = SERVICE_VERSION;
		}
		client.setAcceptVersion(protocolVersion);
		client.setUserAgent(userAgent);
	}

<<<<<<< left_content.java
	@Override
	public RestResponse request(Link link, RequestParameter... parameters) throws OpenShiftException {
		return request(link, IHttpClient.NO_TIMEOUT, parameters);
	}
		
	@Override
	public RestResponse request(Link link, int timeout, RequestParameter... parameters) throws OpenShiftException {
		validateParameters(parameters, link);
		HttpMethod httpMethod = link.getHttpMethod();
		String response = request(link.getHref(), httpMethod, timeout, parameters);
		return ResourceDTOFactory.get(response);
	}

	@Override
	public String request(String href, HttpMethod httpMethod, RequestParameter... parameters) throws OpenShiftException {
		return request(href, httpMethod, IHttpClient.NO_TIMEOUT, parameters);
	}
	
	@Override
	public String request(String href, HttpMethod httpMethod, int timeout, RequestParameter... parameters) throws OpenShiftException {
		URL url = getUrl(href);
		try {
			return request(url, httpMethod, timeout, parameters);
		} catch (UnsupportedEncodingException e) {
			throw new OpenShiftException(e, e.getMessage());
		} catch (UnauthorizedException e) {
			throw new InvalidCredentialsOpenShiftException(url.toString(), e);
		} catch (NotFoundException e) {
			throw new NotFoundOpenShiftException(url.toString(), e);
		} catch (HttpClientException e) {
			throw new OpenShiftEndpointException(
					url.toString(), e, e.getMessage(),
					"Could not request {0}: {1}", url.toString(), getResponseMessage(e));
		} catch (SocketTimeoutException e) {
			throw new OpenShiftTimeoutException(url.toString(), e, e.getMessage(),
					"Could not request url {0}, connection timed out", url.toString());
		}
	}
=======
	public RestResponse request(Link link) throws OpenShiftException {
		return request(link, (Map<String, Object>) null);
	}

	public RestResponse request(Link link, int timeout) throws OpenShiftException {
		return request(link, (Map<String, Object>) null);
	}

	public RestResponse request(Link link, ServiceParameter... serviceParameters) throws OpenShiftException {
		return request(link, IHttpClient.NO_TIMEOUT, serviceParameters);
	}
	
	public RestResponse request(Link link, int timeout, ServiceParameter... serviceParameters) throws OpenShiftException {
		return request(link, timeout, toMap(serviceParameters));
	}

    @Override
    public RestResponse request(Link link, int timeout, IMediaType mediaType, ServiceParameter... serviceParameters) throws OpenShiftException {
        return request(link, timeout, mediaType, toMap(serviceParameters));
    }

    private Map<String, Object> toMap(ServiceParameter... serviceParameters) {
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		for (ServiceParameter serviceParameter : serviceParameters) {
			parameterMap.put(serviceParameter.getKey(), serviceParameter.getValue());
		}
		return parameterMap;
	}

	public RestResponse request(Link link, Map<String, Object> parameters) throws OpenShiftException {
		return request(link, IHttpClient.NO_TIMEOUT, parameters);
	}
	
	public RestResponse request(Link link, int timeout, Map<String, Object> parameters) throws OpenShiftException {
		return request(link, timeout, client.getRequestMediaType(), parameters);
	}

    @Override
    public RestResponse request(Link link, int timeout, IMediaType mediaType, Map<String, Object> parameters) throws OpenShiftException {
        validateParameters(parameters, link);
        HttpMethod httpMethod = link.getHttpMethod();
        String response = request(link.getHref(), httpMethod, timeout, mediaType, parameters);
        return ResourceDTOFactory.get(response);
    }

    public String request(String href, HttpMethod httpMethod, Map<String, Object> parameters) throws OpenShiftException {
		return request(href, httpMethod, IHttpClient.NO_TIMEOUT, parameters);
	}
>>>>>>> right_content.java

	public String request(String href, HttpMethod httpMethod, int timeout, Map<String, Object> parameters) throws OpenShiftException {
		return request(href, httpMethod, timeout, client.getRequestMediaType(), parameters);
	}

    @Override
    public String request(String href, HttpMethod httpMethod, int timeout, IMediaType mediaType, Map<String, Object> parameters) throws OpenShiftException {
        URL url = getUrl(href);
        try {
            return request(url, httpMethod, timeout, mediaType, parameters);
        } catch (EncodingException e) {
            throw new OpenShiftException(e, e.getMessage());
        } catch (UnauthorizedException e) {
            throw new InvalidCredentialsOpenShiftException(url.toString(), e);
        } catch (NotFoundException e) {
            throw new NotFoundOpenShiftException(url.toString(), e);
        } catch (HttpClientException e) {
            throw new OpenShiftEndpointException(
                    url.toString(), e, e.getMessage(),
                    "Could not request {0}: {1}", url.toString(), getResponseMessage(e));
        } catch (SocketTimeoutException e) {
            throw new OpenShiftTimeoutException(url.toString(), e, e.getMessage(),
                    "Could not request url {0}, connection timed out", url.toString());
        }
    }

    private String getResponseMessage(HttpClientException clientException) {
		try {
			RestResponse restResponse = ResourceDTOFactory.get(clientException.getMessage());
			if (restResponse == null) {
				return null;
			}
			StringBuilder builder = new StringBuilder();
			for (Message message : restResponse.getMessages().getAll()) {
				builder.append(message.getText()).append('\n');					
			}
			return builder.toString();
		} catch (OpenShiftException e) {
			// unexpected json content 
			LOGGER.error(e.getMessage());
			return clientException.getMessage();
		} catch(IllegalArgumentException e) {
			// not json
			return clientException.getMessage();
		}
	}

<<<<<<< left_content.java
	private String request(URL url, HttpMethod httpMethod, int timeout, RequestParameter... parameters)
			throws HttpClientException, SocketTimeoutException, OpenShiftException, UnsupportedEncodingException {
=======
	private String request(URL url, HttpMethod httpMethod, int timeout, IMediaType mediaType, Map<String, Object> parameters)
			throws HttpClientException, SocketTimeoutException, OpenShiftException, EncodingException {
>>>>>>> right_content.java
		LOGGER.info("Requesting {} with protocol {} on {}",
				new Object[] { httpMethod.name(), client.getAcceptVersion(), url });
		
		switch (httpMethod) {
		case GET:
			return client.get(url, timeout);
		case POST:
<<<<<<< left_content.java
			return client.post(url, timeout, parameters);
		case PUT:
			return client.put(url, timeout, parameters);
		case DELETE:
			return client.delete(url, timeout, parameters);
=======
			return client.post(parameters, url, timeout, mediaType);
		case PUT:
			return client.put(parameters, url, timeout, mediaType);
		case DELETE:
			return client.delete(parameters, url, timeout, mediaType);
>>>>>>> right_content.java
		default:
			throw new OpenShiftException("Unexpected HTTP method {0}", httpMethod.toString());
		}
		
		
	}
	
	private URL getUrl(String href) throws OpenShiftException {
		try {
			if (href == null) {
				throw new OpenShiftException("Invalid empty url");
			}
			if (href.startsWith(HTTP)) {
				return new URL(href);
			}
			if (href.startsWith(SERVICE_PATH)) {
				return new URL(baseUrl + href);
			}
			if (href.charAt(0) == SLASH) {
				href = href.substring(1, href.length());
			}
			return new URL(getServiceUrl() + href);
		} catch (MalformedURLException e) {
			throw new OpenShiftException(e, e.getMessage());
		}
	}

	private void validateParameters(RequestParameter[] parameters, Link link)
			throws OpenShiftRequestException {
		if (link.getRequiredParams() != null) {
			for (LinkParameter requiredParameter : link.getRequiredParams()) {
				validateRequiredParameter(requiredParameter, parameters, link);
			}
		}
		if (link.getOptionalParams() != null) {
			for (LinkParameter optionalParameter : link.getOptionalParams()) {
				validateOptionalParameters(optionalParameter, link);
			}
		}
	}

	private void validateRequiredParameter(LinkParameter linkParameter, RequestParameter[] parameters, Link link)
			throws OpenShiftRequestException {
		RequestParameter requestParameter = getRequestParameter(linkParameter.getName(), parameters);
		if (requestParameter == null) {
			throw new OpenShiftRequestException(
					"Requesting {0}: required request parameter \"{1}\" is missing", link.getHref(),
					linkParameter.getName());
		}

		if (isEmptyString(linkParameter, requestParameter.getValue())) {
			throw new OpenShiftRequestException("Requesting {0}: required request parameter \"{1}\" is empty",
					link.getHref(), linkParameter.getName());
		}
		// TODO: check valid options (still reported in a very incosistent way)
	}

	private RequestParameter getRequestParameter(String name, RequestParameter[] parameters) {
		if (StringUtils.isEmpty(name)) {
			return null;
		}
		for (RequestParameter parameter : parameters) {
			if (name.equals(parameter.getName())) {
				return parameter;
			}
		}
		return null;
	}
	
	private void validateOptionalParameters(LinkParameter optionalParameter, Link link) {
		// TODO: implement
	}

	private boolean isEmptyString(LinkParameter parameter, Object parameterValue) {
		return parameter.getType() == LinkParameterType.STRING
				&& parameterValue instanceof String
				&& StringUtils.isEmpty((String) parameterValue);
	}

	public String getServiceUrl() {
		return baseUrl + SERVICE_PATH;
	}

	public String getPlatformUrl() {
		return baseUrl;
	}
}

