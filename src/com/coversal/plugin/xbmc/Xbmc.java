package com.coversal.plugin.xbmc;


import org.apache.http.client.methods.HttpPost;
import org.json.JSONObject;

import android.text.InputType;

import com.coversal.plugin.json.JSONRPCException;
import com.coversal.plugin.json.JSONRPCHttpClient;
import com.coversal.ucl.api.BrowsableAPI;
import com.coversal.ucl.api.ControllerAPI;
import com.coversal.ucl.api.TextParameter;
import com.coversal.ucl.plugin.ProfileAnnouncer;
import com.coversal.ucl.plugin.Profile;


public class Xbmc extends Profile {

	static final String SERVER = "Server Address";
	static final String PORT = "Port";
	private static final String USERNAME = "Username (optional)";
	private static final String PASSWORD = "Password (optional)";
	static final int TIMEOUT = 2000;
	
	XbmcController controller = new XbmcController(this);
	XbmcBrowser browser = new XbmcBrowser(this);
	JSONObject currentObject;
	JSONRPCHttpClient session;

	int apiVersion = 0;
	
	public Xbmc(ProfileAnnouncer pa) {
		super(pa);
		
		defineParameter(SERVER, new TextParameter(null, true));
		defineParameter(PORT, new TextParameter("8080", true, InputType.TYPE_CLASS_NUMBER));
		defineParameter(USERNAME, new TextParameter("xbmc", false));
		defineParameter(PASSWORD, new TextParameter("xbmc", false, InputType.TYPE_TEXT_VARIATION_PASSWORD));
		
		setOptionValue(OPTION_STARTUP, START_OPTION_REMOTE);
	}
		
	@Override
	public ControllerAPI getController() {
		return controller;
	}
	
	@Override
	public BrowsableAPI getBrowser() {
		return browser;
	}

	@Override
	public String getProfileName() {
		return "XBMC";
	}

	@Override
	public String getIconName() {
		return "xbmc";
	}

	
	public JSONRPCHttpClient getJsonClient() {
		if (session == null)
				init();
			
		return session;
	}
	
	@Override
	public boolean init() {
//		session = new JSONRPCTcpClient(getValue(SERVER), Integer.valueOf(getValue(PORT)));
		try {
			session = new JSONRPCHttpClient(
					getValue(SERVER), 
					Integer.valueOf(getValue(PORT)), 
					getValue(USERNAME), 
					getDecryptedValue(PASSWORD));
			session.setConnectionTimeout(TIMEOUT);
			session.setSoTimeout(TIMEOUT);

			
			// determinate version
			apiVersion = session.callJSONObject("JSONRPC.Version").getInt("version");
			//Xbmc.debug("\n\n-----CHECKING API VERSION "+apiVersion);
			
//			String version = session.callJSONObject("System.GetInfoLabels",
//					new JSONArray().put("System.BuildVersion")).getString("System.BuildVersion");
//
//			session.callJSONObject("System.GetInfoLabels",
//					new JSONObject().put("labels", new JSONArray()
//					.put("System.BuildVersion"))).getString("System.BuildVersion");
		
			switch (apiVersion) {
			case 2:
				//debug("DHARMA DETECTED: "+version);
				controller.defineDharmaCommands();
				break;
			case 3:
				//debug("EDEN DETECTED: "+version);
				controller.defineEdenCommands();
				break;
			case 4:
			default:
				// tbd
				break;
			}
			
		} catch (JSONRPCException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;//session.isConnected();
	}
	

	@Override
	public void close() {
		session = null;
	}

	@Override
	public boolean isActive() {
		if (session == null) return false;
		else
			try {
				HttpPost post = new HttpPost(
						"http://"+getValue(Xbmc.SERVER)+":"+getValue(Xbmc.PORT));
				post.addHeader("User-Agent",
						"Mozilla/4.0 (compatible; MSIE 6.0; Windows 2000)");
				
				if (session.getHttpClient().execute(post)!= null)
					return true;
				
			} catch (Exception e) {
				e.printStackTrace();
			} 
		return false;
	}

	@Override
	public boolean isPasswordRequired() {
		return false;
	}

	@Override
	public void setPassword(String arg0) {
	}

	@Override
	public String getTargetNameField() {
		return SERVER;
	}

	@Override
	public void onConfigurationUpdate() {

	}
}
