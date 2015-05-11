package edu.mit.mitmobile2.dining.model;

import java.util.HashSet;

import com.google.gson.annotations.SerializedName;


public class MITDiningDining {
    @SerializedName("announcements_html")
    protected String announcementsHTML;
    protected String url;
    protected HashSet<MITDiningLinks> links;
	protected MITDiningVenues venues;

	public String getAnnouncementsHTML() {
		return announcementsHTML;
	}

	public String getUrl() {
		return url;
	}

	public HashSet<MITDiningLinks> getLinks() {
		return links;
	}

	public MITDiningVenues getVenues() {
		return venues;
	}

	@Override
	public String toString() {
		return "MITDiningDining{" +
			"announcementsHTML='" + announcementsHTML + '\'' +
			", url='" + url + '\'' +
			", links=" + links +
			", venues=" + venues +
			'}';
	}
}