package com.ocunapse.aplicondo.guard.api;


import android.util.Log;

public class LoginRequest extends RequestBase {

    private static String prefix = "/login";
    LoginResult res;
    String usrname;
    String pswd;

    public LoginRequest(String username, String password, LoginResult res) {
        this.usrname = username;
        this.pswd = password;
        this.res = res;
    }

     class LoginReq{
        String userId    = usrname;
        String password  = pswd;
    }


    public static class Sites { }

    public static class Token {
        public String accessToken;
        public String refreshToken;
    }

    public static class User {
        public int userId;
        public String fullName;
        public int siteId;
    }

    public static class LoginRes{
        public Boolean success;
        public APIError error;
        public Token tokens;
        public User user;
        public Sites[] sites;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(s.length() < 1) {
            LoginRes rs = new LoginRes();
            rs.success = false;
            rs.error = ServerLost;
            res.get(rs);
        }
        else {
            try {
//                Log.i("get resp", s);
                LoginRes rs = g.fromJson(s, LoginRes.class);
                if(rs.success){
                    application.setSite(rs.user.siteId);
                }
                res.get(rs);
            }catch (Exception e){
                Log.e("API error", e.getMessage());
                LoginRes rs = new LoginRes();
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
        String data = g.toJson(new LoginReq());
        return Request(data, server+ prefix, CallType.POST);
    }

}
