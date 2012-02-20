package com.coversal.plugin.xbmc;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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
	
	private String currentDir;
	private LinkedList<String> browseHistory = new LinkedList<String>();
	private ArrayList<String> directoriesList = new ArrayList<String>();
	private ArrayList<String> filesList = new ArrayList<String>();
	
	private static final HashMap<String, String[]> CATEGORIES = new HashMap<String, String[]>(){
		private static final long serialVersionUID = 1L;{
			
		put("Recently Added Movies", new String[]{"VideoLibrary.GetRecentlyAddedMovies", "movies"});
		put("All Files", new String[]{"Files.GetDirectory", "directories"});
		put("Movies", new String[]{"VideoLibrary.GetMovies","movies"});

		}};	
		
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
	
	
	private JSONArray browsePath(String item) throws JSONRPCException, JSONException {
		JSONObject param = new JSONObject().put("directory", item);
		//param.put("media", "video");//new JSONArray().put("video").put("music").put("picture"));
		
		JSONObject tmpResult = profile
				.getJsonClient()
				.callJSONObject("Files.GetDirectory", param);
		
		JSONArray tmpArr;
		JSONArray json = new JSONArray();
	
		try {
			tmpArr = tmpResult.getJSONArray("directories");
			for (int i = 0; i < tmpArr.length(); i++) {
				directoriesList.add(tmpArr.getJSONObject(i).getString("label"));
				json.put(tmpArr.get(i));
			}
		} catch (Exception e) {
			// throws an exception when no directories found...
		}

		tmpArr = tmpResult.getJSONArray("files");
		
		for (int i = 0; i < tmpArr.length(); i++) {
			String fileType="-";
			try {
				fileType = tmpArr.getJSONObject(i).getString("filetype");
			} catch (Exception e) {}
			
			if (fileType.equals("directory"))
				directoriesList.add(tmpArr.getJSONObject(i).getString("label"));
			else// if(fileType.equals("file"))
				filesList.add(tmpArr.getJSONObject(i).getString("label"));
			json.put(tmpArr.get(i));
		}
		
		return json;
	}
	
	@Override
	public List<AdapterItem> browse(String item) throws RemoteException {
		
		if (getHomeDir().equals(item)) browseHistory.clear();
		if (currentDir != null) browseHistory.addFirst(currentDir);
		
		ArrayList<AdapterItem> list = new ArrayList<AdapterItem>();

		if (item == null || item.equals(HOME_STR)) {
			for (String s: CATEGORIES.keySet())
				list.add(new AdapterItem(-1, s, null, null, null));
			
			currentDir = HOME_STR;
		}
		else {
			list.add(new AdapterItem(-1, BACK_STR, null, null, null));
			
			try {
				JSONArray json;
				if (item.equals("All Files")) {
					// Updating current dir
					currentDir = item;
					
					JSONObject param = new JSONObject().put("media","video");
					
					json = profile
							.getJsonClient()
							.callJSONObject("Files.GetSources", param)
							.getJSONArray("shares");
					
					directoriesList.clear();
					for (int i = 0; i < json.length();
							directoriesList.add(json.getJSONObject(i++).getString("label")));
				}
				else if (CATEGORIES.containsKey(item)) {
					// Updating current dir
					currentDir = item;
					JSONObject param = new JSONObject().put("fields", 
							new JSONArray()
									.put("thumbnail")
									.put("file"));
					
					json = profile
							.getJsonClient()
							.callJSONObject(CATEGORIES.get(item)[0], param)
							.getJSONArray(CATEGORIES.get(item)[1]);
					
				}
				else if (directoriesList.contains(item)) {
					// Updating current dir
					currentDir = currentObjects.get(item).getString("file");
					json = browsePath(currentDir);
				}
					

//				else if (item.equals("Audio")) {
//					JSONObject param = new JSONObject().put("genreid",-1);
//					
//					json = profile
//							.getJsonClient()
//							.callJSONObject("AudioLibrary.GetArtists", param)
//							.getJSONArray("artists");
//				}
//				else if (item.equals("Video")) {
//					JSONObject param = new JSONObject().put("fields", 
//							new JSONArray()
//									.put("thumbnail")
//									.put("file"));
//					
//					json = profile
//							.getJsonClient()
//							.callJSONObject("VideoLibrary.GetMovies", param)
//							.getJSONArray("movies");
//				}
				else {
					// we end up here when a path is passed on as item
					currentDir = item;
					json = browsePath(item);
				}
					
				currentObjects = new HashMap<String, JSONObject>();
				for (int i = 0; i < json.length();i++){
	
					//Xbmc.debug("fields: "+json.getJSONObject(i).names());
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
		String toBrowse = browseHistory.poll();
		currentDir = browseHistory.poll();
		return browse(toBrowse);
	}

	
	@Override
	public Bitmap getCover(String item) throws RemoteException {
		try {
			if (currentObjects == null || !currentObjects.containsKey(item)) return null;
			if (CATEGORIES.containsKey(item)) return null;
			
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

		} catch (Exception e) {
			e.printStackTrace();
		}
//		} catch (JSONRPCException e) {
//			e.printStackTrace();
//		} catch (JSONException e) {
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		} 

		return null;
	}

	@Override
	public String getCurrentDir() throws RemoteException {
		return currentDir;
	}

	@Override
	public String getFullPath(String item) throws RemoteException {
		return item;
	}

	@Override
	public int getItemType(String item) throws RemoteException {
		if (item.equals(BACK_STR))
			return ITEM_TYPE_BACK;
		
		if (CATEGORIES.containsKey(item) || directoriesList.contains(item)) 
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
