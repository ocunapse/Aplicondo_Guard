package com.ocunapse.aplicondo.guard.api;

import android.util.Log;

import java.util.Locale;
import java.util.Objects;

public class VisitUpdateRequest extends RequestBase {

    private final String prefix;

    VisitUpdateResult res;

    public enum Status {
        ARRIVED,
        GUARD_REJECTED,

    }

    Status sts;

    public VisitUpdateRequest(int visit_id, Status status, VisitUpdateResult res){
        sts = status;
        this.res = res;
        this.prefix = String.format(Locale.ENGLISH,"/visitor/visit/%d",visit_id );
    }


    public class VisitUpdateReq {
        String status = String.valueOf(sts);
    }

    public static class VisitUpdateRes{
        public Boolean success;
        public APIError error;
        public VisitorCheckInRequest.Visitor data;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(s.length() < 1) {
            VisitUpdateRequest.VisitUpdateRes rs = new VisitUpdateRequest.VisitUpdateRes();
            rs.success = false;
            rs.error = ServerLost;
            res.get(rs);
        }
        else {
            try {
                VisitUpdateRequest.VisitUpdateRes rs = g.fromJson(s, VisitUpdateRequest.VisitUpdateRes.class);
                res.get(rs);
            }catch (Exception e){
                Log.e("API error", Objects.requireNonNull(e.getMessage()));
                VisitUpdateRequest.VisitUpdateRes rs = new VisitUpdateRequest.VisitUpdateRes();
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
        String data = g.toJson(new VisitUpdateRequest.VisitUpdateReq());
        LOG("walkin", data);
        String url = server + prefix;
        LOG("walkin", url);
        return Request(data, url , CallType.PUT);
    }
}
