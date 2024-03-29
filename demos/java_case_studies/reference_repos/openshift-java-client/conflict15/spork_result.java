/******************************************************************************* 
 * Copyright (c) 2013 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
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

<<<<<<< LEFT
import org.mockito.Matchers;
=======
import com.openshift.internal.client.httpclient.EncodingException;
import com.openshift.internal.client.httpclient.IMediaType;
import org.mockito.ArgumentCaptor;
>>>>>>> RIGHT
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
		mockGetAPI(Samples.GET_API)
				.mockGetCartridges(Samples.GET_CARTRIDGES)
				.mockGetUser(Samples.GET_USER);
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

	public HttpClientMockDirector mockPostAny(Samples postRequestResponse)
			throws SocketTimeoutException, HttpClientException, EncodingException {
		return mockPostAny(postRequestResponse.getContentAsString());
	}

	public HttpClientMockDirector mockPostAny(String jsonResponse)
<<<<<<< LEFT
			throws SocketTimeoutException, HttpClientException, UnsupportedEncodingException {
		when(client.post(any(URL.class), anyInt(), Matchers.<RequestParameter[]>anyVararg()))
=======
			throws SocketTimeoutException, HttpClientException, EncodingException {
		when(client.post(anyMapOf(String.class, Object.class), any(URL.class), anyInt(), any(IMediaType.class)))
>>>>>>> RIGHT
				.thenReturn(jsonResponse);
		return this;
	}

	public HttpClientMockDirector mockPostAny(Exception exception)
<<<<<<< LEFT
			throws SocketTimeoutException, HttpClientException, UnsupportedEncodingException {
		when(client.post(any(URL.class), anyInt(), Matchers.<RequestParameter[]>anyVararg()))
=======
			throws SocketTimeoutException, HttpClientException, EncodingException {
		when(client.post(anyMapOf(String.class, Object.class), any(URL.class), anyInt(), any(IMediaType.class)))
>>>>>>> RIGHT
				.thenThrow(exception);
		return this;
	}

	public HttpClientMockDirector mockPutAny(String jsonResponse)
<<<<<<< LEFT
			throws SocketTimeoutException, HttpClientException, UnsupportedEncodingException {
		when(client.put(any(URL.class), Matchers.<RequestParameter[]>anyVararg()))
=======
			throws SocketTimeoutException, HttpClientException, EncodingException {
		when(client.put(anyMapOf(String.class, Object.class), any(URL.class)))
>>>>>>> RIGHT
				.thenReturn(jsonResponse);
		return this;
	}

	public HttpClientMockDirector mockDeleteAny(String jsonResponse)
<<<<<<< LEFT
			throws SocketTimeoutException, HttpClientException, UnsupportedEncodingException {
		when(client.delete(any(URL.class), anyInt(), Matchers.<RequestParameter[]>anyVararg()))
=======
			throws SocketTimeoutException, HttpClientException, EncodingException {
		when(client.delete(anyMapOf(String.class, Object.class), any(URL.class), anyInt(), any(IMediaType.class)))
>>>>>>> RIGHT
				.thenReturn(jsonResponse);
		return this;
	}

	public HttpClientMockDirector mockGetAPI(Samples getApiResourceResponse) 
			throws HttpClientException, SocketTimeoutException {
		when(client.get(urlEndsWith("/api"), anyInt()))
				.thenReturn(getApiResourceResponse.getContentAsString());
		return this;
	}

	public HttpClientMockDirector mockGetAPI(Exception exception) throws SocketTimeoutException {
		when(client.get(urlEndsWith("/api"), anyInt()))
				.thenThrow(exception);
		return this;
	}

	public HttpClientMockDirector mockGetCartridges(Samples cartridgesResourceResponse) throws HttpClientException,
			SocketTimeoutException {
		when(client.get(urlEndsWith("/cartridges"), anyInt()))
				.thenReturn(cartridgesResourceResponse.getContentAsString());
		return this;
	}

	public HttpClientMockDirector mockGetUser(Samples userResourceResponse) throws HttpClientException,
			SocketTimeoutException {
		when(client.get(urlEndsWith("/user"), anyInt()))
				.thenReturn(userResourceResponse.getContentAsString());
		return this;
	}

	public HttpClientMockDirector mockGetKeys(Samples keysRequestResponse) throws SocketTimeoutException, HttpClientException {
		when(client.get(urlEndsWith("/user/keys"), anyInt()))
				.thenReturn(keysRequestResponse.getContentAsString());
		return this;
	}
	
	public HttpClientMockDirector mockCreateKey(Samples createKeyRequestResponse) 
			throws SocketTimeoutException, HttpClientException, EncodingException {
		when(client.post(
				urlEndsWith("/user/keys"),
				anyInt(),
<<<<<<< LEFT
				Matchers.<RequestParameter[]>anyVararg()))
=======
                any(IMediaType.class)))
>>>>>>> RIGHT
				.thenReturn(createKeyRequestResponse.getContentAsString());
		return this;
	}

<<<<<<< LEFT
	public HttpClientMockDirector mockUpdateKey(String keyName, Samples updateKeyRequestResponse, RequestParameter... parameters) 
			throws SocketTimeoutException, HttpClientException, UnsupportedEncodingException {
=======
	public HttpClientMockDirector mockUpdateKey(String keyName, Samples updateKeyRequestResponse, Pair... pairs) 
			throws SocketTimeoutException, HttpClientException, EncodingException {
>>>>>>> RIGHT
		when(client.put(
				urlEndsWith("/user/keys/" + keyName),
				anyInt(),
<<<<<<< LEFT
				Matchers.<RequestParameter[]>anyVararg()))
=======
                any(IMediaType.class)))
>>>>>>> RIGHT
				.thenReturn(updateKeyRequestResponse.getContentAsString());
		return this;
	}

	public HttpClientMockDirector mockGetDomains(Samples domainsResourceResponse)
			throws SocketTimeoutException, HttpClientException {
		when(client.get(urlEndsWith("/domains"), anyInt()))
				.thenReturn(domainsResourceResponse.getContentAsString());
		return this;
	}

	public HttpClientMockDirector mockCreateDomain(Samples domainResourceResponse)
<<<<<<< LEFT
			throws SocketTimeoutException, HttpClientException, UnsupportedEncodingException {
		when(client.post(urlEndsWith("/domains"), anyInt(), Matchers.<RequestParameter[]>anyVararg()))
=======
			throws SocketTimeoutException, HttpClientException, EncodingException {
		when(client.post(anyMapOf(String.class, Object.class), urlEndsWith("/domains"), anyInt(), any(IMediaType.class)))
>>>>>>> RIGHT
				.thenReturn(domainResourceResponse.getContentAsString());
		return this;
	}

	public HttpClientMockDirector mockDeleteDomain(String domainId, Samples deleteDomainResourceResponse)
<<<<<<< LEFT
			throws SocketTimeoutException, HttpClientException, UnsupportedEncodingException {
		when(client.delete(urlEndsWith("/domains/" + domainId), anyInt(), Matchers.<RequestParameter[]>anyVararg()))
=======
			throws SocketTimeoutException, HttpClientException, EncodingException {
		when(client.delete(anyMapOf(String.class, Object.class), urlEndsWith("/domains/" + domainId), anyInt(), any(IMediaType.class)))
>>>>>>> RIGHT
				.thenReturn(deleteDomainResourceResponse.getContentAsString());
		return this;
	}

	public HttpClientMockDirector mockDeleteDomain(String domainId, Exception exception)
<<<<<<< LEFT
			throws SocketTimeoutException, HttpClientException, UnsupportedEncodingException {
		when(client.delete(urlEndsWith("/domains/" + domainId), anyInt(), Matchers.<RequestParameter[]>anyVararg()))
=======
			throws SocketTimeoutException, HttpClientException, EncodingException {
		when(client.delete(anyMapOf(String.class, Object.class), urlEndsWith("/domains/" + domainId), anyInt(), any(IMediaType.class)))
>>>>>>> RIGHT
				.thenThrow(exception);
		return this;
	}

	public HttpClientMockDirector mockRenameDomain(String domainId, Samples getDomainsResourceResponse)
<<<<<<< LEFT
			throws SocketTimeoutException, HttpClientException, UnsupportedEncodingException {
		when(client.put(urlEndsWith("/domains/" + domainId), anyInt(), Matchers.<RequestParameter[]>anyVararg()))
=======
			throws SocketTimeoutException, HttpClientException, EncodingException {
		when(client.put(anyMapOf(String.class, Object.class), urlEndsWith("/domains/" + domainId), anyInt(), any(IMediaType.class)))
>>>>>>> RIGHT
				.thenReturn(getDomainsResourceResponse.getContentAsString());
		return this;
	}

	public HttpClientMockDirector mockGetDomain(String domainId, Samples domainResourceResponse)
			throws SocketTimeoutException, HttpClientException {
		when(client.get(urlEndsWith("/domains/" + domainId), anyInt()))
				.thenReturn(domainResourceResponse.getContentAsString());
		return this;

	}

	public HttpClientMockDirector mockGetApplications(String domainId, Samples applicationsResourceResponse)
			throws SocketTimeoutException, HttpClientException {
		when(client.get(urlEndsWith("/domains/" + domainId + "/applications"), anyInt()))
				.thenReturn(applicationsResourceResponse.getContentAsString());
		return this;
	}

	public HttpClientMockDirector mockGetApplications(String domainId, Exception exception)
			throws SocketTimeoutException, HttpClientException {
		when(client.get(urlEndsWith("/domains/" + domainId + "/applications"), anyInt()))
				.thenThrow(exception);
		return this;
	}

	public HttpClientMockDirector mockCreateApplication(String domainId, Samples postDomainsResourceResponse)
<<<<<<< LEFT
			throws SocketTimeoutException, HttpClientException, UnsupportedEncodingException {
		when(client.post(
				urlEndsWith("/domains/" + domainId + "/applications"), anyInt(), Matchers.<RequestParameter[]>anyVararg()))
=======
			throws SocketTimeoutException, HttpClientException, EncodingException {
		when(client.post(anyMapOf(String.class, Object.class),
				urlEndsWith("/domains/" + domainId + "/applications"), anyInt(), any(IMediaType.class)))
>>>>>>> RIGHT
				.thenReturn(postDomainsResourceResponse.getContentAsString());
		return this;
	}

	public HttpClientMockDirector mockPostApplicationEvent(String domainId, String applicationName,
			Samples postApplicationEvent)
			throws SocketTimeoutException, HttpClientException, EncodingException {
		when(client.post(
				urlEndsWith("/domains/" + domainId + "/applications/" + applicationName + "/events"),
				anyInt(),
<<<<<<< LEFT
				Matchers.<RequestParameter[]>anyVararg()))
=======
                any(IMediaType.class)))
>>>>>>> RIGHT
				.thenReturn(postApplicationEvent.getContentAsString());
		return this;
	}

	public HttpClientMockDirector mockGetApplication(String domainId, String applicationName,
			Samples applicationResourceResponse)
			throws SocketTimeoutException, HttpClientException {
		when(client.get(urlEndsWith("/domains/" + domainId + "/applications/" + applicationName)))
				.thenReturn(applicationResourceResponse.getContentAsString());
		return this;
	}

	public HttpClientMockDirector mockGetApplicationCartridges(String domainId, String applicationName,
			Samples cartridgesResourceResponse)
			throws SocketTimeoutException, HttpClientException {
		when(client.get(
				urlEndsWith("/domains/" + domainId + "/applications/" + applicationName + "/cartridges"),
				anyInt()))
				.thenReturn(cartridgesResourceResponse.getContentAsString());
		return this;
	}

	public HttpClientMockDirector mockGetGearGroups(String domainId, String applicationName,
			Samples gearGroupsResourceResponse)
			throws SocketTimeoutException, HttpClientException {
		when(client.get(
				urlEndsWith("/domains/" + domainId + "/applications/" + applicationName + "/gear_groups"),
				anyInt()))
				.thenReturn(gearGroupsResourceResponse.getContentAsString());
		return this;
	}

	public HttpClientMockDirector mockPostApplicationEvent(String domainId, String applicationName, Exception exception)
			throws SocketTimeoutException, HttpClientException, EncodingException {
		when(client.post(
				urlEndsWith("/domains/" + domainId + "/applications/" + applicationName + "/events"),
				anyInt(),
<<<<<<< LEFT
				Matchers.<RequestParameter[]>anyVararg()))
=======
                any(IMediaType.class)))
>>>>>>> RIGHT
				.thenThrow(exception);
		return this;
	}

	public HttpClientMockDirector mockAddEmbeddableCartridge(String domainId, String applicationName,
			Samples addEmbeddedCartridgeResponse)
			throws SocketTimeoutException, HttpClientException, EncodingException {
		when(client.post(
				urlEndsWith("/domains/" + domainId + "/applications/" + applicationName + "/cartridges"),
				anyInt(),
<<<<<<< LEFT
				Matchers.<RequestParameter[]>anyVararg()))
=======
                any(IMediaType.class)))
>>>>>>> RIGHT
				.thenReturn(addEmbeddedCartridgeResponse.getContentAsString());
		return this;
	}

	public HttpClientMockDirector mockAddEmbeddableCartridge(String domainId, String applicationName,
			Exception exception)
			throws SocketTimeoutException, HttpClientException, EncodingException {
		when(client.post(
				urlEndsWith("/domains/" + domainId + "/applications/" + applicationName + "/cartridges"),
<<<<<<< LEFT
				anyInt(), 
				Matchers.<RequestParameter[]>anyVararg()))
=======
				anyInt(),
                any(IMediaType.class)))
>>>>>>> RIGHT
				.thenThrow(exception);
		return this;
	}

	public HttpClientMockDirector mockRemoveEmbeddableCartridge(String domainId, String applicationName,
			String cartridgeName,
			Exception exception)
			throws SocketTimeoutException, HttpClientException, EncodingException {
		when(client.delete(
				urlEndsWith(
				"/domains/" + domainId + "/applications/" + applicationName + "/cartridges/" + cartridgeName),
				anyInt(),
<<<<<<< LEFT
				Matchers.<RequestParameter[]>anyVararg()))
=======
                any(IMediaType.class)))
>>>>>>> RIGHT
				.thenThrow(exception);
		return this;
	}

	public HttpClientMockDirector verifyPostApplicationEvent(String domainId, String applicationName)
			throws SocketTimeoutException, HttpClientException, EncodingException {
		verify(client, times(1)).post(
				urlEndsWith("/domains/" + domainId + "/applications/" + applicationName + "/events"),
				anyInt(),
<<<<<<< LEFT
				Matchers.<RequestParameter[]>anyVararg());
=======
                any(IMediaType.class));
>>>>>>> RIGHT
		return this;
	}

	public HttpClientMockDirector verifyListEmbeddableCartridges(int times, String domainId, String applicationName)
			throws SocketTimeoutException, HttpClientException {
		verify(client, times(times)).get(
				urlEndsWith("/domains/" + domainId + "/applications/" + applicationName + "/cartridges"),
				anyInt());
		return this;

	}

	public HttpClientMockDirector verifyGetApplicationCartridges(int times, String domainId, String applicationName)
			throws SocketTimeoutException, HttpClientException {
		verify(client, times(times)).get(
				urlEndsWith("/domains/" + domainId + "/applications/" + applicationName + "/cartridges"),
				anyInt());
		return this;
	}

	public HttpClientMockDirector verifyAddEmbeddableCartridge(String domainId, String applicationName)
			throws SocketTimeoutException, HttpClientException, EncodingException {
		verify(client, times(1)).post(
				urlEndsWith("/domains/" + domainId + "/applications/" + applicationName + "/cartridges"),
				anyInt(),
<<<<<<< LEFT
				Matchers.<RequestParameter[]>anyVararg());
=======
                any(IMediaType.class));
>>>>>>> RIGHT
		return this;
	}

	public HttpClientMockDirector verifyDeleteEmbeddableCartridge(String domainId, String applicationName,
			String cartridgeName)
			throws SocketTimeoutException, HttpClientException, EncodingException {
		verify(client, times(1)).delete(
				urlEndsWith("/domains/" + domainId + "/applications/" + applicationName + "/cartridges/"
						+ cartridgeName),
				anyInt(),
<<<<<<< LEFT
				Matchers.<RequestParameter[]>anyVararg());
=======
                any(IMediaType.class));
>>>>>>> RIGHT
		return this;
	}

	public HttpClientMockDirector verifyGetAny(int times) throws SocketTimeoutException, HttpClientException {
		verify(client, times(times)).get(any(URL.class), anyInt());
		return this;
	}

	public HttpClientMockDirector verifyGet(String url, int times)
			throws SocketTimeoutException, HttpClientException, MalformedURLException {
		verify(client, times(times)).get(eq(new URL(url)), anyInt());
		return this;
	}

	public HttpClientMockDirector verifyPostAny(int times)
<<<<<<< LEFT
			throws SocketTimeoutException, HttpClientException, UnsupportedEncodingException {
		verify(client, times(times)).post(any(URL.class), anyInt(),Matchers.<RequestParameter[]>anyVararg());
		return this;
	}

	public HttpClientMockDirector verifyPost(RequestParameter... parameters)
			throws SocketTimeoutException, HttpClientException, UnsupportedEncodingException {
		verify(client).post(any(URL.class), anyInt(), eq(parameters));
=======
			throws SocketTimeoutException, HttpClientException, EncodingException {
		verify(client, times(times)).post(anyMapOf(String.class, Object.class), any(URL.class), anyInt(), any(IMediaType.class));
>>>>>>> RIGHT
		return this;
	}

	public HttpClientMockDirector verifyPost(String url, int times)
<<<<<<< LEFT
			throws SocketTimeoutException, HttpClientException, UnsupportedEncodingException, MalformedURLException {
		verify(client, times(times)).post(new URL(url), anyInt(), Matchers.<RequestParameter[]>anyVararg());
=======
			throws SocketTimeoutException, HttpClientException, EncodingException, MalformedURLException {
		verify(client, times(times)).post(anyMapOf(String.class, Object.class), new URL(url), anyInt(), any(IMediaType.class));
>>>>>>> RIGHT
		return this;
	}

	public HttpClientMockDirector verifyPutAny(int times)
<<<<<<< LEFT
			throws SocketTimeoutException, HttpClientException, UnsupportedEncodingException {
		verify(client, times(times)).put(any(URL.class), anyInt(),Matchers.<RequestParameter[]>anyVararg());
=======
			throws SocketTimeoutException, HttpClientException, EncodingException {
		verify(client, times(times)).put(anyMapOf(String.class, Object.class), any(URL.class), anyInt(), any(IMediaType.class));
>>>>>>> RIGHT
		return this;
	}

	public HttpClientMockDirector verifyPut(String url, int times)
<<<<<<< LEFT
			throws SocketTimeoutException, HttpClientException, UnsupportedEncodingException, MalformedURLException {
		verify(client, times(times)).put(new URL(url), anyInt(),Matchers.<RequestParameter[]>anyVararg());
=======
			throws SocketTimeoutException, HttpClientException, EncodingException, MalformedURLException {
		verify(client, times(times)).put(anyMapOf(String.class, Object.class), new URL(url), anyInt(), any(IMediaType.class));
>>>>>>> RIGHT
		return this;
	}

	public HttpClientMockDirector verifyDeleteAny(int times)
<<<<<<< LEFT
			throws SocketTimeoutException, HttpClientException, UnsupportedEncodingException {
		verify(client, times(times)).delete(any(URL.class), anyInt(),Matchers.<RequestParameter[]>anyVararg());
=======
			throws SocketTimeoutException, HttpClientException, EncodingException {
		verify(client, times(times)).delete(anyMapOf(String.class, Object.class), any(URL.class), anyInt(), any(IMediaType.class));
>>>>>>> RIGHT
		return this;
	}

	public HttpClientMockDirector verifyDelete(String url, int times)
<<<<<<< LEFT
			throws SocketTimeoutException, HttpClientException, UnsupportedEncodingException, MalformedURLException {
		verify(client, times(times)).delete(new URL(url), anyInt(), Matchers.<RequestParameter[]>anyVararg());
=======
			throws SocketTimeoutException, HttpClientException, EncodingException, MalformedURLException {
		verify(client, times(times)).delete(anyMapOf(String.class, Object.class), new URL(url), anyInt(), any(IMediaType.class));
>>>>>>> RIGHT
		return this;
	}

	public HttpClientMockDirector verifyGetDomains() throws SocketTimeoutException, HttpClientException {
		verify(client, times(1)).get(urlEndsWith("/domains"), anyInt());
		return this;
	}

	public HttpClientMockDirector verifyRenameDomain(String domainId)
<<<<<<< LEFT
			throws SocketTimeoutException, HttpClientException, UnsupportedEncodingException {
		verify(client, times(1)).put(urlEndsWith(domainId), anyInt(),Matchers.<RequestParameter[]>anyVararg());
=======
			throws SocketTimeoutException, HttpClientException, EncodingException {
		verify(client, times(1)).put(anyMapOf(String.class, Object.class), urlEndsWith(domainId), anyInt(), any(IMediaType.class));
>>>>>>> RIGHT
		return this;
	}

	public HttpClientMockDirector verifyGetDomain(String domainId) throws SocketTimeoutException, HttpClientException {
		verify(client, times(1)).get(urlEndsWith("/domains/" + domainId), anyInt());
		return this;
	}

	public HttpClientMockDirector verifyGetApplications(String domainId, int times)
			throws SocketTimeoutException, HttpClientException {
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

<<<<<<< LEFT
	public HttpClientMockDirector verifyCreateKey(RequestParameter... parameters)
			throws SocketTimeoutException, HttpClientException, UnsupportedEncodingException {
		verify(client).post(
				urlEndsWith("/user/keys"), eq(IHttpClient.NO_TIMEOUT), eq(parameters));
=======
	public HttpClientMockDirector verifyCreateKey(Pair... pairs)
			throws SocketTimeoutException, HttpClientException, EncodingException {
		verify(client).post(anyMapOf(String.class, Object.class),
				urlEndsWith("/user/keys"), eq(IHttpClient.NO_TIMEOUT), any(IMediaType.class));
		assertPostParameters(pairs);
>>>>>>> RIGHT
		return this;
	}

<<<<<<< LEFT
	public HttpClientMockDirector verifyUpdateKey(String keyName, RequestParameter... parameters)
			throws SocketTimeoutException, HttpClientException, UnsupportedEncodingException {
		verify(client).put(
				urlEndsWith("/user/keys/" + keyName), eq(IHttpClient.NO_TIMEOUT), eq(parameters));
=======
	public HttpClientMockDirector verifyUpdateKey(String keyName, Pair... pairs)
			throws SocketTimeoutException, HttpClientException, EncodingException {
		verify(client).put(anyMapOf(String.class, Object.class),
				urlEndsWith("/user/keys/" + keyName), eq(IHttpClient.NO_TIMEOUT), any(IMediaType.class));
		assertPutParameters(pairs);
>>>>>>> RIGHT
		return this;
	}

<<<<<<< LEFT
	public HttpClientMockDirector verifyCreateApplication(String domainId, int timeout, RequestParameter... parameters)
			throws SocketTimeoutException, HttpClientException, UnsupportedEncodingException {
		verify(client).post(
				urlEndsWith("/domains/" + domainId + "/applications"), eq(timeout), eq(parameters));
=======
	public HttpClientMockDirector verifyCreateApplication(String domainId, int timeout, Pair... pairs)
			throws SocketTimeoutException, HttpClientException, EncodingException {
		verify(client).post(anyMapOf(String.class, Object.class),
				urlEndsWith("/domains/" + domainId + "/applications"), eq(timeout), any(IMediaType.class));
		assertPostParameters(pairs);
>>>>>>> RIGHT
		return this;
	}

<<<<<<< LEFT
=======
    public HttpClientMockDirector verifyCreateApplication(String domainId, int timeout, Class<? extends IMediaType> mediaType, Pair... pairs)
            throws SocketTimeoutException, HttpClientException, EncodingException {
        verify(client).post(anyMapOf(String.class, Object.class),
                urlEndsWith("/domains/" + domainId + "/applications"), eq(timeout), any(mediaType));
        assertPostParameters(pairs);
        return this;
    }

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public HttpClientMockDirector assertPostParameters(Pair... pairs)
			throws SocketTimeoutException, HttpClientException, EncodingException {
		ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
		verify(client).post(captor.capture(), any(URL.class), anyInt(), any(IMediaType.class));
		assertParameters(captor.getValue(), pairs);
		return this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public HttpClientMockDirector assertPutParameters(Pair... pairs)
			throws SocketTimeoutException, HttpClientException, EncodingException {
		ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
		verify(client).put(captor.capture(), any(URL.class), anyInt(), any(IMediaType.class));
		assertParameters(captor.getValue(), pairs);
		return this;
	}

	private void assertParameters(@SuppressWarnings("rawtypes") Map postedParameters, Pair... pairs) {
		assertThat(postedParameters).hasSize(pairs.length);
		for (Pair pair : pairs) {
			Object value = postedParameters.get(pair.getKey());
            //It's possible that the value is not a String (e.g. a Map), so we convert to String before checking.
			assertThat(value.toString()).isNotNull().isEqualTo(pair.getValue());
		}
	}

>>>>>>> RIGHT
	public IHttpClient client() {
		return client;
	}
}
