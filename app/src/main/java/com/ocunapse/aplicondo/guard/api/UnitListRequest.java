package com.ocunapse.aplicondo.guard.api;

import android.util.Log;

public class UnitListRequest extends RequestBase {
    private String prefix;
    UnitListResult res;
    int siteId;

    public UnitListRequest( UnitListResult res) {
        this.siteId = application.getSite();
        this.res = res;
        prefix = String.format("/units/%d",siteId );
    }

    public static class Unit {
        public int id ;
        public String unit_label;
        public int owner_id;
    }


    public static class UnitListRes{
        public Boolean success;
        public APIError error;
        public Unit[] data;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(s.length() < 1) {
            UnitListRes rs = new UnitListRes();
            rs.success = false;
            rs.error = ServerLost;
            res.get(rs);
        }
        else {
            try {
                Log.i("get resp", s);
                UnitListRes rs = g.fromJson(s, UnitListRes.class);
                res.get(rs);
            }catch (Exception e){
                UnitListRes rs = new UnitListRes();
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
