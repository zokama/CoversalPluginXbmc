package com.coversal.plugin.xbmc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
	HashMap<String, Object[]> params;
	
	public XbmcController(Xbmc xbmc) {
		super(xbmc);
		
		profile = xbmc;
		params = new HashMap<String, Object[]>();
		
		// version specific commands defined at runtime
		// defined here as empty for automatic keymap
		defineCommand(BACK, "", false);
		defineCommand(OK, "", false);
		defineCommand(HOME, "", false);
		defineCommand(UP, "", false);
		defineCommand(DOWN, "", false);
		defineCommand(LEFT, "", false);
		defineCommand(RIGHT, "", false);
		
		// common commands to Dharma and Eden
		defineCommand(START_PLAY, "XBMC.Play", false);
		defineCommand(PLAY_PAUSE, "VideoPlayer.PlayPause", false);
		defineCommand(STOP, "VideoPlayer.Stop", false);
		defineCommand(REWIND, "VideoPlayer.SmallSkipBackward", false);
		defineCommand(FORWARD, "VideoPlayer.SmallSkipForward", false);
		defineCommand(VOL_UP, "XBMC.SetVolume", false);
		defineParam(VOL_UP, +10);
		defineCommand(VOL_DOWN, "XBMC.SetVolume", false);
		defineParam(VOL_DOWN, -10);
		defineCommand(NEXT, "VideoPlayer.SkipNext", false);
		defineCommand(PREVIOUS, "VideoPlayer.SkipPrevious", false);
		
		defineCommand("Shutdown menu", "[http]0xF053)", false);
		defineCommand("Context menu", "[http]0xF043)", false);
		defineCommand("Media info", "[http]0xF049)", false);
		defineCommand(FULLSCREEN, "[http]0xF009", false);
		defineCommand("OSD", "[http]274", false);

		defineKey(CUSTOM1, BACK, false, "Back");
		defineKey(CUSTOM2, HOME, false, "Home");
		defineKey(CUSTOM3, "Mute", false, "Mute");
		defineKey(CUSTOM4, "Media info", false, "Info");
		defineKey("custom5", "Context menu", false, "Ctx Menu");
		defineKey("custom6", "OSD", false, "OSD");
		defineKey("custom7", "Shutdown menu", false, "Shutdown");
		defineKey("custom8", "Scan Library", false, "Scan");

	}
	
	void defineDharmaCommands() {
		
		defineCommand(BACK, "[http]0xF008", false);
		defineCommand(OK, "[http]0xF00d)", false);
		defineCommand(HOME, "[http]0xF01B", false);
		defineCommand(UP, "[http]270", false);
		defineCommand(DOWN, "[http]271", false);
		defineCommand(LEFT, "[http]272", false);
		defineCommand(RIGHT, "[http]273", false);
		defineCommand("Scan Library", "VideoLibrary.ScanForContent", false);
		defineCommand("Mute", "XBMC.ToggleMute", false);
		
	}

	void defineEdenCommands() {
		
		defineCommand(BACK, "Input.Back", false);
		defineCommand(OK, "Input.Select", false);
		defineCommand(HOME, "Input.Home", false);
		defineCommand(UP, "Input.Up", false);
		defineCommand(DOWN, "Input.Down", false);
		defineCommand(LEFT, "Input.Left", false);
		defineCommand(RIGHT, "Input.Right", false);
		defineCommand("Scan Library", "VideoLibrary.Scan", false);
		defineCommand("Mute", "XBMC.ToggleMute", false);
	}
	
	void defineAudioCommands() {
		
		defineCommand(PLAY_PAUSE, "AudioPlayer.PlayPause", false);
		defineCommand(STOP, "AudioPlayer.Stop", false);
		defineCommand(REWIND, "AudioPlayer.SmallSkipBackward", false);
		defineCommand(FORWARD, "AudioPlayer.SmallSkipForward", false);
		defineCommand(NEXT, "AudioPlayer.SkipNext", false);
		defineCommand(PREVIOUS, "AudioPlayer.SkipPrevious", false);		
	}
	
	void defineVideoCommands() {
		
		defineCommand(PLAY_PAUSE, "VideoPlayer.PlayPause", false);
		defineCommand(STOP, "VideoPlayer.Stop", false);
		defineCommand(REWIND, "VideoPlayer.SmallSkipBackward", false);
		defineCommand(FORWARD, "VideoPlayer.SmallSkipForward", false);
		defineCommand(NEXT, "VideoPlayer.SkipNext", false);
		defineCommand(PREVIOUS, "VideoPlayer.SkipPrevious", false);		
	}
	
	void definePictureCommands() {
		
		defineCommand(PLAY_PAUSE, " PicturePlayer.PlayPause", false);
		defineCommand(STOP, " PicturePlayer.Stop", false);
		defineCommand(REWIND, "PicturePlayer.ZoomOut", false);
		defineCommand(FORWARD, "PicturePlayer.ZoomIn", false);
		defineCommand(NEXT, "PicturePlayer.SkipNext", false);
		defineCommand(PREVIOUS, "PicturePlayer.SkipPrevious", false);
		
	}

	private void defineParam(String cmdName, Object... parameters) {
		params.put(cmdName, parameters);
	}
	
	private int getVolume() {
		
		try {
			return (int) profile.getJsonClient().callLong("XBMC.GetVolume");
		} catch (JSONRPCException e) {
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
							getVolume()+((Integer)params.get(action)[0])));
				else
					profile.getJsonClient().call(getCommand(action), 
							getVolume()+((Integer)params.get(action)[0]));
				}
			
			//else...
			
		} catch (JSONRPCException e) {
			//e.printStackTrace();
		}
			
		return true;
	}

	@Override
	public List<String> getContextMenuItems(int arg0) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getDeviceList() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLayoutName() throws RemoteException {
		return "rhys";
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
			JSONObject activePlayers = profile.getJsonClient().callJSONObject("Player.GetActivePlayers");
			
			if (activePlayers.getBoolean("audio"))
				defineAudioCommands();
			
			else if (activePlayers.getBoolean("picture"))
				definePictureCommands();
			
			else 
				defineVideoCommands();
			
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
		
		try {
        	InputStream is = profile.getJsonClient().getHttpClient()
        			.execute(post).getEntity().getContent();
        	if (is != null) {
        		// consume reponse otherwise we get warnings
        		String line;
        		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        		
        		while ((line = reader.readLine()) != null) {
        			for (String s: line.split("<li>")) {
	        			if (s.contains("Title")) {
	        				playingMedia = s.replaceAll("^.*Title:", "");
	        				break;
	        			} 
	        			else if (fileName == null && s.contains("Filename"))
	        				fileName = s.replaceAll("^Filename:.+/", "");
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
        	return playingMedia;
	}

	@Override
	public boolean isPlaying() throws RemoteException {
		if (getPlayingMedia() == null)
			return false;
		else 
			return true;
	}

	@Override
	public void onItemSelected(String action, String item) throws RemoteException {
		
		if (profile.currentObject != null)
			Executors.newSingleThreadExecutor().execute(new Runnable(){
			
			@Override
			public void run() {
				//Xbmc.debug("TRying to play "+item+ " file: "+profile.currentObject.getString("file"));

				try {
					 profile.getJsonClient().call(getCommand(START_PLAY),
							 new JSONObject().put("file", profile.currentObject.getString("file")));					 
					 
				} catch (JSONRPCException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}});

	}

	
}
