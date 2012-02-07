package com.coversal.plugin.xbmc;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.RemoteException;
import android.text.InputType;

import com.coversal.plugin.json.JSONRPCException;
import com.coversal.plugin.json.zJSONRPCHttpClient;
import com.coversal.ucl.api.BrowsableAPI;
import com.coversal.ucl.api.ControllerAPI;
import com.coversal.ucl.api.TextParameter;
import com.coversal.ucl.plugin.PluginAnnouncer;
import com.coversal.ucl.plugin.Profile;


public class Xbmc extends Profile {

	static final String SERVER = "Server Address";
	static final String PORT = "Port";
	private static final String USERNAME = "Username (optional)";
	private static final String PASSWORD = "Password (optional)";
	private static final int TIMEOUT = 2000;
	
	XbmcController controller = new XbmcController(this);
	XbmcBrowser browser = new XbmcBrowser(this);
	JSONObject currentObject;
	zJSONRPCHttpClient session;

	boolean isDharma = false;
	
	public Xbmc(PluginAnnouncer pa) {
		super(pa);
		
		defineParameter(SERVER, new TextParameter(null, true));
		defineParameter(PORT, new TextParameter("8080", true, InputType.TYPE_CLASS_NUMBER));
		defineParameter(USERNAME, new TextParameter("xbmc", false));
		defineParameter(PASSWORD, new TextParameter("xbmc", false, InputType.TYPE_TEXT_VARIATION_PASSWORD));
	}
		
	@Override
	public ControllerAPI getController() throws RemoteException {
		return controller;
	}
	
	@Override
	public BrowsableAPI getBrowser() throws RemoteException {
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

	
	public zJSONRPCHttpClient getJsonClient() {
		return session;
	}
	
	@Override
	public boolean init() throws RemoteException {
//		session = new JSONRPCTcpClient(getValue(SERVER), Integer.valueOf(getValue(PORT)));
		session = new zJSONRPCHttpClient(
				getValue(SERVER), 
				Integer.valueOf(getValue(PORT)), 
				getValue(USERNAME), 
				getValue(PASSWORD));
		session.setConnectionTimeout(TIMEOUT);
		session.setSoTimeout(TIMEOUT);
		
		// determinate version
		try {
			String version = session.callJSONObject("System.GetInfoLabels",
					new JSONArray().put("System.BuildVersion")).getString("System.BuildVersion");
			
			if (version.matches("^10.+")) {
				controller.defineDharmaCommands();
				isDharma = true;
			}
			else {
				controller.defineEdenCommands();
			}
		} catch (JSONRPCException e) {
			// probably didn't like the System.GetInfoLabel so lets say we're with Eden
			debug("EDEN DETECTED (version unkunown) ");
			controller.defineEdenCommands();
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return true; //session.isConnected();
	}
	

	@Override
	public void close() throws RemoteException {
		session = null;
	}

	@Override
	public boolean isActive() throws RemoteException {
		if (session == null) return false;
		else return true; //session.isConnected();
	}

	@Override
	public boolean isPasswordRequired() throws RemoteException {
		return false;
	}

	@Override
	public void setPassword(String arg0) throws RemoteException {
	}

	@Override
	public String getTargetNameField() {
		return SERVER;
	}

	@Override
	public void onConfigurationUpdate() {

	}
}
