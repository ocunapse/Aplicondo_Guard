package com.ocunapse.aplicondo.guard.api;

import android.util.Log;

import java.util.Locale;

public class VisitorListRequest extends RequestBase {
    private String prefix;
    VisitorListResult res;
    int siteId;

    public VisitorListRequest(String date,VisitorListResult res) {
        this.siteId = application.getSite();
        this.res = res;
        if(date == null) {
            prefix = String.format(Locale.ENGLISH,"/visitor/site/%d",siteId);
        } else {
            prefix = String.format(Locale.ENGLISH,"/visitor/site/%d?d=%s",siteId,date);
        }
    }

    public static class Profile {
        public int id;
        public String full_name;
        public String identification;
        public String email;
        public String phone_number;
        public String gender;
        public String photo_url;
    }

    public static class Unit {
        public int id ;
        public String unit_label;
        public int owner_id;
        public int site_id;
        public String block;
        public String floor;
        public int level;
        public int unit_type_id;
        public String site_unit;
        public boolean subletting_allowed;

        public Owner owners;
        public Tenant[] tenants;
    }

    public class Owner{
        public int id;
        public int profile_id;
        public Profile profile;
        public Family[] family;
    }
    public class Tenant{
        public int id;
        public int profile_id;
        public boolean is_master;
        public int unit_id;
        public Family[] family;
        public Profile profile;
    }

    public static class Family {
        public int id;
        public int profile_id;
        public int unit_id;
        public int ownersId;
        public int tenantsId;
        public Profile profile;
    }


    public static class VisitorListRes{
        public Boolean success;
        public APIError error;
        public VisitorCheckInRequest.Visitor[] visitors;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(s.length() < 1) {
            VisitorListRes rs = new VisitorListRes();
            rs.success = false;
            rs.error = ServerLost;
            res.get(rs);
        }
        else {
            try {
                Log.i("get resp", s);
                VisitorListRes rs = g.fromJson(s, VisitorListRes.class);
                res.get(rs);
            }catch (Exception e){
                VisitorListRes rs = new VisitorListRes();
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
