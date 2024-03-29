package com.openshift.internal.client;
import com.openshift.client.HttpMethod;
import com.openshift.client.OpenShiftException;
import com.openshift.internal.client.response.Link;
import com.openshift.internal.client.response.RestResponse;
import java.util.Map;
import com.openshift.internal.client.httpclient.IMediaType;
/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/


/**
 * @author Andre Dietisheim
 */
public interface IRestService {

	public static final String SERVICE_VERSION = "1.2";

	public RestResponse request(Link link, RequestParameter... serviceParameters)
			throws OpenShiftException;
	
<<<<<<< left_content.java
	public RestResponse request(Link link, int timeout, RequestParameter... serviceParameters)
			throws OpenShiftException;

	public String request(String url, HttpMethod httpMethod, RequestParameter... serviceParameters)
=======
	public RestResponse request(Link link, int timeout, ServiceParameter... serviceParameters)
			throws OpenShiftException;

    public RestResponse request(Link link, int timeout, IMediaType mediaType, ServiceParameter... serviceParameters)
            throws OpenShiftException;

	public abstract RestResponse request(Link link, Map<String, Object> parameters)
			throws OpenShiftException;

	public abstract RestResponse request(Link link, int timeout, Map<String, Object> parameters)
			throws OpenShiftException;

    public abstract RestResponse request(Link link, int timeout, IMediaType mediaType, Map<String, Object> parameters)
            throws OpenShiftException;

	public abstract String request(String url, HttpMethod httpMethod, Map<String, Object> parameters)
>>>>>>> right_content.java
			throws OpenShiftException;

	public String request(String url, HttpMethod httpMethod, int timeout, RequestParameter... serviceParameters)
			throws OpenShiftException;

<<<<<<< left_content.java
	public String getServiceUrl();
=======
    public abstract String request(String url, HttpMethod httpMethod, int timeout, IMediaType mediaType, Map<String, Object> parameters)
            throws OpenShiftException;

	public abstract String getServiceUrl();
>>>>>>> right_content.java

	public String getPlatformUrl();

}
