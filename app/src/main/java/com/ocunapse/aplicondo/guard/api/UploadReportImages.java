package com.ocunapse.aplicondo.guard.api;

import android.util.Log;

import java.io.File;
import java.util.Locale;

public class UploadReportImages extends RequestBase {
    private final String prefix ;
    UploadReportImageResult res;
    File[] file;

    public UploadReportImages( File[] file, UploadReportImageResult res) {
        this.res = res;
        this.file = file;
        this.prefix = String.format(Locale.ENGLISH,"/guards/%d/uploadAttachment", application.getSite());
    }

    public static class ImagesData{
        public String[] url;
    }

    public static class UploadReportImgRes{
        public Boolean success;
        public APIError error;
        public ImagesData data;
    }


    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        LOG("walkin",s);
        if(s.length() < 1) {
            UploadReportImgRes rs = new UploadReportImgRes();
            rs.success = false;
            rs.error = ServerLost;
            res.get(rs);
        }
        else {
            try {
                UploadReportImgRes rs = g.fromJson(s, UploadReportImgRes.class);
                res.get(rs);
            }catch (Exception e){
                Log.e("API error", e.getMessage());
                UploadReportImgRes rs = new UploadReportImgRes();
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
        return RequestMultipart(file, url, CallType.POST);
    }

}
