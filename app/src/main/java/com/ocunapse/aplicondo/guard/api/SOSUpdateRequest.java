package com.ocunapse.aplicondo.guard.api;

import android.util.Log;

import java.util.Locale;
import java.util.Objects;

public class SOSUpdateRequest extends RequestBase {

    private String prefix;

    SOSUpdateResult res;

    int report_id;

    public SOSUpdateRequest(int id, int reportId, SOSUpdateResult res){
        this.report_id = reportId;
        this.prefix = String.format(Locale.ENGLISH,"/guard/sos/%d", id);
        this.res = res;
    }


    public class SOSUpdateReq {
        int reported_id = report_id;
    }

    public static class SOSUpdateRes{
        public Boolean success;
        public APIError error;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.e("-sos-", "onPostExecute: "+s );
        if(s.length() < 1) {
            SOSUpdateRequest.SOSUpdateRes rs = new SOSUpdateRequest.SOSUpdateRes();
            rs.success = false;
            rs.error = ServerLost;
            res.get(rs);
        }
        else {
            try {
                SOSUpdateRequest.SOSUpdateRes rs = g.fromJson(s, SOSUpdateRequest.SOSUpdateRes.class);
                res.get(rs);
            }catch (Exception e){
                Log.e("API error", Objects.requireNonNull(e.getMessage()));
                SOSUpdateRequest.SOSUpdateRes rs = new SOSUpdateRequest.SOSUpdateRes();
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
        String data = g.toJson(new SOSUpdateRequest.SOSUpdateReq());
        String url = server + prefix;
        return Request(data, url , CallType.PUT);
    }
}
