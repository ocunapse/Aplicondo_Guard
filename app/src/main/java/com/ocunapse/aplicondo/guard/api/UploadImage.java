package com.ocunapse.aplicondo.guard.api;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;

public class UploadImage extends RequestBase {
    private final static String prefix = "/visitor/";
    UploadImageResult res;

    int unit_id;
    int profile_id;
    File file;

    public UploadImage(int unit_id, int profile_id, File file, UploadImageResult res) {
        this.res = res;
        this.unit_id = unit_id;
        this.profile_id = profile_id;
        this.file = file;
    }

    public class ImageData{
        public String url;
    }

    public static class UploadImageRes{
        public Boolean success;
        public APIError error;
        public ImageData data;
    }


    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        LOG("walkin",s);
        if(s.length() < 1) {
            UploadImageRes rs = new UploadImageRes();
            rs.success = false;
            rs.error = ServerLost;
            res.get(rs);
        }
        else {
            try {
                UploadImageRes rs = g.fromJson(s, UploadImageRes.class);
                res.get(rs);
            }catch (Exception e){
                Log.e("API error", e.getMessage());
                UploadImageRes rs = new UploadImageRes();
                rs.success = false;
                rs.error = new APIError();
                rs.error.code = 888;
                rs.error.message = "Parsing error";
                rs.error.cause = e.getMessage();
                res.get(rs);
            }
        }
    }

    @Override
    protected String doInBackground(Void... voids) {
        String url = server + prefix + unit_id + "/" + profile_id + "/uploadImage";
        return RequestMultipart(file, url, CallType.POST);
    }

}
