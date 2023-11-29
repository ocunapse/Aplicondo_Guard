package com.ocunapse.aplicondo.guard.api;


import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

public class WalkInVisitorRequest extends RequestBase {

    private final static String prefix = "/visitor/";

    int unit_id;
    int profile_id;

    WalkInResult res;
    String Name;
    String mobNum;
    String visitDate;
    String endDate;
    String plateNum;
    String transports;
    String image_url;

    public enum Transport {
        WALK_IN,
        VEHICLE
    }

    public WalkInVisitorRequest(int unit_id, int profile_id, String name, String mobile_number,String vehicle_registration,Transport transport,long visit_date,String image,  WalkInResult res) {
        TimeZone malaysianTimeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur");
        SimpleDateFormat start = new SimpleDateFormat("yyyy/MM/dd 00:00:01 z");
        start.setTimeZone(malaysianTimeZone);
        SimpleDateFormat end = new SimpleDateFormat("yyyy/MM/dd 23:59:00 z");
        end.setTimeZone(malaysianTimeZone);
        this.unit_id = unit_id;
        this.profile_id = profile_id;
        this.Name = name;
        this.mobNum = mobile_number;
        this.plateNum = transport == Transport.WALK_IN ? vehicle_registration : null;
        this.transports = transport.toString();
        this.visitDate = start.format(new Date(visit_date));
        this.endDate = end.format(new Date(visit_date));
        this.image_url = image;
        Log.d("walkin",visit_date + " --- " + this.visitDate +" ---- " +endDate);
        this.res = res;
    }

      class WalkInReq{
        String name = Name;
        String mobile_number = mobNum;
        String visit_date = visitDate;
        String end_date = endDate;
        String vehicle_registration = plateNum;
        String transport = transports;
        String category="VISITOR";
        String type="ONE_TIME";
        String imageUrl = image_url;
    }


    public static class WalkInRes{
        public Boolean success;
        public APIError error;
        public VisitorCheckInRequest.Visitor data;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.d("walkin", s);
        if(s.length() < 1) {
            WalkInRes rs = new WalkInRes();
            rs.success = false;
            rs.error = ServerLost;
            res.get(rs);
        }
        else {
            try {
                WalkInRes rs = g.fromJson(s, WalkInRes.class);
                res.get(rs);
            }catch (Exception e){
                Log.e("API error", Objects.requireNonNull(e.getMessage()));
                WalkInRes rs = new WalkInRes();
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
        String data = g.toJson(new WalkInReq());
        Log.d("walkin", data);
        String url = server + prefix + unit_id + "/" + profile_id + "/createVisit";
        Log.d("walkin", url);
        return Request(data, url , CallType.POST);
    }

}
