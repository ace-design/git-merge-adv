package com.openshift.internal.client;
import com.openshift.client.HttpMethod;
import com.openshift.client.OpenShiftException;
import com.openshift.internal.client.httpclient.IMediaType;
import com.openshift.internal.client.response.Link;
import com.openshift.internal.client.response.RestResponse;

/**
 * @author Andre Dietisheim
 */
public interface IRestService {
  public static final String SERVICE_VERSION = "1.2";

  public RestResponse request(Link link, RequestParameter... serviceParameters) throws OpenShiftException;

  public RestResponse request(Link link, int timeout, RequestParameter... serviceParameters) throws OpenShiftException;

  public RestResponse request(Link link, int timeout, IMediaType mediaType, ServiceParameter... serviceParameters) throws OpenShiftException;

  public abstract RestResponse request(Link link, int timeout, IMediaType mediaType, Map<String, Object> parameters) throws OpenShiftException;

  public String request(String url, HttpMethod httpMethod, RequestParameter... serviceParameters) throws OpenShiftException;

  public String request(String url, HttpMethod httpMethod, int timeout, RequestParameter... serviceParameters) throws OpenShiftException;

  public abstract String request(String url, HttpMethod httpMethod, int timeout, IMediaType mediaType, Map<String, Object> parameters) throws OpenShiftException;

  public String getServiceUrl();

  public String getPlatformUrl();
}