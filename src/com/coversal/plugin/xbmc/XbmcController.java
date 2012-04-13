package com.coversal.plugin.xbmc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.RemoteException;

import com.coversal.plugin.json.JSONRPCException;
import com.coversal.ucl.plugin.Controller;

public class XbmcController extends Controller {

	
	Xbmc profile;
	HashMap<String, Object> params;
	
	public XbmcController(Xbmc xbmc) {
		super(xbmc);
		
		profile = xbmc;
		params = new HashMap<String, Object>();
		
		try {
			initCommands(-1);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		defineKey(CUSTOM1, HOME, false, "Home");
		defineKey(CUSTOM2, "OSD", false, "OSD");
		defineKey(CUSTOM3, "Context menu", false, "Ctx Menu");
		defineKey(CUSTOM4, "Subtitles", false, "Subtitles");
		defineKey(CUSTOM5, "Mute", false, "Mute");
		defineKey(CUSTOM6, "Shutdown menu", false, "Shutdown");
		defineKey(CUSTOM7, "Scan Library", false, "Scan");
	}
	
	
	void zdefinePictureCommands() {
		
		defineCommand(PLAY_PAUSE, " PicturePlayer.PlayPause", false);
		defineCommand(STOP, " PicturePlayer.Stop", false);
		defineCommand(REWIND, "PicturePlayer.ZoomOut", false);
		defineCommand(FORWARD, "PicturePlayer.ZoomIn", false);
		defineCommand(NEXT, "PicturePlayer.SkipNext", false);
		defineCommand(PREVIOUS, "PicturePlayer.SkipPrevious", false);
	}
	
	void initCommands(int version) throws JSONException {
		defineCommand("Shutdown menu", "[http]0xF053)", false);
		defineCommand("Context menu", "[http]0xF043)", false);
		defineCommand("Subtitles", "[http]0xF04c)", false);
		defineCommand(INFO, "[http]0xF049)", false);
		defineCommand(FULLSCREEN, "[http]0xF009", false);
		defineCommand(OSD, "[http]274", false);
		defineParam(VOL_UP, +10);
		defineParam(VOL_DOWN, -10);
		
		switch (version) {
		case 2:
			defineCommand(START_PLAY, "XBMC.Play", false);
			defineCommand(BACK, "[http]0xF008", false);
			defineCommand(OK, "[http]0xF00d)", false);
			defineCommand(HOME, "[http]0xF01B", false);
			defineCommand(UP, "[http]270", false);
			defineCommand(DOWN, "[http]271", false);
			defineCommand(LEFT, "[http]272", false);
			defineCommand(RIGHT, "[http]273", false);
			defineCommand("Mute", "XBMC.ToggleMute", false);
			defineCommand(VOL_UP, "XBMC.SetVolume", false);
			defineCommand(VOL_DOWN, "XBMC.SetVolume", false);
			break;
		case 3:
			defineCommand(START_PLAY, "XBMC.Play", false);
			
			
			
			defineCommand(BACK, "Input.Back", false);
			defineCommand(OK, "Input.Select", false);
			defineCommand(HOME, "Input.Home", false);
			defineCommand(UP, "Input.Up", false);
			defineCommand(DOWN, "Input.Down", false);
			defineCommand(LEFT, "Input.Left", false);
			defineCommand(RIGHT, "Input.Right", false);
			defineCommand("Mute", "XBMC.ToggleMute", false);
			defineCommand(VOL_UP, "XBMC.SetVolume", false);
			defineCommand(VOL_DOWN, "XBMC.SetVolume", false);
			defineCommand(ADD_TO_PLAYLIST, "Playlist.Add", false);
			break;
		case 4:
			defineCommand(START_PLAY, "Player.Open", false);
			defineCommand(BACK, "Input.Back", false);
			defineCommand(OK, "Input.Select", false);
			defineCommand(HOME, "Input.Home", false);
			defineCommand(UP, "Input.Up", false);
			defineCommand(DOWN, "Input.Down", false);
			defineCommand(LEFT, "Input.Left", false);
			defineCommand(RIGHT, "Input.Right", false);
			defineCommand("Mute", "Application.SetMute", false);
			defineParam("Mute", new JSONObject().put("mute", "toggle"));
			defineCommand(VOL_UP, "Application.SetVolume", false);
			defineCommand(VOL_DOWN, "Application.SetVolume", false);
			defineCommand(ADD_TO_PLAYLIST, "Playlist.Add", false);
			
			defineCommand(PLAY_PAUSE, "Player.PlayPause", false);
			defineCommand(STOP, "Player.Stop", false);
			defineCommand(REWIND, "Player.SetSpeed", false);
			defineCommand(FORWARD, "Player.SetSpeed", false);
			defineCommand(NEXT, "Player.GoNext", false);
			defineCommand(PREVIOUS, "Player.GoPrevious", false);
			break;
		default:
			defineCommand(START_PLAY, "", false);
			defineCommand(BACK, "", false);
			defineCommand(OK, "", false);
			defineCommand(HOME, "", false);
			defineCommand(UP, "", false);
			defineCommand(DOWN, "", false);
			defineCommand(LEFT, "", false);
			defineCommand(RIGHT, "", false);
			defineCommand("Mute", "", false);
			defineCommand(VOL_UP, "", false);
			defineCommand(VOL_DOWN, "", false);
			defineCommand(ADD_TO_PLAYLIST, "", false);
			defineCommand(PLAY_PAUSE, "", false);
			defineCommand(STOP, "", false);
			defineCommand(REWIND, "", false);
			defineCommand(FORWARD, "", false);
			defineCommand(NEXT, "", false);
			defineCommand(PREVIOUS, "", false);
			defineCommand("Scan Library", "", false);
			break;
		}
	}
	
	private void defineCommands(String playerType, int playerId, int apiVersion) throws JSONException {
		switch (apiVersion) {
		case 2:
			//audio
			defineCommand(PLAY_PAUSE, playerType+"Player.PlayPause", false);
			defineCommand(STOP, playerType+"Player.Stop", false);
			defineCommand(REWIND, playerType+"Player.SmallSkipBackward", false);
			defineCommand(FORWARD, playerType+"Player.SmallSkipForward", false);
			defineCommand(NEXT, playerType+"Player.SkipNext", false);
			defineCommand(PREVIOUS, playerType+"Player.SkipPrevious", false);
			
			defineCommand("Scan Library", playerType+"Library.ScanForContent", false);
			defineCommand(ADD_TO_PLAYLIST, playerType+"Playlist.Add", false);			
			break;
			
		case 3:
			//audio
			defineCommand(PLAY_PAUSE, playerType+"Player.PlayPause", false);
			defineCommand(STOP, playerType+"Player.Stop", false);
			defineCommand(REWIND, playerType+"Player.SmallSkipBackward", false);
			defineCommand(FORWARD, playerType+"Player.SmallSkipForward", false);
			defineCommand(NEXT, playerType+"Player.SkipNext", false);
			defineCommand(PREVIOUS, playerType+"Player.SkipPrevious", false);
			
			defineCommand("Scan Library", playerType+"Library.Scan", false);
			break;
			
		case 4:
		default:
			//video + audio + pictures
			defineParam(PLAY_PAUSE, new JSONObject().put("playerid", playerId));
			defineParam(STOP, new JSONObject().put("playerid", playerId));
			defineParam(REWIND, new JSONObject().put("playerid", playerId).put("speed", "decrement"));
			defineParam(FORWARD, new JSONObject().put("playerid", playerId).put("speed", "increment"));
			defineParam(NEXT, new JSONObject().put("playerid", playerId));
			defineParam(PREVIOUS, new JSONObject().put("playerid", playerId));
			
			defineCommand("Scan Library", playerType+"Library.Scan", false);
			break;
		}
	}

	private void defineParam(String cmdName, Object parameter) {
		params.put(cmdName, parameter);
	}
	
	private int getVolume() {
		
		try {
			if (profile.apiVersion == 4)
				return profile.getJsonClient().callJSONObject(
						"Application.GetProperties",
						new JSONObject().put("properties", new JSONArray().put("volume")))
						.getInt("volume");
			else
				return profile.getJsonClient().callInt("XBMC.GetVolume");
		} catch (JSONRPCException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	@Override
	public boolean execute(final String action) throws RemoteException {

		// generate a thread in order to avoid delays
		if (getCommand(action).contains("[http]")) {
			String cmd = getCommand(action).replace("[http]", "");
			
			HttpPost post = new HttpPost(
					"http://"+profile.getValue(Xbmc.SERVER)+":"+profile.getValue(Xbmc.PORT)
					+"/xbmcCmds/xbmcHttp?command=SendKey("+cmd+")");
			post.addHeader("User-Agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows 2000)");
			
			//Xbmc.debug("Sening command"+post.getURI());

			try {

	        	InputStream is = profile.getJsonClient().getHttpClient().execute(post).getEntity().getContent();
	        	if (is != null) {
	        		// consume reponse otherwise we get warnings
	        		while(is.read() > 0);
	        		is.close();
	        	}
				
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		else try {
			if (!params.containsKey(action))
				profile.getJsonClient().call(getCommand(action));
			
			else if (action.equals(VOL_UP) || action.equals(VOL_DOWN)){
				if (profile.apiVersion>2)
					profile.getJsonClient().call(getCommand(action), new JSONArray().put(
							getVolume()+((Integer)params.get(action))));
				else
					profile.getJsonClient().call(getCommand(action), 
							getVolume()+((Integer)params.get(action)));
				}
			
			else
				profile.getJsonClient().call(getCommand(action), params.get(action));
			
		} catch (JSONRPCException e) {
			e.printStackTrace();
		}
			
		return true;
	}

	@Override
	public List<String> getContextMenuItems(int type) throws RemoteException {
		List<String> list = new ArrayList<String>();
		
//		if (((XbmcBrowser)profile.getBrowser()).currentBrowserObject.method != null) {
//			if (type == ITEM_TYPE_DIRECTORY)
//				list.add("Add folder to playlist");
//			else
//				list.add("Add to playlist");
//		}
		
		return list;
	}

	@Override
	public List<String> getDeviceList() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLayoutName() throws RemoteException {
		return "coversal1";
	}

	@Override
	public int getMediaDuration() throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMediaPosition() throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getPlayingMedia() throws RemoteException {
		
		// determinate player type (video, audio, picture)
		try {
			
			if (profile.apiVersion <4) {
				JSONObject activePlayers = profile.getJsonClient().callJSONObject("Player.GetActivePlayers");
				
				if (activePlayers.getBoolean("audio"))
					defineCommands("Audio", 0, profile.apiVersion);
				
				else if (activePlayers.getBoolean("picture"))
					defineCommands("Pictures", 0, profile.apiVersion);
				
				else 
					defineCommands("Video", 0, profile.apiVersion);
			}
			else {
				JSONArray activePlayers = profile.getJsonClient()
						.callJSONArray("Player.GetActivePlayers");
				
				
				
				if (activePlayers.length()>0) {
					profile.currentPlayerId = activePlayers.getJSONObject(0).getInt("playerid");
					
					for (int i=0; i<activePlayers.length(); i++)
						//Xbmc.debug("==>"+activePlayers.getJSONObject(i));
				
					if (activePlayers.getJSONObject(0).getString("type").equals("audio"))
						defineCommands("Audio", profile.currentPlayerId, profile.apiVersion);
					
					else if (activePlayers.getJSONObject(0).getString("type").equals("pictures"))
						defineCommands("Pictures", profile.currentPlayerId, profile.apiVersion);
					
					else 
						defineCommands("Video", profile.currentPlayerId, profile.apiVersion);
					
					
				}
			}
		} catch (JSONRPCException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		HttpPost post = new HttpPost(
				"http://"+profile.getValue(Xbmc.SERVER)+":"+profile.getValue(Xbmc.PORT)
				+"/xbmcCmds/xbmcHttp?command=getcurrentlyplaying");
		post.addHeader("User-Agent",
				"Mozilla/4.0 (compatible; MSIE 6.0; Windows 2000)");
		
		//Xbmc.debug("GETTING PLAYING MEDIA "+post.getURI());

		String playingMedia = null;
		String fileName = null;
		String artist = null;
		
		try {
        	InputStream is = profile.getJsonClient().getHttpClient()
        			.execute(post).getEntity().getContent();
        	if (is != null) {
        		// consume reponse otherwise we get warnings
        		String line;
        		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        		
        		profile.currentPlaylistPosition = -1;
        		
        		while ((line = reader.readLine()) != null) {
        			for (String s: line.split("<li>")) {
	        			if (playingMedia == null && s.matches("^Title:.*")) {
	        				playingMedia = s.replaceAll("^Title:", "");
	        			} 
	        			else if (fileName == null && s.contains("Filename"))
	        				fileName = s.replaceAll("^Filename:.+[/\\\\]", "");
	        			
	        			if (artist == null && s.matches("^Artist:.*"))
	        				artist = s.replaceAll("^Artist:", "");
	        			
	        			if (profile.currentPlaylistPosition == -1 && s.matches("^(Song|Video)No:\\d+"))
	        				profile.currentPlaylistPosition = 
	        						Integer.valueOf(s.replaceAll("^(Song|Video)No:", ""));
        			}
        		}

        		is.close();
        		reader.close();
        	}
		} catch (IllegalStateException e) {
		} catch (ClientProtocolException e) {
		} catch (IOException e) {
		}

        if (playingMedia == null && fileName != null && !fileName.contains("Nothing Playing"))
        	return fileName;
        else
        	return (artist==null?"":artist+" - ")+(playingMedia==null?"":playingMedia);
	}

	@Override
	public boolean isPlaying() throws RemoteException {
		String playing = getPlayingMedia();
		if (playing == null || playing.equals(""))
			return false;
		else 
			return true;
	}

	@Override
	public void onItemSelected(String action, String item) throws RemoteException {
	
		if (action == null) {
			if (profile.currentObject != null)
				Executors.newSingleThreadExecutor().execute(new Runnable(){
				
				@Override
				public void run() {
					//Xbmc.debug("TRying to play "+item+ " file: "+profile.currentObject.getString("file"));
	
					try {
						if (profile.apiVersion < 4)
							profile.getJsonClient().call(getCommand(START_PLAY),
									new JSONObject().put("file", profile.currentObject.getString("file")));					 
						else
							profile.getJsonClient().call(getCommand(START_PLAY),
									new JSONObject().put("item", new JSONObject().put("file", profile.currentObject.getString("file"))));
						 
					} catch (JSONRPCException e) {
						e.printStackTrace();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}});
		}
		else {
				
		}
	}

	
}
