package com.ocunapse.aplicondo.guard.api;


public class LoginRequest extends RequestBase {

    LoginResult res;
    String usrname;
    String pswd;
    int pinn;

    public LoginRequest(String username, String password, LoginResult res) {
        this.usrname = username;
        this.pswd = password;
        this.res = res;
    }

    class LoginReq{
        String action   = Action;
        String uname    = usrname;
        String pwd      = pswd;
        int pin         = pinn;
    }

    public class Login {
        public String id;
        public String name;
        public String username;

    }

    public static class LoginRes{
        public APIerror error;
        public Boolean success;
        public Login[] data;
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
                LoginRes rs = g.fromJson(s, LoginRes.class);
                //if(rs.success)
                    //ArenaApp.setUserLogin(rs.result);
                res.get(rs);
            }catch (Exception e){
                LoginRes rs = new LoginRes();
                rs.success = false;
                rs.error.code = 888;
                rs.error.msg = s;
                res.get(rs);
            }
        }
    }


    @Override
    protected String doInBackground(Void... voids) {
        String data = g.toJson(new LoginReq());
        return Request(data, server, CallType.POST);
    }

}
