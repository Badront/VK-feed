package com.vk.sdk.api.methods;

import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.model.VKApiNews;

/**
 * User: Histler
 * Date: 29.05.14
 */
public class VKApiNewsfeed extends VKApiBase {
    public VKRequest get(VKParameters params){
        if(params!=null){
            params.put("filters","post");
        }
        return prepareRequest("get",params, VKRequest.HttpMethod.GET, VKApiNews.class );
    }
}
