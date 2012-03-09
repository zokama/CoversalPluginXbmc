package com.coversal.plugin.xbmc;

import java.io.BufferedInputStream;
import java.io.InputStream;
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
//	HashMap<String, JSONObject> currentObjects;
	
	//private String currentDir;
	//private String currentMedia;
	
	private ArrayList<String> directoriesList = new ArrayList<String>();
	private ArrayList<String> filesList = new ArrayList<String>();
	static private BrowserObject homeBrowserObject;
	private BrowserObject currentBrowserObject; 
	
	private class BrowserObject {
		String method;
		String media;
		String result;
		JSONObject param;
		JSONObject json;
		HashMap<String, BrowserObject> subCategories;
		BrowserObject parent;
		
		private BrowserObject (BrowserObject bo, JSONObject jo) {
			parent = bo;
			method = null;
			media = null;
			result = null;
			param = null;
			subCategories = new HashMap<String, BrowserObject>();
			json = jo;			
		}
		
		BrowserObject(BrowserObject bo, String meth, String med, String r, JSONObject jo) {
			parent = bo;
			method = meth;
			media = med;
			result = r;
			param = jo;
			subCategories = new HashMap<String, BrowserObject>();
			json =null;
		}
		
		void put(String name, String method, String media, String result, JSONObject param) {
			subCategories.put(name, new BrowserObject(this, method, media, result, param));
		}
		
		void put(String name, BrowserObject bo) {
			subCategories.put(name, bo);
		}
		
		void put(String name, JSONObject jo) {
			subCategories.put(name, new BrowserObject(this, jo));
		}
		
		String getPath(BrowserObject bo) {
			if (bo != null) {
				for (String s: subCategories.keySet())
					if (bo.equals(subCategories.get(s))) {
						if (parent == null) return s;
						else return parent.getPath(this)+"/"+s;
				}
			}
			else if (parent != null) return parent.getPath(this);
			
			return HOME_STR;			
		}
	}
	
	public XbmcBrowser(Xbmc p) {
		super(p);
		profile = p;
		//currentMedia = "";
		
		homeBrowserObject = new BrowserObject(null, null, null, null, null);
		try {

			homeBrowserObject.put("1-Movies", "VideoLibrary.GetMovies", null, "movies", 
					new JSONObject().put("fields",	new JSONArray()
							.put("thumbnail")
							.put("file")) );
			
			homeBrowserObject.put("2-Series", "VideoLibrary.GetTVShows", null, "tvshows", null);
			
			BrowserObject music = new BrowserObject(homeBrowserObject, null, null, null, null);
				music.put("Albums", "AudioLibrary.GetAlbums", null, "albums", 
						new JSONObject().put("fields",	new JSONArray().put("thumbnail")));
				music.put("Artists", "AudioLibrary.GetArtists", null, "artists", null);
				music.put("Genre", "AudioLibrary.GetGenres", null, "genres", null);
				homeBrowserObject.put("3-Music", music);
		
				homeBrowserObject.put("4-All Video Files", "Files.GetSources", "video", "shares", 
					new JSONObject().put("media", "video"));
			
				homeBrowserObject.put("5-All Music Files", "Files.GetSources", "music", "shares", 
					new JSONObject().put("media", "music"));
			
				homeBrowserObject.put("6-Pictures", "Files.GetSources", "pictures", "shares", 
					new JSONObject().put("media", "pictures"));
			
		} catch (JSONException e) {
			e.printStackTrace();
		}

		currentBrowserObject = homeBrowserObject;
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
	
	
	private JSONArray browsePath(BrowserObject bo) throws JSONRPCException, JSONException {
		JSONObject param = new JSONObject()
				.put("directory", bo.json.get("file"))
				.put("media", bo.media);//new JSONArray().put("video").put("music").put("picture"));
		
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
		
//		if (getHomeDir().equals(item)) browseHistory.clear();
//		if (currentDir != null) browseHistory.addFirst(currentDir);
		
		ArrayList<AdapterItem> list = new ArrayList<AdapterItem>();

		Xbmc.debug("BROWSING: "+item);
		
		// browse home
		if (item == null)
			item = profile.getOptionValue(OPTION_HOME_DIR);
		
		if (item == null || item.equals(HOME_STR)) {
			currentBrowserObject = homeBrowserObject;
			item = HOME_STR;
		}
		
		// browse back
		else if (BACK_STR.equals(item) && currentBrowserObject.parent != null)
			currentBrowserObject = currentBrowserObject.parent; 

		// browse forward
		else if (currentBrowserObject.subCategories.containsKey(item)) 
			currentBrowserObject = currentBrowserObject.subCategories.get(item);
		
		// unknown item. full path?
		else {
			currentBrowserObject = homeBrowserObject;
			String path[] = item.split("/");
			int i = 0;
			while (i<path.length && currentBrowserObject.subCategories.containsKey(path[i])) {
				Xbmc.debug(i+"/EXPERIMENTAL recreating path: "+currentBrowserObject.getPath(null));
				currentBrowserObject = currentBrowserObject.subCategories.get(path[i++]);
			}
		}
			
		if (currentBrowserObject.parent!= null)
			list.add(new AdapterItem(-1, BACK_STR, null, null, null));
		
		if(currentBrowserObject.method == null
				&& currentBrowserObject.json == null
				&& currentBrowserObject.subCategories.size() > 0) {
			
			directoriesList.clear();
			
			for (String s: currentBrowserObject.subCategories.keySet()){
				list.add(new AdapterItem(-1, s, null, null, null));
				directoriesList.add(s);
			}
		}
		
		else {
			try {
				JSONArray json;
				
				if (currentBrowserObject.json != null) {
					//currentDir = currentBrowserObject.json.getString("file");
					if (currentBrowserObject.json.has("albumid")) {
						currentBrowserObject.method = "AudioLibrary.GetSongs";
						currentBrowserObject.result="songs";
						currentBrowserObject.param = new JSONObject()
								.put("albumid", currentBrowserObject.json.get("albumid"))
								.put("fields",	new JSONArray().put("thumbnail").put("file"));
					}
					else if (currentBrowserObject.json.has("artistid")) {
						currentBrowserObject.method = "AudioLibrary.GetSongs";
						currentBrowserObject.result="songs";
						currentBrowserObject.param = new JSONObject()
								.put("artistid", currentBrowserObject.json.get("artistid"))
								.put("fields",	new JSONArray().put("thumbnail").put("file"));
					}
					else if (currentBrowserObject.json.has("genreid")) {
						currentBrowserObject.method = "AudioLibrary.GetSongs";
						currentBrowserObject.result="songs";
						currentBrowserObject.param = new JSONObject()
								.put("genreid", currentBrowserObject.json.get("genreid"))
								.put("fields",	new JSONArray().put("thumbnail").put("file"));
					}
					else {
						currentBrowserObject.method = null;
					}
				}
				
				if (currentBrowserObject.method != null) {
					json = profile
							.getJsonClient()
							.callJSONObject(currentBrowserObject.method, currentBrowserObject.param)
							.getJSONArray(currentBrowserObject.result);
					
					if (!currentBrowserObject.method.equals("VideoLibrary.GetMovies")
							&& !currentBrowserObject.method.equals("AudioLibrary.GetSongs")) {
						//currentMedia = currentBrowserObject.media;
						
						directoriesList.clear();
						for (int i = 0; i < json.length();
							directoriesList.add(json.getJSONObject(i++).getString("label")));
					}	
				}
				else if (currentBrowserObject.json.has("file")){
					json = browsePath(currentBrowserObject);
				}
				else
					throw new Exception ("No JSON method found, no file information for browsing.");
				
				
//				else {
//					// we end up here when a path is passed on as item: in case of home dir or browse back
//					//currentDir = item;
//					json = browsePath(item);
//				}
					
				//currentObjects = new HashMap<String, JSONObject>();
				for (int i = 0; i < json.length();i++){
	
					//Xbmc.debug("fields: "+json.getJSONObject(i).names());
					list.add(new AdapterItem(-1, json.getJSONObject(i).getString("label"),
		  				null, null, null));
		  		
					currentBrowserObject.put(json.getJSONObject(i).getString("label"), json.getJSONObject(i));
					
					Xbmc.debug("json object: "+json.getJSONObject(i));
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return list;
	}

	
	@Override
	public List<AdapterItem> browseBack() throws RemoteException {
		//String toBrowse = browseHistory.poll();
		//currentDir = browseHistory.poll();
		return browse(BACK_STR);
	}

	
	@Override
	public Bitmap getCover(String item) throws RemoteException {
		try {
			Xbmc.debug("searching cover for "+item);
			if (!currentBrowserObject.subCategories.containsKey(item) 
					||currentBrowserObject.subCategories.get(item).json == null) return null;
			
			JSONObject tbn;
			String tbnPath = currentBrowserObject.subCategories.get(item).json.getString("thumbnail");
			
			if (profile.apiVersion>2)
				tbn = profile.getJsonClient().callJSONObject("Files.Download", 
							new JSONObject().put("path", tbnPath));
			else
				tbn = profile.getJsonClient().callJSONObject("Files.Download", tbnPath);
			
			String tbnUrl = "http://"+profile.getValue(Xbmc.SERVER)+":"+profile.getValue(Xbmc.PORT)+"/"
						+URLEncoder.encode(tbn.getString("path"), "UTF-8");
						
			return downloadBitmap(tbnUrl);

		} catch (Exception e) {
			//e.printStackTrace();
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
		return currentBrowserObject.getPath(null);
	}

	@Override
	public String getFullPath(String item) throws RemoteException {
		return item;
	}

	@Override
	public int getItemType(String item) throws RemoteException {
		if (item.equals(BACK_STR))
			return ITEM_TYPE_BACK;
		
		if (directoriesList.contains(item)) 
			return ITEM_TYPE_DIRECTORY;
		
		return ITEM_TYPE_MULTIMEDIA;
	}


	@Override
	public List<AdapterItem> onItemSelected(String item) {
		super.onItemSelected(item);
		profile.currentObject = currentBrowserObject.subCategories.get(item).json;
		return null; 
	}

	
}
