package com.ocunapse.aplicondo.guard.api;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.util.Locale;

public class UploadImage extends RequestBase {
    private final String prefix ;
    UploadImageResult res;
    File file;

    public UploadImage(int unit_id, int profile_id, File file, UploadImageResult res) {
        this.res = res;
        this.file = file;
        this.prefix = String.format(Locale.ENGLISH,"/visitor/%d/%d/uploadImage",unit_id, profile_id );
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
        String url = server + prefix;
        File[] files = new File[]{file};
        return RequestMultipart(files, url, CallType.POST);
    }

}
