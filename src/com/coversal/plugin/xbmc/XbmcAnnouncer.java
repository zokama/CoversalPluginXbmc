package com.coversal.plugin.xbmc;

import com.coversal.ucl.plugin.PluginAnnouncer;

public class XbmcAnnouncer extends PluginAnnouncer {

	public XbmcAnnouncer() {
		defineProfile("XBMC", Xbmc.class);
	}

}
