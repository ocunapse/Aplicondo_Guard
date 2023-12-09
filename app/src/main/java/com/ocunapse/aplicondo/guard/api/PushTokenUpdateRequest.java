package com.ocunapse.aplicondo.guard.api;

import android.util.Log;

import java.util.Locale;
import java.util.Objects;

public class PushTokenUpdateRequest extends RequestBase {

    private final String prefix = "/devices/updatePushToken";

    PushTokenUpdateResult res;

    String ptoken;
    boolean toReplace;

    public PushTokenUpdateRequest(String token, boolean replace, PushTokenUpdateResult res){
        this.ptoken = token;
        this.toReplace = replace;
        this.res = res;
    }


    public class PushTokenUpdateReq {
        String token = ptoken;
        boolean replace = toReplace;
        String type = "Android";
        int user_id = application.getDecodedToken().id;
        String old_token = replace ? application.getPushToken() : null;
    }

    public static class PushTokenUpdateRes{
        public Boolean success;
        public APIError error;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.e("-token-", "onPostExecute: "+s );
        if(s.length() < 1) {
            PushTokenUpdateRequest.PushTokenUpdateRes rs = new PushTokenUpdateRequest.PushTokenUpdateRes();
            rs.success = false;
            rs.error = ServerLost;
            res.get(rs);
        }
        else {
            try {
                PushTokenUpdateRequest.PushTokenUpdateRes rs = g.fromJson(s, PushTokenUpdateRequest.PushTokenUpdateRes.class);
                res.get(rs);
            }catch (Exception e){
                Log.e("API error", Objects.requireNonNull(e.getMessage()));
                PushTokenUpdateRequest.PushTokenUpdateRes rs = new PushTokenUpdateRequest.PushTokenUpdateRes();
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
        String data = g.toJson(new PushTokenUpdateRequest.PushTokenUpdateReq());
        String url = server + prefix;
        return Request(data, url , CallType.POST);
    }
}
