package com.ocunapse.aplicondo.guard.api;


import android.util.Log;

public class VisitorCheckInRequest extends RequestBase {

    private final static String prefix = "/visitor/visit/";
    VisitorResult res;
    int visitorId;

    public VisitorCheckInRequest(int visitorId, VisitorResult res) {
        this.res = res;
        this.visitorId = visitorId;
    }

    public static class Profile {
        public String full_name;
        public String identification;
        public String email;
        public String phone_number;
        public String gender;
        public String photo_url;
        public String race;
    }

    public static class Visitor {
        public int id;
        public int unit_id;
        public int profile_id;
        public String name;
        public String mobile_number;
        public String visit_date;
        public String vehicle_registration;
        public String category;
        public String transport;
        public String type;
        public String dropoff_location;
        public String schedule;
        public String weekdays;
        public String status;
        public Profile profile;
        public UnitListRequest.Unit unit;
    }

    public static class VisitorRes{
        public Boolean success;
        public APIError error;
        public Visitor data;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(s.length() < 1) {
            VisitorRes rs = new VisitorRes();
            rs.success = false;
            rs.error = ServerLost;
            res.get(rs);
        }
        else {
            try {
//                Log.i("get resp", s);
                VisitorRes rs = g.fromJson(s, VisitorRes.class);
                res.get(rs);
            }catch (Exception e){
                Log.e("API error", e.getMessage());
                VisitorRes rs = new VisitorRes();
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
        return Request(null, server + prefix + visitorId, CallType.GET);
    }

}
