package com.ocunapse.aplicondo.guard.api;

import static com.ocunapse.aplicondo.guard.util.StringUtil.md5;

import android.util.Log;

import com.ocunapse.aplicondo.guard.GuardApp;

import java.util.Locale;
import java.util.Objects;

public class ChangePasswordRequest extends RequestBase {

    private final String prefix;

    GuardApp app = new GuardApp();
    ChangePasswordResult res;
    String oldPassword;
    String newPassword;

    public ChangePasswordRequest(String oldPassword, String newPassword, ChangePasswordResult res){
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.res = res;
        this.prefix = String.format(Locale.ENGLISH,"/auth/%d/changePassword", app.getDecodedToken().id);
    }


    public class ChangePasswordReq {

        public String currentPassword = md5(oldPassword).toLowerCase();
        public String password = md5(newPassword).toLowerCase();
    }

    public static class ChangePasswordRes{
        public Boolean success;
        public APIError error;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(s.length() < 1) {
            ChangePasswordRequest.ChangePasswordRes rs = new ChangePasswordRequest.ChangePasswordRes();
            rs.success = false;
            rs.error = ServerLost;
            res.get(rs);
        }
        else {
            try {
                ChangePasswordRequest.ChangePasswordRes rs = g.fromJson(s, ChangePasswordRequest.ChangePasswordRes.class);
                res.get(rs);
            }catch (Exception e){
                Log.e("API error", Objects.requireNonNull(e.getMessage()));
                ChangePasswordRequest.ChangePasswordRes rs = new ChangePasswordRequest.ChangePasswordRes();
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
        String data = g.toJson(new ChangePasswordRequest.ChangePasswordReq());
        String url = server + prefix;
        LOG("--chpwd",url);
        LOG("--chpwd",data);
        return Request(data, url , CallType.POST);
    }
}
