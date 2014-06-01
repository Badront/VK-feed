package com.vk.sdk.api.model;

import android.os.Parcel;
import android.os.Parcelable;
import org.json.JSONObject;

/**
 * User: Histler
 * Date: 29.05.14
 */
public class VKApiNews extends VKApiModel implements Parcelable {
    /*public String new_from;*/
	public String next_from;
    public VKPostArray items;
	public VKUsersArray profiles;
	public VKApiCommunityArray groups;
    @Override
    public int describeContents() {
        return 0;
    }
	@SuppressWarnings("unchecked")
    public VKApiNews parse(JSONObject source) {
        if (source.has("response")) {
            JSONObject response=source.optJSONObject("response");
            //new_from = response.optString("new_from");
			next_from=response.optString("next_from");
			if(response.has("profiles")){
				profiles=new VKUsersArray();
				profiles.fill(response.optJSONArray("profiles"),new VKList.ReflectParser(VKApiUserFull.class));
			}
			if(response.has("groups")){
				groups=new VKApiCommunityArray();
				groups.fill(response.optJSONArray("groups"),new VKList.ReflectParser(VKApiCommunityFull.class));
			}
        }
        items = new VKPostArray();
        items.fill(source,VKApiPost.class);
        return this;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //dest.writeString(this.new_from);
		dest.writeString(this.next_from);
        dest.writeParcelable(items,flags);
        dest.writeParcelable(profiles,flags);
        dest.writeParcelable(groups,flags);

    }
}
