package com.coversal.plugin.json;

import android.os.RemoteException;
import android.text.InputType;

import com.coversal.ucl.api.BrowsableAPI;
import com.coversal.ucl.api.TextParameter;
import com.coversal.ucl.plugin.PluginAnnouncer;
import com.coversal.ucl.plugin.Profile;

public abstract class JsonProfile extends Profile {
	
	private static final String SERVER = "Server Address";
	private static final String PORT = "Port";
	
	JSONRPCTcpClient session;
	JsonBrowser browser;
	
	public JsonProfile(PluginAnnouncer pa) {
		super(pa);

		defineParameter(SERVER, new TextParameter(null, true));
		defineParameter(PORT, new TextParameter("9090", true, InputType.TYPE_CLASS_NUMBER));
		
		browser = new JsonBrowser(this);
	}

	public JSONRPCTcpClient getJsonClient() {
		return session;
	}
	
	@Override
	public boolean init() throws RemoteException {
		session = new JSONRPCTcpClient(getValue(SERVER), Integer.valueOf(getValue(PORT)));
		session.setConnectionTimeout(2000);
		session.setSoTimeout(2000);
		return session.isConnected();
	}
	

	@Override
	public void close() throws RemoteException {
		session = null;
	}

	@Override
	public BrowsableAPI getBrowser() throws RemoteException {
		return browser;
	}

	@Override
	public boolean isActive() throws RemoteException {
		if (session == null) return false;
		else return session.isConnected();
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
