package com.coversal.plugin.xbmc;

import java.util.HashMap;
import java.util.List;

import android.os.RemoteException;

import com.coversal.plugin.json.JSONRPCException;
import com.coversal.ucl.plugin.Controller;

public class XbmcController extends Controller {

	
	Xbmc profile;
	HashMap<String, Object[]> params;
	
	public XbmcController(Xbmc xbmc) {
	
		profile = xbmc;
		params = new HashMap<String, Object[]>();
		
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
		defineCommand(BACK, "Input.Back", false);
		defineCommand(OK, "Input.Select", false);
		defineCommand(HOME, "Input.Home", false);
		defineCommand(UP, "Input.Up", false);
		defineCommand(DOWN, "Input.Down", false);
		defineCommand(LEFT, "Input.Left", false);
		defineCommand(RIGHT, "Input.Right", false);
		
		defineKey(CUSTOM1, BACK, false, "Back");
		defineKey(CUSTOM2, HOME, false, "Home");
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
	public boolean execute(String action) throws RemoteException {
		try {
			if (!params.containsKey(action))
				profile.getJsonClient().call(getCommand(action));
			
			else if (action.equals(VOL_UP) || action.equals(VOL_DOWN)){
					profile.getJsonClient().call(getCommand(action), 
							getVolume()+((Integer)params.get(action)[0]));
				}
			
			//else...
			
		} catch (JSONRPCException e) {
			e.printStackTrace();
		}
		return false;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isPlaying() throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onItemSelected(String arg0, String arg1) throws RemoteException {
		// TODO Auto-generated method stub

	}

}
