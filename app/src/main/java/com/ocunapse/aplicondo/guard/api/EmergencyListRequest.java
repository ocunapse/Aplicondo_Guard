package com.ocunapse.aplicondo.guard.api;

import android.util.Log;

import java.util.Date;
import java.util.Locale;

public class EmergencyListRequest extends RequestBase {
    private String prefix;
    EmergencyListResult res;
    int siteId;

    public EmergencyListRequest( EmergencyListResult res) {
        this.siteId = application.getSite();
        this.res = res;
        prefix = String.format(Locale.ENGLISH,"/guards/%d/emergency",siteId);
    }

    public static class Emergency {
        public int id;
        public int unit_id;
        public int site_id;
        public int user_id;
        public boolean duplicate;
        public String reason;
        public Date timestamp;
        public Object reported_id;
        public UnitListRequest.Unit unit;
        public User user;
    }

    public static class User {
        public int id;
        public int profile_id;
        public VisitorListRequest.Profile profiles;
    }



    public static class EmergencyListRes{
        public boolean success;
        public APIError error;
        public Emergency[] data;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(s.length() < 1) {
            EmergencyListRes rs = new EmergencyListRes();
            rs.success = false;
            rs.error = ServerLost;
            res.get(rs);
        }
        else {
            try {
                Log.i("get resp", s);
                EmergencyListRes rs = g.fromJson(s, EmergencyListRes.class);
                res.get(rs);
            }catch (Exception e){
                EmergencyListRes rs = new EmergencyListRes();
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
        return Request(null, server+prefix, CallType.GET);
    }

}
