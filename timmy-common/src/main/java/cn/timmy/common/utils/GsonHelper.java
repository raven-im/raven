package cn.timmy.common.utils;

import com.google.gson.Gson;

/**
 * Author zxx
 * Description 
 * Date Created on 2018/6/4
 */
public class GsonHelper {

    private static Gson gson = null;

    public static Gson getGson() {
        if (null == gson) {
            gson = new Gson();
        }
        return gson;
    }


}
