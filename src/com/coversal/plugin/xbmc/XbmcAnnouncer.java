package com.coversal.plugin.xbmc;

import com.coversal.ucl.plugin.ProfileAnnouncer;

public class XbmcAnnouncer extends ProfileAnnouncer {

	public XbmcAnnouncer() {
		defineProfile("XBMC", Xbmc.class);
	}
}
