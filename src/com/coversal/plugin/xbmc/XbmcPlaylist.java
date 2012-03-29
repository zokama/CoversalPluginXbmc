package com.coversal.plugin.xbmc;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.RemoteException;

import com.coversal.plugin.json.JSONRPCException;
import com.coversal.plugin.xbmc.XbmcBrowser.BrowserObject;
import com.coversal.ucl.api.AdapterItem;
import com.coversal.ucl.plugin.PlaylistManager;

public class XbmcPlaylist extends PlaylistManager {

	private static final int PLAYLIST_ID = 0;
	Xbmc profile;
	JSONArray playlistObjects;

	//private String currentMedia;

	
	public XbmcPlaylist(Xbmc p) {
		profile = p;
		playlistObjects = new JSONArray();
			
	}

	
	
	// We overwrite this in order to support the secured http connection  
	protected Bitmap downloadBitmap(String url) {
		
		HttpGet get = new HttpGet(url);
		get.addHeader("User-Agent",
				"Mozilla/4.0 (compatible; MSIE 6.0; Windows 2000)");

		
		Bitmap btm = null;
		BufferedInputStream buf;
        
		try {
			// reusing JSON http client. would need to be changed if we use JSON over TCP  
			HttpResponse response  = profile.getJsonClient().getHttpClient().execute(get);
			InputStream in = response.getEntity().getContent();
			buf = new BufferedInputStream(in);
	        btm = BitmapFactory.decodeStream(buf);
	        buf.close();
	        in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}		
		
		if (btm == null) return null;
		
		// resizing bitmap
		int width = btm.getWidth(); 
		int height = btm.getHeight();
		
		float scale = ((float) 50) / width; 
		
		Matrix matrix = new Matrix(); 
		matrix.postScale(scale, scale); 
		
		Bitmap resizedBitmap = Bitmap.createBitmap(btm, 0, 0, width, height, matrix, false); 
		btm.recycle();
		
		return resizedBitmap;
	}
	

//	JSONObject getFromPlaylist(String item) {
//		try {
//			for (int i=0; i< playlistObjects.length(); i++) {
//				if (item.equals(playlistObjects.getJSONObject(i).getString("label")))
//					return playlistObjects.getJSONObject(i).put("index", i);
//					}
//			
//			// if we get there it means that we didn't get anything with label before
//			for (int i=0; i< playlistObjects.length(); i++) {
//				String filePath = playlistObjects.getJSONObject(i).getString("file");
//				
//				if (filePath != null && filePath.contains(item))
//					return playlistObjects.getJSONObject(i).put("index", i);
//			}
//		} catch (JSONException e) {e.printStackTrace();	}
//		
//		return null;
//	}
	
//	int getIndex(JSONArray arr, JSONObject obj) {
//		for( int i =0; i<playlistObjects.length(); i++)
//			try {
//				if (playlistObjects.getJSONObject(i).equals(obj))
//					return i;
//			} catch (JSONException e) {e.printStackTrace();	}
//		
//		return -1;
//	}

	


	@Override
	public void add(String item) throws RemoteException {
		
		//String media = ((XbmcBrowser)profile.getBrowser()).currentBrowserObject.media;
		String method = getPlaylistMethod()+".Add";
		
		JSONObject param;
		BrowserObject bo = ((XbmcBrowser)profile.getBrowser())
				.currentBrowserObject.subCategories.get(item);
		if (bo == null) return;
		
		try {
			for (String s: bo.getAllFiles()) {
			
				if (profile.apiVersion == 2) {
					param = new JSONObject().put("file", s);
				}
				else
					param = new JSONObject().put("item",
							new JSONObject().put("file", s));
				
				if (profile.apiVersion > 3) {
					param.put("playlistid", PLAYLIST_ID);
				}
				
				profile.getJsonClient().call(method, param);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}



	@Override
	public void clear() throws RemoteException {
		try {
			JSONObject param = null;
			
			if (profile.apiVersion > 3)
				param = new JSONObject().put("playlistid", PLAYLIST_ID);
			
			profile.getJsonClient().call(getPlaylistMethod()+".Clear", param);
			
		} catch (JSONRPCException e) { e.printStackTrace(); 
		} catch (JSONException e) { e.printStackTrace(); }
	}



	@Override
	public Bitmap getCover(int arg0) throws RemoteException {
//		JSONObject jsonItem = getFromPlaylist(item);
//		
//		try {
//			//Xbmc.debug("searching cover for "+item);
//			if (jsonItem == null) return null;
//			
//			
//			String tbnPath = null;
//
//			if (jsonItem.has("thumbnail"))
//				tbnPath = jsonItem.getString("thumbnail");
//			Xbmc.debug("\n\ndownloading cover thumbnail "+tbnPath);
//			
//			if ((tbnPath == null || tbnPath.equals("")) && jsonItem.has("fanart"))
//				tbnPath = jsonItem.getString("fanart");
//			Xbmc.debug("\n\ndownloading cover fanart "+tbnPath);
//			
//			if (tbnPath == null || tbnPath.equals("")) return null;
//			
//			Xbmc.debug("\n\ndownloading cover 1 "+tbnPath+"\n\n");
//			
//			JSONObject tbn;
//			if (profile.apiVersion>2)
//				tbn = profile.getJsonClient().callJSONObject("Files.Download", 
//							new JSONObject().put("path", tbnPath));
//			else
//				tbn = profile.getJsonClient().callJSONObject("Files.Download", tbnPath);
//			
//			String tbnUrl = "http://"+profile.getValue(Xbmc.SERVER)+":"+profile.getValue(Xbmc.PORT)+"/"
//						+URLEncoder.encode(tbn.getString("path"), "UTF-8");
//						
//			Xbmc.debug("\n\ndownloading cover 2 "+tbnUrl+"\n\n");
//			
//			return downloadBitmap(tbnUrl);
//
//		} catch (Exception e) {
//			//e.printStackTrace();
//		}
		return null;
	}



	@Override
	public int getPlayingMediaIndex() throws RemoteException {
		
//		try {
//			if (profile.apiVersion < 3){
//				JSONObject playlist = profile.getJsonClient()
//						.callJSONObject(getPlaylistMethod()+".GetItems");
//				
//				if (playlist.has("current"))
//						return playlist.getInt("current");
//			}
//			else if (profile.apiVersion == 3){
//				JSONObject state = profile.getJsonClient()
//						.callJSONObject(getPlaylistMethod()+".State");
//								
//				if (state != null && state.has("current"))
//					return state.getInt("current");
//			}
//			else {
//				
//				Xbmc.debug("----------------------\n\n"+profile.getJsonClient()
//						.callJSONObject("Player.GetProperties", new JSONObject()
//						.put("playerid", profile.currentPlayerId)
//						.put("properties", new JSONArray().put("position").put("playlistid"))));
				
//				return profile.getJsonClient()
//						.callJSONObject("Player.GetProperties", new JSONObject()
//						.put("playerid", 0)
//						.put("properties", new JSONArray().put("position")))
//						.getInt("position");
				
//			}
//			
//		} catch (JSONRPCException e) { e.printStackTrace();
//		} catch (JSONException e) { e.printStackTrace(); }
		
		return profile.currentPlaylistPosition;
	}


	private String getPlaylistMethod() {
		String method = "Playlist";

		// determinate player type (video, audio, picture)
		try {
			if (profile.apiVersion < 4) {
				JSONObject activePlayers = profile.getJsonClient().callJSONObject("Player.GetActivePlayers");
				
				if (activePlayers.getBoolean("video"))
					method = "VideoPlaylist";
				
				else 
					method = "AudioPlaylist";
			}	
		} catch (JSONRPCException e) {e.printStackTrace();
		} catch (JSONException e) {e.printStackTrace();}

		return method;
	}

	
	
	
	@Override
	public List<AdapterItem> getPlaylistItems() throws RemoteException {
		
		try {
//			Xbmc.debug("raw results: "+profile
//					.getJsonClient()
//					.callJSONObject(getPlaylistMethod()+".GetItems").toString());
			
			JSONObject param = null;
			
			if (profile.apiVersion > 3)
				param = new JSONObject().put("playlistid", PLAYLIST_ID);
			
			playlistObjects = profile
					.getJsonClient()
					.callJSONObject(getPlaylistMethod()+".GetItems", param)
					.getJSONArray("items");
								
			ArrayList<AdapterItem> list = new ArrayList<AdapterItem>();
			for (int i=0; i< playlistObjects.length(); i++) {
				
				String toadd = playlistObjects.getJSONObject(i).getString("label");
				
				if (toadd == null || toadd.equals("")) {
					toadd = playlistObjects.getJSONObject(i).getString("file").replaceAll("^.*[\\\\/]([^\\\\/]*)$", "$1");
				}
				
				list.add(new AdapterItem(-1, toadd, null, null, null));
			}
				
			return list;
		} catch (Exception e) {e.printStackTrace();}
			
		
		return null;
	}



	@Override
	public void play(int position, long id) throws RemoteException {
		
		String method = getPlaylistMethod();
		
		try {
			if (profile.apiVersion == 2) {
				profile.getJsonClient().call(method+".Play", position);
			}
			else if (profile.apiVersion == 3){ 
				profile.getJsonClient().call(method+".Play",
						new JSONObject().put("item", position));
			}
			else { // API v4
				profile.getJsonClient().call("Player.Open",
						new JSONObject().put("item", 
						new JSONObject().put("playlistid", PLAYLIST_ID).put("position", position)));
			}
			
		} catch (JSONRPCException e) { e.printStackTrace(); 
		} catch (JSONException e) {	e.printStackTrace(); }

	}



	@Override
	public void remove(int position, long id) throws RemoteException {
		JSONObject param;

		try {
		
			if (profile.apiVersion == 2) {
				param = new JSONObject()
						//.put("playlist", "music")
						.put("item", position);

				profile.getJsonClient().call(
						"Playlist.Remove", param);
				
			}
			else if (profile.apiVersion == 3){
				param = new JSONObject().put("item",position);
				
				profile.getJsonClient().call(
						"AudioPlaylist.Remove", param);
			} 
			else {
				param = new JSONObject().put("playlistid", PLAYLIST_ID).put("position", position);
				
				profile.getJsonClient().call(
						"Playlist.Remove", param);
			}
		
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	
}
