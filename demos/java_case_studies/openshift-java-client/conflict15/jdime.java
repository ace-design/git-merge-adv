package com.openshift.internal.client;
import static com.openshift.client.utils.RequestParametersMatcher.eq;
import static com.openshift.client.utils.UrlEndsWithMatcher.urlEndsWith;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import org.mockito.Matchers;
import com.openshift.internal.client.httpclient.EncodingException;
import com.openshift.internal.client.httpclient.IMediaType;
import org.mockito.Mockito;
import com.openshift.client.IHttpClient;
import com.openshift.client.utils.Samples;
import com.openshift.internal.client.httpclient.HttpClientException;

/**
 * @author Andre Dietisheim
 */
public class HttpClientMockDirector {
  private IHttpClient client;

  public HttpClientMockDirector() throws SocketTimeoutException, HttpClientException {
    this.client = Mockito.mock(IHttpClient.class);
    mockGetAPI(Samples.GET_API).mockGetCartridges(Samples.GET_CARTRIDGES).mockGetUser(Samples.GET_USER);
  }

  public HttpClientMockDirector mockUserAgent(String userAgent) throws SocketTimeoutException, HttpClientException {
    when(client.getUserAgent()).thenReturn(userAgent);
    return this;
  }

  public HttpClientMockDirector mockMediaType(IMediaType mediaType) throws SocketTimeoutException, HttpClientException {
    when(client.getRequestMediaType()).thenReturn(mediaType);
    return this;
  }

  public HttpClientMockDirector mockGetAny(String response) throws SocketTimeoutException, HttpClientException {
    when(client.get(any(URL.class), anyInt())).thenReturn(response);
    return this;
  }

  public HttpClientMockDirector mockGetAny(Exception exception) throws SocketTimeoutException, HttpClientException {
    when(client.get(any(URL.class), anyInt())).thenThrow(exception);
    return this;
  }

  public HttpClientMockDirector mockPostAny(Samples postRequestResponse) throws SocketTimeoutException, HttpClientException, EncodingException {
    return mockPostAny(postRequestResponse.getContentAsString());
  }

  public HttpClientMockDirector mockPostAny(String jsonResponse) throws SocketTimeoutException, HttpClientException, EncodingException {
    when(client.post(any(URL.class), anyInt(), 
<<<<<<< left.java
    Matchers.<RequestParameter[]>anyVararg()
=======
    any(IMediaType.class)
>>>>>>> right.java
    )).thenReturn(jsonResponse);
    return this;
  }

  public HttpClientMockDirector mockPostAny(Exception exception) throws SocketTimeoutException, HttpClientException, EncodingException {
    when(client.post(any(URL.class), anyInt(), 
<<<<<<< left.java
    Matchers.<RequestParameter[]>anyVararg()
=======
    any(IMediaType.class)
>>>>>>> right.java
    )).thenThrow(exception);
    return this;
  }

  public HttpClientMockDirector mockPutAny(String jsonResponse) throws SocketTimeoutException, HttpClientException, EncodingException {
    when(client.put(any(URL.class), Matchers.<RequestParameter[]>anyVararg())).thenReturn(jsonResponse);
    return this;
  }

  public HttpClientMockDirector mockDeleteAny(String jsonResponse) throws SocketTimeoutException, HttpClientException, EncodingException {
    when(client.delete(any(URL.class), anyInt(), 
<<<<<<< left.java
    Matchers.<RequestParameter[]>anyVararg()
=======
    any(IMediaType.class)
>>>>>>> right.java
    )).thenReturn(jsonResponse);
    return this;
  }

  public HttpClientMockDirector mockGetAPI(Samples getApiResourceResponse) throws HttpClientException, SocketTimeoutException {
    when(client.get(urlEndsWith("/api"), anyInt())).thenReturn(getApiResourceResponse.getContentAsString());
    return this;
  }

  public HttpClientMockDirector mockGetAPI(Exception exception) throws SocketTimeoutException {
    when(client.get(urlEndsWith("/api"), anyInt())).thenThrow(exception);
    return this;
  }

  public HttpClientMockDirector mockGetCartridges(Samples cartridgesResourceResponse) throws HttpClientException, SocketTimeoutException {
    when(client.get(urlEndsWith("/cartridges"), anyInt())).thenReturn(cartridgesResourceResponse.getContentAsString());
    return this;
  }

  public HttpClientMockDirector mockGetUser(Samples userResourceResponse) throws HttpClientException, SocketTimeoutException {
    when(client.get(urlEndsWith("/user"), anyInt())).thenReturn(userResourceResponse.getContentAsString());
    return this;
  }

  public HttpClientMockDirector mockGetKeys(Samples keysRequestResponse) throws SocketTimeoutException, HttpClientException {
    when(client.get(urlEndsWith("/user/keys"), anyInt())).thenReturn(keysRequestResponse.getContentAsString());
    return this;
  }

  public HttpClientMockDirector mockCreateKey(Samples createKeyRequestResponse) throws SocketTimeoutException, HttpClientException, EncodingException {
    when(client.post(urlEndsWith("/user/keys"), anyInt(), 
<<<<<<< left.java
    Matchers.<RequestParameter[]>anyVararg()
=======
    any(IMediaType.class)
>>>>>>> right.java
    )).thenReturn(createKeyRequestResponse.getContentAsString());
    return this;
  }

  public HttpClientMockDirector mockUpdateKey(String keyName, Samples updateKeyRequestResponse, RequestParameter... parameters) throws SocketTimeoutException, HttpClientException, EncodingException {
    when(client.put(urlEndsWith("/user/keys/" + keyName), anyInt(), 
<<<<<<< left.java
    Matchers.<RequestParameter[]>anyVararg()
=======
    any(IMediaType.class)
>>>>>>> right.java
    )).thenReturn(updateKeyRequestResponse.getContentAsString());
    return this;
  }

  public HttpClientMockDirector mockGetDomains(Samples domainsResourceResponse) throws SocketTimeoutException, HttpClientException {
    when(client.get(urlEndsWith("/domains"), anyInt())).thenReturn(domainsResourceResponse.getContentAsString());
    return this;
  }

  public HttpClientMockDirector mockCreateDomain(Samples domainResourceResponse) throws SocketTimeoutException, HttpClientException, EncodingException {
    when(client.post(urlEndsWith("/domains"), anyInt(), 
<<<<<<< left.java
    Matchers.<RequestParameter[]>anyVararg()
=======
    any(IMediaType.class)
>>>>>>> right.java
    )).thenReturn(domainResourceResponse.getContentAsString());
    return this;
  }

  public HttpClientMockDirector mockDeleteDomain(String domainId, Samples deleteDomainResourceResponse) throws SocketTimeoutException, HttpClientException, EncodingException {
    when(client.delete(urlEndsWith("/domains/" + domainId), anyInt(), 
<<<<<<< left.java
    Matchers.<RequestParameter[]>anyVararg()
=======
    any(IMediaType.class)
>>>>>>> right.java
    )).thenReturn(deleteDomainResourceResponse.getContentAsString());
    return this;
  }

  public HttpClientMockDirector mockDeleteDomain(String domainId, Exception exception) throws SocketTimeoutException, HttpClientException, EncodingException {
    when(client.delete(urlEndsWith("/domains/" + domainId), anyInt(), 
<<<<<<< left.java
    Matchers.<RequestParameter[]>anyVararg()
=======
    any(IMediaType.class)
>>>>>>> right.java
    )).thenThrow(exception);
    return this;
  }

  public HttpClientMockDirector mockRenameDomain(String domainId, Samples getDomainsResourceResponse) throws SocketTimeoutException, HttpClientException, EncodingException {
    when(client.put(urlEndsWith("/domains/" + domainId), anyInt(), 
<<<<<<< left.java
    Matchers.<RequestParameter[]>anyVararg()
=======
    any(IMediaType.class)
>>>>>>> right.java
    )).thenReturn(getDomainsResourceResponse.getContentAsString());
    return this;
  }

  public HttpClientMockDirector mockGetDomain(String domainId, Samples domainResourceResponse) throws SocketTimeoutException, HttpClientException {
    when(client.get(urlEndsWith("/domains/" + domainId), anyInt())).thenReturn(domainResourceResponse.getContentAsString());
    return this;
  }

  public HttpClientMockDirector mockGetApplications(String domainId, Samples applicationsResourceResponse) throws SocketTimeoutException, HttpClientException {
    when(client.get(urlEndsWith("/domains/" + domainId + "/applications"), anyInt())).thenReturn(applicationsResourceResponse.getContentAsString());
    return this;
  }

  public HttpClientMockDirector mockGetApplications(String domainId, Exception exception) throws SocketTimeoutException, HttpClientException {
    when(client.get(urlEndsWith("/domains/" + domainId + "/applications"), anyInt())).thenThrow(exception);
    return this;
  }

  public HttpClientMockDirector mockCreateApplication(String domainId, Samples postDomainsResourceResponse) throws SocketTimeoutException, HttpClientException, EncodingException {
    when(client.post(urlEndsWith("/domains/" + domainId + "/applications"), anyInt(), 
<<<<<<< left.java
    Matchers.<RequestParameter[]>anyVararg()
=======
    any(IMediaType.class)
>>>>>>> right.java
    )).thenReturn(postDomainsResourceResponse.getContentAsString());
    return this;
  }

  public HttpClientMockDirector mockPostApplicationEvent(String domainId, String applicationName, Samples postApplicationEvent) throws SocketTimeoutException, HttpClientException, EncodingException {
    when(client.post(urlEndsWith("/domains/" + domainId + "/applications/" + applicationName + "/events"), anyInt(), 
<<<<<<< left.java
    Matchers.<RequestParameter[]>anyVararg()
=======
    any(IMediaType.class)
>>>>>>> right.java
    )).thenReturn(postApplicationEvent.getContentAsString());
    return this;
  }

  public HttpClientMockDirector mockGetApplication(String domainId, String applicationName, Samples applicationResourceResponse) throws SocketTimeoutException, HttpClientException {
    when(client.get(urlEndsWith("/domains/" + domainId + "/applications/" + applicationName))).thenReturn(applicationResourceResponse.getContentAsString());
    return this;
  }

  public HttpClientMockDirector mockGetApplicationCartridges(String domainId, String applicationName, Samples cartridgesResourceResponse) throws SocketTimeoutException, HttpClientException {
    when(client.get(urlEndsWith("/domains/" + domainId + "/applications/" + applicationName + "/cartridges"), anyInt())).thenReturn(cartridgesResourceResponse.getContentAsString());
    return this;
  }

  public HttpClientMockDirector mockGetGearGroups(String domainId, String applicationName, Samples gearGroupsResourceResponse) throws SocketTimeoutException, HttpClientException {
    when(client.get(urlEndsWith("/domains/" + domainId + "/applications/" + applicationName + "/gear_groups"), anyInt())).thenReturn(gearGroupsResourceResponse.getContentAsString());
    return this;
  }

  public HttpClientMockDirector mockPostApplicationEvent(String domainId, String applicationName, Exception exception) throws SocketTimeoutException, HttpClientException, EncodingException {
    when(client.post(urlEndsWith("/domains/" + domainId + "/applications/" + applicationName + "/events"), anyInt(), 
<<<<<<< left.java
    Matchers.<RequestParameter[]>anyVararg()
=======
    any(IMediaType.class)
>>>>>>> right.java
    )).thenThrow(exception);
    return this;
  }

  public HttpClientMockDirector mockAddEmbeddableCartridge(String domainId, String applicationName, Samples addEmbeddedCartridgeResponse) throws SocketTimeoutException, HttpClientException, EncodingException {
    when(client.post(urlEndsWith("/domains/" + domainId + "/applications/" + applicationName + "/cartridges"), anyInt(), 
<<<<<<< left.java
    Matchers.<RequestParameter[]>anyVararg()
=======
    any(IMediaType.class)
>>>>>>> right.java
    )).thenReturn(addEmbeddedCartridgeResponse.getContentAsString());
    return this;
  }

  public HttpClientMockDirector mockAddEmbeddableCartridge(String domainId, String applicationName, Exception exception) throws SocketTimeoutException, HttpClientException, EncodingException {
    when(client.post(urlEndsWith("/domains/" + domainId + "/applications/" + applicationName + "/cartridges"), anyInt(), 
<<<<<<< left.java
    Matchers.<RequestParameter[]>anyVararg()
=======
    any(IMediaType.class)
>>>>>>> right.java
    )).thenThrow(exception);
    return this;
  }

  public HttpClientMockDirector mockRemoveEmbeddableCartridge(String domainId, String applicationName, String cartridgeName, Exception exception) throws SocketTimeoutException, HttpClientException, EncodingException {
    when(client.delete(urlEndsWith("/domains/" + domainId + "/applications/" + applicationName + "/cartridges/" + cartridgeName), anyInt(), 
<<<<<<< left.java
    Matchers.<RequestParameter[]>anyVararg()
=======
    any(IMediaType.class)
>>>>>>> right.java
    )).thenThrow(exception);
    return this;
  }

  public HttpClientMockDirector verifyPostApplicationEvent(String domainId, String applicationName) throws SocketTimeoutException, HttpClientException, EncodingException {
    verify(client, times(1)).post(urlEndsWith("/domains/" + domainId + "/applications/" + applicationName + "/events"), anyInt(), 
<<<<<<< left.java
    Matchers.<RequestParameter[]>anyVararg()
=======
    any(IMediaType.class)
>>>>>>> right.java
    );
    return this;
  }

  public HttpClientMockDirector verifyListEmbeddableCartridges(int times, String domainId, String applicationName) throws SocketTimeoutException, HttpClientException {
    verify(client, times(times)).get(urlEndsWith("/domains/" + domainId + "/applications/" + applicationName + "/cartridges"), anyInt());
    return this;
  }

  public HttpClientMockDirector verifyGetApplicationCartridges(int times, String domainId, String applicationName) throws SocketTimeoutException, HttpClientException {
    verify(client, times(times)).get(urlEndsWith("/domains/" + domainId + "/applications/" + applicationName + "/cartridges"), anyInt());
    return this;
  }

  public HttpClientMockDirector verifyAddEmbeddableCartridge(String domainId, String applicationName) throws SocketTimeoutException, HttpClientException, EncodingException {
    verify(client, times(1)).post(urlEndsWith("/domains/" + domainId + "/applications/" + applicationName + "/cartridges"), anyInt(), 
<<<<<<< left.java
    Matchers.<RequestParameter[]>anyVararg()
=======
    any(IMediaType.class)
>>>>>>> right.java
    );
    return this;
  }

  public HttpClientMockDirector verifyDeleteEmbeddableCartridge(String domainId, String applicationName, String cartridgeName) throws SocketTimeoutException, HttpClientException, EncodingException {
    verify(client, times(1)).delete(urlEndsWith("/domains/" + domainId + "/applications/" + applicationName + "/cartridges/" + cartridgeName), anyInt(), 
<<<<<<< left.java
    Matchers.<RequestParameter[]>anyVararg()
=======
    any(IMediaType.class)
>>>>>>> right.java
    );
    return this;
  }

  public HttpClientMockDirector verifyGetAny(int times) throws SocketTimeoutException, HttpClientException {
    verify(client, times(times)).get(any(URL.class), anyInt());
    return this;
  }

  public HttpClientMockDirector verifyGet(String url, int times) throws SocketTimeoutException, HttpClientException, MalformedURLException {
    verify(client, times(times)).get(eq(new URL(url)), anyInt());
    return this;
  }

  public HttpClientMockDirector verifyPostAny(int times) throws SocketTimeoutException, HttpClientException, EncodingException {
    verify(client, times(times)).post(any(URL.class), anyInt(), 
<<<<<<< left.java
    Matchers.<RequestParameter[]>anyVararg()
=======
    any(IMediaType.class)
>>>>>>> right.java
    );
    return this;
  }

  public HttpClientMockDirector verifyPost(RequestParameter... parameters) throws SocketTimeoutException, HttpClientException, UnsupportedEncodingException {
    verify(client).post(any(URL.class), anyInt(), eq(parameters));
    return this;
  }

  public HttpClientMockDirector verifyPost(String url, int times) throws SocketTimeoutException, HttpClientException, EncodingException, MalformedURLException {
    verify(client, times(times)).post(new URL(url), anyInt(), 
<<<<<<< left.java
    Matchers.<RequestParameter[]>anyVararg()
=======
    any(IMediaType.class)
>>>>>>> right.java
    );
    return this;
  }

  public HttpClientMockDirector verifyPutAny(int times) throws SocketTimeoutException, HttpClientException, EncodingException {
    verify(client, times(times)).put(any(URL.class), anyInt(), 
<<<<<<< left.java
    Matchers.<RequestParameter[]>anyVararg()
=======
    any(IMediaType.class)
>>>>>>> right.java
    );
    return this;
  }

  public HttpClientMockDirector verifyPut(String url, int times) throws SocketTimeoutException, HttpClientException, EncodingException, MalformedURLException {
    verify(client, times(times)).put(new URL(url), anyInt(), 
<<<<<<< left.java
    Matchers.<RequestParameter[]>anyVararg()
=======
    any(IMediaType.class)
>>>>>>> right.java
    );
    return this;
  }

  public HttpClientMockDirector verifyDeleteAny(int times) throws SocketTimeoutException, HttpClientException, EncodingException {
    verify(client, times(times)).delete(any(URL.class), anyInt(), 
<<<<<<< left.java
    Matchers.<RequestParameter[]>anyVararg()
=======
    any(IMediaType.class)
>>>>>>> right.java
    );
    return this;
  }

  public HttpClientMockDirector verifyDelete(String url, int times) throws SocketTimeoutException, HttpClientException, EncodingException, MalformedURLException {
    verify(client, times(times)).delete(new URL(url), anyInt(), 
<<<<<<< left.java
    Matchers.<RequestParameter[]>anyVararg()
=======
    any(IMediaType.class)
>>>>>>> right.java
    );
    return this;
  }

  public HttpClientMockDirector verifyGetDomains() throws SocketTimeoutException, HttpClientException {
    verify(client, times(1)).get(urlEndsWith("/domains"), anyInt());
    return this;
  }

  public HttpClientMockDirector verifyRenameDomain(String domainId) throws SocketTimeoutException, HttpClientException, EncodingException {
    verify(client, times(1)).put(urlEndsWith(domainId), anyInt(), 
<<<<<<< left.java
    Matchers.<RequestParameter[]>anyVararg()
=======
    any(IMediaType.class)
>>>>>>> right.java
    );
    return this;
  }

  public HttpClientMockDirector verifyGetDomain(String domainId) throws SocketTimeoutException, HttpClientException {
    verify(client, times(1)).get(urlEndsWith("/domains/" + domainId), anyInt());
    return this;
  }

  public HttpClientMockDirector verifyGetApplications(String domainId, int times) throws SocketTimeoutException, HttpClientException {
    verify(client, times(times)).get(urlEndsWith("/domains/" + domainId + "/applications"), anyInt());
    return this;
  }

  public HttpClientMockDirector verifyGetAPI() throws SocketTimeoutException, HttpClientException {
    verify(client, times(1)).get(urlEndsWith("/broker/rest/api"), anyInt());
    return this;
  }

  public HttpClientMockDirector verifyGetUser() throws SocketTimeoutException, HttpClientException {
    verify(client, times(1)).get(urlEndsWith("/broker/rest/user"), anyInt());
    return this;
  }

  public HttpClientMockDirector verifyCreateKey(RequestParameter... parameters) throws SocketTimeoutException, HttpClientException, EncodingException {
    verify(client).post(urlEndsWith("/user/keys"), eq(IHttpClient.NO_TIMEOUT), 
<<<<<<< left.java
    eq(parameters)
=======
    any(IMediaType.class)
>>>>>>> right.java
    );
    return this;
  }

  public HttpClientMockDirector verifyUpdateKey(String keyName, RequestParameter... parameters) throws SocketTimeoutException, HttpClientException, EncodingException {
    verify(client).put(urlEndsWith("/user/keys/" + keyName), eq(IHttpClient.NO_TIMEOUT), 
<<<<<<< left.java
    eq(parameters)
=======
    any(IMediaType.class)
>>>>>>> right.java
    );
    return this;
  }

  public HttpClientMockDirector verifyCreateApplication(String domainId, int timeout, RequestParameter... parameters) throws SocketTimeoutException, HttpClientException, EncodingException {
    verify(client).post(urlEndsWith("/domains/" + domainId + "/applications"), eq(timeout), 
<<<<<<< left.java
    eq(parameters)
=======
    any(IMediaType.class)
>>>>>>> right.java
    );
    return this;
  }

  public HttpClientMockDirector verifyCreateApplication(String domainId, int timeout, Class<? extends IMediaType> mediaType, Pair... pairs) throws SocketTimeoutException, HttpClientException, EncodingException {
    verify(client).post(anyMapOf(String.class, Object.class), urlEndsWith("/domains/" + domainId + "/applications"), eq(timeout), any(mediaType));
    assertPostParameters(pairs);
    return this;
  }


<<<<<<< Unknown file: This is a bug in JDime.
=======
  @SuppressWarnings(value = { "unchecked", "rawtypes" }) public HttpClientMockDirector assertPostParameters(Pair... pairs) throws SocketTimeoutException, HttpClientException, EncodingException {
    ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
    verify(client).post(captor.capture(), any(URL.class), anyInt(), any(IMediaType.class));
    assertParameters(captor.getValue(), pairs);
    return this;
  }
>>>>>>> right.java



<<<<<<< Unknown file: This is a bug in JDime.
=======
  @SuppressWarnings(value = { "unchecked", "rawtypes" }) public HttpClientMockDirector assertPutParameters(Pair... pairs) throws SocketTimeoutException, HttpClientException, EncodingException {
    ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
    verify(client).put(captor.capture(), any(URL.class), anyInt(), any(IMediaType.class));
    assertParameters(captor.getValue(), pairs);
    return this;
  }
>>>>>>> right.java



<<<<<<< Unknown file: This is a bug in JDime.
=======
  private void assertParameters(@SuppressWarnings(value = { "rawtypes" }) Map postedParameters, Pair... pairs) {
    assertThat(postedParameters).hasSize(pairs.length);
    for (Pair pair : pairs) {
      Object value = postedParameters.get(pair.getKey());
      assertThat(value.toString()).isNotNull().isEqualTo(pair.getValue());
    }
  }
>>>>>>> right.java


  public IHttpClient client() {
    return client;
  }
}