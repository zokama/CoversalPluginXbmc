package com.coversal.plugin.xbmc;

import android.os.RemoteException;

import com.coversal.plugin.json.JsonProfile;
import com.coversal.ucl.api.BrowsableAPI;
import com.coversal.ucl.api.ControllerAPI;
import com.coversal.ucl.plugin.PluginAnnouncer;


public class Xbmc extends JsonProfile {

	public Xbmc(PluginAnnouncer pa) {
		super(pa);
	}

	XbmcController controller = new XbmcController(this);
	
	@Override
	public ControllerAPI getController() throws RemoteException {
		return controller;
	}
	
	@Override
	public BrowsableAPI getBrowser() throws RemoteException {
		return null;
	}

	@Override
	public String getProfileName() {
		return "XBMC";
	}

	@Override
	public String getIconName() {
		return "xbmc";
	}

}
