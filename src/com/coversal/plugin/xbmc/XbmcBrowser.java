package com.coversal.plugin.xbmc;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.coversal.ucl.api.AdapterItem;
import com.coversal.ucl.plugin.Browsable;

public class XbmcBrowser extends Browsable {

	
	Xbmc profile;
	HashMap<String, JSONObject> currentObjects;
	
	private static final String[] categories = {"Files", "Audio", "Video"}; 
//		Files.GetSources
//		Files.Download
//		Files.GetDirectory
//
//		AudioLibrary.GetArtists
//		AudioLibrary.GetAlbums
//		AudioLibrary.GetSongs
//		AudioLibrary.ScanForContent
//		
//		VideoLibrary.GetMovies
//		VideoLibrary.GetTVShows
//		VideoLibrary.GetSeasons
//		VideoLibrary.GetEpisodes
//		VideoLibrary.GetMusicVideos
//		VideoLibrary.GetRecentlyAddedMovies
//		VideoLibrary.GetRecentlyAddedEpisodes
//		VideoLibrary.GetRecentlyAddedMusicVideos
//		VideoLibrary.ScanForContent};


	public XbmcBrowser(Xbmc p) {
		super(p);
		profile = p;
	}

	
	private Bitmap downloadBitmap(String url) {
		
		
		
		
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
		
		
		// resizing bitmap
		int width = btm.getWidth(); 
		int height = btm.getHeight();
		
		float scale = ((float) 50) / width; 
		
		Matrix matrix = new Matrix(); 
		matrix.postScale(scale, scale); 
		
		Bitmap resizedBitmap = Bitmap.createBitmap(btm, 0, 0, width, height, matrix, false); 
		btm.recycle();
		
		
		
//		Bitmap btm = null;
//		InputStream in;
//        BufferedInputStream buf;
//		
//        if (url == null || url.equals("")) return null;
//        	
//		try {
////			HttpURLConnection con = (HttpURLConnection)  new URL("http", "192.168.1.37", 8080,"/vfs/special://masterprofile/Thumbnails/Video/5/56ddda34.tbn").openConnection();
//			HttpURLConnection con = profile.session.getHttpClient().;//(HttpURLConnection) new URL(url).openConnection();
//		
//			if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
//				 // valid response, retrieve your input stream
//			
//	            con.setRequestMethod("GET");
//	            con.setDoOutput(true);
//	            con.setDoInput(true);
//	            con.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
//	            con.setRequestProperty("Accept","[star]/[star]");
//	            con.connect();
//	            
//			// in = new URL(url).openStream();
//            in = con.getInputStream();
//			buf = new BufferedInputStream(in);
//	        btm = BitmapFactory.decodeStream(buf);
//	        in.close();
//	        con.disconnect();
//			}
//			else {
//				 JsonProfile.debug("Connection failed: "+con.getResponseCode());
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}  
		
		return resizedBitmap;
	}
	
	@Override
	public List<AdapterItem> browse(String item) throws RemoteException {

		ArrayList<AdapterItem> list = new ArrayList<AdapterItem>();

		if (item == null) {

			list.add(new AdapterItem(-1, "Files", null, null, null));
			list.add(new AdapterItem(-1, "Audio", null, null, null));
			list.add(new AdapterItem(-1, "Video", null, null, null));
		}
		else {
			try {
				JSONArray json;
				
				if (item.equals("Files")) {
					JSONObject param = new JSONObject().put("media","video");
					
					json = profile
							.getJsonClient()
							.callJSONObject("Files.GetSources", param)
							.getJSONArray("shares");
				}
				else if (item.equals("Audio")) {
					JSONObject param = new JSONObject().put("genreid",-1);
					
					json = profile
							.getJsonClient()
							.callJSONObject("AudioLibrary.GetArtists", param)
							.getJSONArray("artists");
				}
				else if (item.equals("Video")) {
					JSONObject param = new JSONObject().put("fields", 
							new JSONArray()
									.put("thumbnail")
									.put("file"));
					
					json = profile
							.getJsonClient()
							.callJSONObject("VideoLibrary.GetMovies", param)
							.getJSONArray("movies");
				}
				else
					return list;
					
	
				currentObjects = new HashMap<String, JSONObject>();
				for (int i = 0; i < json.length();i++){
	
					Xbmc.debug("fields: "+json.getJSONObject(i).names());
					list.add(new AdapterItem(-1, json.getJSONObject(i).getString("label"),
		  				null, null, null));
		  		
					currentObjects.put(json.getJSONObject(i).getString("label"), json.getJSONObject(i));
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return list;
	}

	@Override
	public List<AdapterItem> browseBack() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bitmap getCover(String item) throws RemoteException {
		try {
			if (currentObjects == null || !currentObjects.containsKey(item)) return null;
			
			for(int i=0; i<categories.length; i++)
				if (categories[i].equals(item))
					return null;
			
			
			JSONObject tbn;
			
			if (!profile.isDharma)
				tbn = profile
					.getJsonClient().callJSONObject("Files.Download", 
							new JSONObject().put("path", 
							currentObjects.get(item).getString("thumbnail")));
			else
				tbn = profile.getJsonClient().callJSONObject(
						"Files.Download", currentObjects.get(item).getString("thumbnail"));
			
			String tbnUrl = "http://"+profile.getValue(Xbmc.SERVER)+":"+profile.getValue(Xbmc.PORT)+"/"
						+URLEncoder.encode(tbn.getString("path"), "UTF-8");
						
			return downloadBitmap(tbnUrl);

		} catch (JSONRPCException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} 

		return null;
	}

	@Override
	public String getCurrentDir() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFullPath(String item) throws RemoteException {
		return item;
	}

	@Override
	public int getItemType(String item) throws RemoteException {
		for(int i=0; i<categories.length; i++)
			if (categories[i].equals(item))
				return ITEM_TYPE_DIRECTORY;
		
		return ITEM_TYPE_MULTIMEDIA;
	}


	@Override
	public List<AdapterItem> onItemSelected(String item) {
		super.onItemSelected(item);
		profile.currentObject = currentObjects.get(item);
		return null; 
	}

	
}
