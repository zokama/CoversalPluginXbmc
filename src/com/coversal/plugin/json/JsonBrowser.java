package com.coversal.plugin.json;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import android.graphics.Bitmap;
import android.os.RemoteException;

import com.coversal.ucl.api.AdapterItem;
import com.coversal.ucl.plugin.Browsable;

public class JsonBrowser extends Browsable {

	
	JsonProfile profile;
	
	public JsonBrowser() {
	}

	public JsonBrowser(JsonProfile p) {
		super(p);
		profile = p;
	}

	@Override
	public List<AdapterItem> browse(String item) throws RemoteException {
		
		ArrayList<AdapterItem> list = new ArrayList<AdapterItem>();
		
		try {
			JSONArray json = profile.session.callJSONObject("VideoLibrary.GetMovies").getJSONArray("movies");

			for (int i = 0; i < json.length();
			  		list.add(new AdapterItem(-1, json.getJSONObject(i++).getString("label"),
			  				null, null, null)));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		  
		return null;
	}

	@Override
	public List<AdapterItem> browseBack() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bitmap getCover(String arg0) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCurrentDir() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFullPath(String arg0) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getItemType(String arg0) throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

}
