package com.ocunapse.aplicondo.guard.api;

import static com.ocunapse.aplicondo.guard.util.StringUtil.md5;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ocunapse.aplicondo.guard.GuardApp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public class RequestBase extends AsyncTask<Void,Void, String> implements Serializable {

    public static GuardApp application = new GuardApp();
    public static Gson g = new GsonBuilder().create();
    public static boolean Debug = true;
    public static boolean Live = true;




    public static String server = RequestBase.Live ?"https://aplicondo.ocunapse.com/v1/api" : "https://aplicondo.ocunapse.com/v1/api" ;


    public String Action;

    public static class APIError{
        public int code = 0;
        public String message;
        public String cause;
    }

    public APIError ServerLost;

    public enum CallType{
        POST {
            @Override
            public String toString() {
                return "POST";
            }
        },
        GET {
            @Override
            public String toString() {
                return "GET";
            }
        },

        PUT {
            @Override
            public String toString() {
                return "PUT";
            }
        }
    }

    private static TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs,
                        String authType) {}
                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs,
                        String authType) {}
            }
    };


    public interface LoginResult{           void get(LoginRequest.LoginRes res);                    }
    public interface UnitListResult{        void get(UnitListRequest.UnitListRes res);              }
    public interface VisitorResult{         void get(VisitorCheckInRequest.VisitorRes res);         }
    public interface WalkInResult{          void get(WalkInVisitorRequest.WalkInRes res);           }
    public interface UploadImageResult{     void get(UploadImage.UploadImageRes res);               }
    public interface VisitUpdateResult{     void get(VisitUpdateRequest.VisitUpdateRes res);        }
    public interface ChangePasswordResult{  void get(ChangePasswordRequest.ChangePasswordRes res);  }
    public interface VisitorListResult{     void get(VisitorListRequest.VisitorListRes res);        }
    public interface ReportResult{          void get(ReportRequest.ReportRes res);                  }
    public interface PushTokenUpdateResult{ void get(PushTokenUpdateRequest.PushTokenUpdateRes res);}

    public interface EmergencyListResult{   void get(EmergencyListRequest.EmergencyListRes res);    }
    public interface SOSUpdateResult{       void get(SOSUpdateRequest.SOSUpdateRes res);            }
    public interface UploadReportImageResult{void get(UploadReportImages.UploadReportImgRes res);   }




    @Override
    protected String doInBackground(Void... voids) {
        return null;
    }

    public String Request(String data, String server, CallType ct) {
        ServerLost = new APIError();
        ServerLost.message = "Sorry Server Down Please try again Later";
        ServerLost.code = 999;
        try {
            return ApiRequest(server,data, ct);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return "";
        }
    }

    public String RequestMultipart(File[] file, String server, CallType ct) {
        ServerLost = new APIError();
        ServerLost.message = "Sorry Server Down Please try again Later";
        ServerLost.code = 999;
        try {
            return ApiRequestMultipart(server,file, ct);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return "";
        }
    }

    public String HttpsRequest(String data, String server, CallType ct){
        String TAG = Tagit(server);
        String text = "";
        BufferedReader reader = null;

        try {
            URL url = new URL(server);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            //HttpURLConnection conn =(HttpURLConnection) url.openConnection();

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            conn.setUseCaches(false);
            conn.setRequestProperty("Authorization","BEARER "+application.getToken());
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            if(ct == CallType.POST || ct == CallType.PUT) {
                conn.setRequestMethod(String.valueOf(ct));
                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                wr.write( data );
                //DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                //wr.write(data.getBytes());
                wr.flush();
                wr.close();
            }


            int respCode = conn.getResponseCode();
            Log.d("responsecode",respCode+"");
            InputStreamReader in ;
            if(respCode != 200) {
                in = new InputStreamReader(conn.getErrorStream());
            }
            else
                in = new InputStreamReader(conn.getInputStream());
            reader = new BufferedReader(in);
            StringBuilder sb = new StringBuilder();
            String line = null;

            while((line = reader.readLine()) != null)
            { sb.append(line); }

            text = sb.toString();
            LOG(TAG,sb.toString());
            conn.disconnect();
        }
        catch(Exception ex) {
            Log.e(TAG,ex.getMessage(),ex);
        }
        finally
        {
            try {  if(reader != null)
                reader.close(); }
            catch(Exception ex) { Log.e(TAG, ex.getMessage(), ex); }

        }
        return text;
    }

    public String HttpRequest(String data, String server,CallType ct){
        String TAG = Tagit(server);
        String text = "";
        BufferedReader reader = null;

        try {
            URL url = new URL(server);
            //HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            HttpURLConnection conn =(HttpURLConnection) url.openConnection();

//            SSLContext sc = SSLContext.getInstance("SSL");
//            sc.init(null, trustAllCerts, new java.security.SecureRandom());
//            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            conn.setUseCaches(false);
            if(ct == CallType.POST || ct == CallType.PUT) {
                conn.setRequestMethod(String.valueOf(ct));
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                wr.write( data );
                //DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                //wr.write(data.getBytes());
                wr.flush();
                wr.close();
            }

            int respCode = conn.getResponseCode();
            Log.d("responsecode",respCode+"");
            InputStreamReader in ;
            if(respCode == 400) {
                in = new InputStreamReader(conn.getErrorStream());
            }
            else
                in = new InputStreamReader(conn.getInputStream());
            reader = new BufferedReader(in);
            StringBuilder sb = new StringBuilder();
            String line = null;

            while((line = reader.readLine()) != null)
            { sb.append(line); }

            text = sb.toString();
            LOG(TAG,sb.toString());
            conn.disconnect();
        }
        catch(Exception ex) {
            Log.e(TAG,ex.getMessage(),ex);
        }
        finally
        {
            try {  if(reader != null)
                reader.close(); }
            catch(Exception ex) { Log.e(TAG, ex.getMessage(), ex); }

        }
        return text;
    }

    static String Tagit(String url) {
        //String res = "basic";
        String[] tg = url.split("/");
        String key = (tg[tg.length-1]);//.split("\\."))[0];
        return key;
    }

    public static void LOG( String tag, String msg){
        if(Debug) Log.d(tag,msg);
    }


    public String ApiRequest(String server, String body, CallType ct) throws IOException {
        final MediaType JSON = MediaType.get("application/json;charset=UTF-8");

        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = null;
        if(body != null)
         formBody = RequestBody.create(body, JSON);
       Request.Builder rb  = new Request.Builder().url(server);
       if(application.getToken() != null) rb.header("Authorization","BEARER "+application.getToken());

       Request request;
        if(ct == CallType.POST) request = rb.post(formBody).build();
        else if(ct == CallType.PUT) request = rb.put(formBody).build();
        else request = rb.get().build();
        Response response = client.newCall(request).execute();
        assert response.body() != null;
        return response.body().string();
    }

    public String ApiRequestMultipart(String server, File[] files, CallType ct) throws IOException {

        OkHttpClient client = new OkHttpClient();
//        System.out.println(file.getAbsolutePath());
//        System.out.println(file.getName());
//        MultipartBody.Part filePart = MultipartBody.Part.createFormData("media", "document.jpeg", formBody);
        Request.Builder rb  = new Request.Builder().url(server);
        if(application.getToken() != null) rb.header("Authorization","BEARER "+application.getToken());
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MediaType.parse("multipart/form-data"));
        for(int i=0;i< files.length; i++){
            RequestBody formBody = RequestBody.create(MediaType.parse("image/png"), files[i]);
            builder.addFormDataPart("media","document_"+i+".png", formBody);
        };

        RequestBody requestBody = builder.build();
        Request request;
        if(ct == CallType.POST) request = rb.post(requestBody).build();
        else if(ct == CallType.PUT) request = rb.put(requestBody).build();
        else request = rb.get().build();
        Response response = client.newCall(request).execute();
        assert response.body() != null;
        return response.body().string();
    }


}