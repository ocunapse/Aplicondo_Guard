package com.ocunapse.aplicondo.guard.api;


import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class ReportRequest extends RequestBase {

    private final String prefix;

    ReportResult res;
    String Name;
    String mobNum;
    String callerName;
    String unit;
    String report;
    int sosId;
    GuardReportType gtype;
    String image_urls[];

    public enum GuardReportType {
        SOS,
        Incident,
        Other
    }

    public ReportRequest(@NonNull GuardReportType type, String unitNumber, String resident_name, String resident_contact, String details, String[] image_attachments, int sos_id, ReportResult res) {
        this.callerName = resident_name;
        this.mobNum = resident_contact;
        this.unit = unitNumber;
        this.image_urls = image_attachments;
        this.gtype = type;
        this.report = details;
        this.res = res;
        if(sos_id > 0) this.sosId = sos_id;
        int siteId = application.getSite();
        this.prefix = String.format(Locale.ENGLISH, "/guards/%d/createReport", siteId);
    }

    public ReportRequest(@NonNull GuardReportType type, String unitNumber, String resident_name, String resident_contact, String details, String[] image_attachments, ReportResult res) {
        this(type,unitNumber,resident_name,resident_contact,details,image_attachments,0,res);
    }


    class ReportReq {
        String type = String.valueOf(gtype);
        String guard_name = application.getDecodedToken().full_name;
        String resident_name = callerName;
        String resident_contact = mobNum;
        String unitNumber = unit;
        String details = report;
        int sos_id = sosId;
        String[] image_attachments = image_urls;
    }

    public static class Report {
        int id;
        int site_id;
        String incident_id;
        String type;
        Date date;
        String guard_name;
        String unitNumber;
        String resident_name;
        String reident_contact;
        String details;
        String[] image_attachments;
    }


    public static class ReportRes {
        public Boolean success;
        public APIError error;
        public Report data;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        LOG("Report--", s);
        if (s.length() < 1) {
            ReportRes rs = new ReportRes();
            rs.success = false;
            rs.error = ServerLost;
            res.get(rs);
        } else {
            try {
                ReportRes rs = g.fromJson(s, ReportRes.class);
                res.get(rs);
            } catch (Exception e) {
                Log.e("API error", Objects.requireNonNull(e.getMessage()));
                ReportRes rs = new ReportRes();
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
        String data = g.toJson(new ReportReq());
        LOG("Report", data);
        String url = server + prefix;
        LOG("Report", url);
        return Request(data, url, CallType.POST);
    }

}
