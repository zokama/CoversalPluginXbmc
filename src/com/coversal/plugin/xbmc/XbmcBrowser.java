package com.coversal.plugin.xbmc;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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

import com.coversal.ucl.api.AdapterItem;
import com.coversal.ucl.plugin.Browsable;

public class XbmcBrowser extends Browsable {

	
	Xbmc profile;
	
	private ArrayList<String> directoriesList = new ArrayList<String>();
	private BrowserObject homeBrowserObject;
	BrowserObject currentBrowserObject; 
	
	public abstract class BrowserObject {
		String method;
		String media;
		String result;
		JSONObject param;
		JSONObject json;
		LinkedHashMap<String, BrowserObject> subCategories;
		BrowserObject parent;
		String idField;
		Class<? extends BrowserObject> subObject;
		
		BrowserObject(BrowserObject bo, String meth, String r, String m) {
			parent = bo;
			method = meth;
			media = m;
			result = r;
			param = new JSONObject();
			subCategories = new LinkedHashMap<String, BrowserObject>();
			json = null;
			idField = null;
			subObject = null;
			
			try {
				param.put("sort", new JSONObject()
						.put("method", "label").put("order", "ascending"));
			} catch (JSONException e) {	e.printStackTrace(); }
		}
		
		
		protected void put(String name, JSONObject jo) {
						
			if (subObject != null) {
				try {
					
					subCategories.put(name, subObject.getDeclaredConstructor(XbmcBrowser.class,
							BrowserObject.class, String.class)
									.newInstance(XbmcBrowser.this, BrowserObject.this, media));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
				subCategories.put(name, new SimpleList(this, null, result, media));
			}

			subCategories.get(name).json = jo;
			
			if (jo != null && idField != null)	try {
				subCategories.get(name).param.put(idField, jo.get(idField));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		
		private String getPath(BrowserObject bo) {
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
		
		
		protected JSONArray browse(boolean updateDirList) throws Exception {
			
			if (subObject == null) {
				if (profile.apiVersion<4) 
					param.put("fields", new JSONArray().put("thumbnail").put("file"));
				else
					param.put("properties", new JSONArray().put("thumbnail").put("file"));
			}
			
			
			
			if (method != null) {
				JSONArray list = profile
						.getJsonClient()
						.callJSONObject(method, param)
						.getJSONArray(result);
				
				if (updateDirList)
					directoriesList.clear();
				
				for (int i = 0; i < list.length(); i++) {
					String label = list.getJSONObject(i).getString("label");
					put(label, list.getJSONObject(i));
					
					if (updateDirList && subObject != null) directoriesList.add(label);
				}
				
				return list;
			}
	
			return null;
		}
		
		
		public ArrayList<String> getAllFiles() {

			ArrayList<String> list = new ArrayList<String>();
			
			try {
				if (browse(false) == null) {
					if (json != null && json.has("file"))
						list.add(json.getString("file"));
				}
				else {
					for (BrowserObject bo: subCategories.values()) {
						list.addAll(bo.getAllFiles());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return list;
		}
	}

	
	
	
	// Simple
	class SimpleList extends BrowserObject {

		SimpleList(BrowserObject parent, String method, String result, String media) {
			super(parent, method, result, media);
		}
	}

	//Songs
	class SongList extends BrowserObject {

		SongList(BrowserObject parent, String media) {
			super(parent, "AudioLibrary.GetSongs", "songs", media);
			
			try {
				if (parent.param.has("albumid"))
					param.put("albumid", parent.param.get("albumid"));
				if (parent.param.has("genreid"))
					param.put("genreid", parent.param.get("genreid"));
				if (parent.param.has("artistid"))
					param.put("artistid", parent.param.get("artistid"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	//Albums
	class AlbumList extends BrowserObject {

		public AlbumList(BrowserObject parent, String media) {
			super(parent, "AudioLibrary.GetAlbums", "albums", media);
			idField = "albumid";
			subObject = SongList.class;
			
			try {
				if (parent.param.has("genreid"))
					param.put("genreid", parent.param.get("genreid"));
				if (parent.param.has("artistid"))
					param.put("artistid", parent.param.get("artistid"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
				
		}	
	}
	
	// Artists
	class ArtistList extends BrowserObject {

		public ArtistList(BrowserObject parent, String media) {
			super(parent, "AudioLibrary.GetArtists", "artists", media);
			idField = "artistid";
			subObject = AlbumList.class;
			
			try {
				if (parent.param.has("genreid"))
					param.put("genreid", parent.param.get("genreid"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	// Genres
	class GenreList extends BrowserObject {

		public GenreList(BrowserObject parent, String media) {
			super(parent, "AudioLibrary.GetGenres", "genres", media);
			idField = "genreid";
			subObject = ArtistList.class;
		}
		
	}
	
	// Files
	class SourceList extends BrowserObject {

		SourceList(BrowserObject parent, String media) {
			super(parent, "Files.GetSources", profile.apiVersion == 4? "sources":"shares", media);
			subObject = FileList.class;
			
			try {
					param.put("media", media);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	// Files
	class FileList extends BrowserObject {

		FileList(BrowserObject parent, String media) {
			super(parent, "Files.GetDirectory", null, media);
			subObject = FileList.class;
			
			try {
					param.put("media", media);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected JSONArray browse(boolean updateDirList) throws Exception {
			
			if (method == null) return null;
			
			param.put("directory", json.get("file"));
	
			JSONObject tmpResult = profile
					.getJsonClient()
					.callJSONObject(method, param);
	
			//Xbmc.debug("\n\n*********** "+tmpResult+"\n\n");
			
			JSONArray tmpArr;
			JSONArray list = new JSONArray();

			if (updateDirList)
				directoriesList.clear();
			
			// get directories
			if (tmpResult.has("directories")) try {
				tmpArr = tmpResult.getJSONArray("directories");
				for (int i = 0; i < tmpArr.length(); i++) {
					String label = tmpArr.getJSONObject(i).getString("label");
					
					if (updateDirList) directoriesList.add(label);
					
					list.put(tmpArr.get(i));
					put(label, tmpArr.getJSONObject(i));
				}
			} catch (Exception e) {	}
	
			// get files
			if (tmpResult.has("files")) try {
				tmpArr = tmpResult.getJSONArray("files");
				
				for (int i = 0; i < tmpArr.length(); i++) {
					String label = tmpArr.getJSONObject(i).getString("label");
					list.put(tmpArr.get(i));
					put(label, tmpArr.getJSONObject(i));
					
					JSONObject jo = tmpArr.getJSONObject(i);
					
					if (jo.has("filetype") && jo.getString("filetype").equals("directory")
							&& !(profile.apiVersion > 3 && jo.getString("type").equals("movie"))) {
						if( updateDirList)	directoriesList.add(label);
					}
					else 
						subCategories.get(label).method = null;
				}
			} catch (Exception e) {e.printStackTrace();}
	
			return list;
		}		
	}
	
	// Series
	class SerieList extends BrowserObject {

		SerieList(BrowserObject parent, String media) {
			super(parent, "VideoLibrary.GetTVShows", "tvshows", media);
			idField = "tvshowid";
			subObject = SeasonList.class;
		}
		
	}

	// Season
	class SeasonList extends BrowserObject {

		SeasonList(BrowserObject parent, String media) {
			super(parent, "VideoLibrary.GetSeasons", "seasons", media);
			idField = "season";
			subObject = EpisodeList.class;
			
			try {
				if (parent.param.has("tvshowid"))
					param.put("tvshowid", parent.param.get("tvshowid"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	// Epìsode
	class EpisodeList extends BrowserObject {

		EpisodeList(BrowserObject parent, String media) {
			super(parent, "VideoLibrary.GetEpisodes", "episodes", media);
			
			try {
				if (parent.param.has("tvshowid"))
					param.put("tvshowid", parent.param.get("tvshowid"));
				if (parent.param.has("season"))
					param.put("season", parent.param.get("season"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public XbmcBrowser(Xbmc p) {
		super(p);
		profile = p;
			
		homeBrowserObject = new SimpleList(null, null, null, null);

		homeBrowserObject.subCategories.put("Movie Library", 
				new SimpleList(homeBrowserObject, "VideoLibrary.GetMovies", "movies", "video"));
		homeBrowserObject.subCategories.put("Series", 
				new SerieList(homeBrowserObject, "video"));
			
		BrowserObject music = new SimpleList(homeBrowserObject, null, null, "music");
			music.subCategories.put("Albums", new AlbumList(music, "music"));	
			music.subCategories.put("Artists", new ArtistList(music, "music"));		
			music.subCategories.put("Genre", new GenreList(music, "music"));
			
		homeBrowserObject.subCategories.put("Music Library", music);
		homeBrowserObject.subCategories.put("All Video Files", 
				new SourceList(homeBrowserObject, "video")); 
		homeBrowserObject.subCategories.put("All Music Files", 
				new SourceList(homeBrowserObject, "music")); 
		homeBrowserObject.subCategories.put("Pictures",
				new SourceList(homeBrowserObject, "pictures")); 

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
	

	
	@Override
	public List<AdapterItem> browse(String item) throws RemoteException {
		
		ArrayList<AdapterItem> list = new ArrayList<AdapterItem>();

		//Xbmc.debug("BROWSING: "+item+" on server "+profile.getValue(Xbmc.SERVER));
		
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
				JSONArray json = currentBrowserObject.browse(true);
					
				//currentObjects = new HashMap<String, JSONObject>();
				for (int i = 0; i < json.length();i++){
					//Xbmc.debug("fields: "+json.getJSONObject(i).names());
					list.add(new AdapterItem(-1, json.getJSONObject(i).getString("label"),
		  				null, null, null));
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return list;
	}

	
	@Override
	public List<AdapterItem> browseBack() throws RemoteException {
		return browse(BACK_STR);
	}

	
	@Override
	public Bitmap getCover(String item) throws RemoteException {
		try {
			//Xbmc.debug("searching cover for "+item);
			if (!currentBrowserObject.subCategories.containsKey(item) 
					||currentBrowserObject.subCategories.get(item).json == null
					||currentBrowserObject.method.equals("Files.GetDirectory")) return null;
			
			
			String tbnPath = null;
			if (currentBrowserObject.subCategories.get(item).json.has("thumbnail"))
				tbnPath = currentBrowserObject.subCategories.get(item).json.getString("thumbnail");
			else if (currentBrowserObject.subCategories.get(item).json.has("fanart"))
				tbnPath = currentBrowserObject.subCategories.get(item).json.getString("fanart");
			
			
			if (tbnPath == null || tbnPath.equals("")) return null;
			
			JSONObject tbn;
			if (profile.apiVersion == 2)
				tbn = profile.getJsonClient().callJSONObject("Files.Download", tbnPath);
				
			else if (profile.apiVersion == 3)
				tbn = profile.getJsonClient().callJSONObject("Files.Download", 
						new JSONObject().put("path", tbnPath));
			
			else 
				tbn = profile.getJsonClient().callJSONObject("Files.PrepareDownload", 
						new JSONObject().put("path", tbnPath));
						
			String tbnUrl;
			
			if (profile.apiVersion < 4)
				tbnUrl = "http://"+profile.getValue(Xbmc.SERVER)+":"+profile.getValue(Xbmc.PORT)+"/"
							+URLEncoder.encode(tbn.getString("path"), "UTF-8");
			else
				tbnUrl = "http://"+profile.getValue(Xbmc.SERVER)+":"+profile.getValue(Xbmc.PORT)+"/"
						+tbn.getJSONObject("details").getString("path");
			
			return downloadBitmap(tbnUrl);

		} catch (Exception e) {
			e.printStackTrace();
		}

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
