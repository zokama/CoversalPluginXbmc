package com.coversal.plugin.json;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Implementation of JSON-RPC over HTTP/POST
 */
public class JSONRPCHttpClient extends JSONRPCClient
{

	/*
	 * HttpClient to issue the HTTP/POST request
	 */
	private DefaultHttpClient httpClient;
	private String hostname;
	private int port;
	private String user;
	private String passwd;


	// HTTP 1.0
	private static final ProtocolVersion PROTOCOL_VERSION = new ProtocolVersion("HTTP", 1, 0);

	/**
	 * Construct a JsonRPCClient with the given service uri
	 * 
	 * @param uri
	 *            uri of the service
	 */
	public JSONRPCHttpClient(String hostname, int port, String username, String password)
	{
		this.hostname = hostname;
		this.port = port;
		this.user = username;
		this.passwd = password;
		
		// thread safe
		HttpParams params = new BasicHttpParams();
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), port));
		HttpConnectionParams.setConnectionTimeout(params, 2000);
		HttpConnectionParams.setSoTimeout(params, 2000);
		HttpProtocolParams.setVersion(params, PROTOCOL_VERSION);

		ClientConnectionManager cm = new ThreadSafeClientConnManager(params, registry);
		
		httpClient = new DefaultHttpClient(cm, params);
		
		// authentication
		if (user != null && passwd != null) {
			httpClient.getCredentialsProvider().setCredentials(
	                new AuthScope(hostname, port),
	                new UsernamePasswordCredentials(user, passwd));
		}
	}

	public HttpClient getHttpClient() {
		return httpClient;
	}
	
	@Override
	protected JSONObject doJSONRequest(JSONObject jsonRequest) throws JSONRPCException
	{
		// Create HTTP/POST request with a JSON entity containing the request
		HttpPost request = new HttpPost("http://"+hostname+":"+port+"/jsonrpc");
//		HttpParams params = new BasicHttpParams();
//		
//		request.setParams(params);

		StringEntity entity;
		try
		{
			entity = new JSONEntity(jsonRequest);
            
    		request.setEntity(entity);
    		//Profile.debug(" ---------->"+jsonRequest.toString());
		}
		catch (UnsupportedEncodingException e1)	{
			throw new JSONRPCException("Unsupported encoding", e1);
		}

		String responseString = null;
		
		try
		{
			// Execute the request and try to decode the JSON Response
			
			HttpResponse response = httpClient.execute(request);
			responseString = EntityUtils.toString(response.getEntity());
			responseString = responseString.trim();
			
			JSONObject jsonResponse = new JSONObject(responseString);
			// Check for remote errors
			if (jsonResponse.has("error"))
			{
				Object jsonError = jsonResponse.get("error");
				if (!jsonError.equals(null))
					throw new JSONRPCException(jsonResponse.get("error"));
				return jsonResponse; // JSON-RPC 1.0
			}
			else
			{
				return jsonResponse; // JSON-RPC 2.0
			}
		}
		// Underlying errors are wrapped into a JSONRPCException instance
		catch (ClientProtocolException e)
		{
			throw new JSONRPCException("HTTP error", e);
		}
		catch (IOException e)
		{
			throw new JSONRPCException("IO error", e);
		}
		catch (JSONException e)
		{
			throw new JSONRPCException("Invalid JSON response:\n"+responseString, e);
		}
	}
}
