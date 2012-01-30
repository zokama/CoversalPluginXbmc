package com.coversal.plugin.json;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

import org.json.JSONObject;

public class JSONRPCTcpClient extends JSONRPCClient {

	private Socket skt;
	
	public JSONRPCTcpClient(String hostname, int port) {
		super(hostname);
	    	try {
				skt = new Socket(hostname, port);
		    	skt.setSoTimeout(2000);
			} catch (Exception e) {
				e.printStackTrace();
			}

	}

	@Override
	protected JSONObject doJSONRequest(JSONObject request)
			throws JSONRPCException {
		
		JsonProfile.debug(" ----------"+request.toString());
		BufferedReader input;
		DataOutputStream output;
//		char []buf;
    	String response;

    
	    try {
//	    	buf = new char[skt.getReceiveBufferSize()/16];
	    	output = new DataOutputStream(skt.getOutputStream());
	    	input = new BufferedReader(new InputStreamReader(skt.getInputStream()));
	    	//{"jsonrpc": "2.0", "method": "JSONRPC.Introspect", "id": 1}
	    	output.write(request.toString().getBytes());
	    	
    	
	    	response = String.valueOf((char) input.read());
	    	
	    	if (!response.equals("{"))
	    		throw new Exception ("Unrecognized JSON format. Strind does not start with {");
	    	
	    	int brackets = 1;
	    	
	    	while (brackets > 0) {
	    		char c = (char) input.read();
	    		if (c == '{') brackets++;
	    		else if (c == '}') brackets--;
	    		response += c;
	    		//count = input.read(buf);
	    		//response = response.concat(new String(buf, 0, count));
                //HocoActivity.debug("********-JSON response: " + count);
                //response = response + buf;
	    	}
	    	//while ( count > 0);
	    	
//	    	while ( (count = input.read()) > 0) {
//		    	//response = response + new String(buf, 0, count);
//	                HocoActivity.debug(count+"-JSON response: " + response);
//	                //response = response + buf;
//		    	}
	    	
			//input.close();
			//output.close();
			//skt.close();
			
			JSONObject jsonResponse= new JSONObject(response);
			
			// Check for remote errors
			if (jsonResponse.has("error"))
			{
				Object jsonError = jsonResponse.get("error");
				if (!jsonError.equals(null))
					throw new JSONRPCException(jsonError);
			}

			return jsonResponse; // JSON-RPC 2.0
	    }
	    catch (Exception e) {
//	    	try {
//				return new JSONObject(response);
//			} catch (JSONException e1) {
//				e1.printStackTrace();
//			}
//	    	 HocoActivity.debug("**("+ response.length()+" characters) RESPONSE STATUS: " + response);
	    	throw new JSONRPCException(e);
	    }
	    
	}
	
	public boolean isConnected() {
		if (skt == null) return false;
		else return skt.isConnected();
	}

}

