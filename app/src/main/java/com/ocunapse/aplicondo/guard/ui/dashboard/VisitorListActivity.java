package com.ocunapse.aplicondo.guard.ui.dashboard;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

import static com.ocunapse.aplicondo.guard.api.RequestBase.LOG;
import static com.ocunapse.aplicondo.guard.ui.visitor_entry.EntryFragment.VisitorDialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.chip.Chip;
import com.ocunapse.aplicondo.guard.R;
import com.ocunapse.aplicondo.guard.api.VisitorCheckInRequest;
import com.ocunapse.aplicondo.guard.api.VisitorListRequest;
import com.ocunapse.aplicondo.guard.api.WalkInVisitorRequest;
import com.ocunapse.aplicondo.guard.databinding.ActivityVisitorListBinding;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

public class VisitorListActivity extends AppCompatActivity {

    ActivityVisitorListBinding binding;
    VisitorCheckInRequest.Visitor[] visitors = null;
    VisitorCheckInRequest.Visitor[] ogvisitors = null;

    SwipeRefreshLayout.OnRefreshListener refreshListener;
    int currentTab = 0;
    String[] historyStatus = {"ARRIVED","GUARD_REJECTED"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVisitorListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String date = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
        requestVisitorData(date,false);

        // Expected Visitor
        Objects.requireNonNull(binding.tabs.getTabAt(0)).view.setOnClickListener(view -> {
            currentTab = 0;
            visitors = Arrays.stream(ogvisitors).filter(visitor -> !Arrays.asList(historyStatus).contains(visitor.status)).toArray(VisitorCheckInRequest.Visitor[]::new);
            doList(date);
        });

        // Checked-In Visitor
        Objects.requireNonNull(binding.tabs.getTabAt(1)).view.setOnClickListener(view -> {
            currentTab = 1;
            visitors = Arrays.stream(ogvisitors).filter(visitor -> Arrays.asList(historyStatus).contains(visitor.status)).toArray(VisitorCheckInRequest.Visitor[]::new);
            doList(date);
        });

        refreshListener = () -> {
            requestVisitorData(date,true);
        };
        binding.swiperefresh.setOnRefreshListener(refreshListener);

    }

    protected void requestVisitorData(String date,boolean isSwipe){
        ProgressDialog dialog;
        if(!isSwipe){
             dialog = ProgressDialog.show(this, "Loading...",
                    "Loading. Please wait...", true);
        } else {
            dialog = null;
        }

        new VisitorListRequest(date, res -> {
            if(res.success) {
                ogvisitors = res.visitors;
                if(currentTab == 0) visitors = Arrays.stream(ogvisitors).filter(visitor -> !Arrays.asList(historyStatus).contains(visitor.status)).toArray(VisitorCheckInRequest.Visitor[]::new);
                else visitors = Arrays.stream(ogvisitors).filter(visitor -> Arrays.asList(historyStatus).contains(visitor.status)).toArray(VisitorCheckInRequest.Visitor[]::new);
                doList(date);
            }
            if(!isSwipe) dialog.dismiss();
            else binding.swiperefresh.setRefreshing(false);
        }).execute();
    }


    protected void doList(String date){
        binding.visitListSv.removeAllViews();
        LinearLayout lv = new LinearLayout(this);
        lv.setPadding(10,10,10,10);
        lv.setOrientation(LinearLayout.VERTICAL);
        lv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        if(visitors.length > 0) {
            for (VisitorCheckInRequest.Visitor v : visitors) {
                LayoutInflater inflater = LayoutInflater.from(getBaseContext());
                View item = inflater.inflate(R.layout.visitor_list_item, lv, false);
                item.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                ((TextView) item.findViewById(R.id.visitor_item_name_tv)).setText(v.name);
                ((TextView) item.findViewById(R.id.visitor_item_unit_tv)).setText(String.valueOf(v.unit.unit_label));
                if(v.vehicle_registration != null && v.vehicle_registration.length() > 0 && v.transport.equals(String.valueOf(WalkInVisitorRequest.Transport.VEHICLE)))
                    ((TextView) item.findViewById(R.id.visitor_item_vehicle_tv)).setText(v.vehicle_registration);
                else  {
                    ((TextView) item.findViewById(R.id.vNumHead)).setVisibility(View.GONE);
                    ((TextView) item.findViewById(R.id.visitor_item_vehicle_tv)).setVisibility(View.GONE);
                }
                ((TextView) item.findViewById(R.id.visitor_item_visit_type_tv)).setText(String.valueOf(v.type));
                if(v.reasonForVisit != null && v.reasonForVisit.length() > 0)
                    ((TextView) item.findViewById(R.id.visitor_item_purpose_tv)).setText(v.reasonForVisit);
                else  {
                    ((TextView) item.findViewById(R.id.vPurposeHead)).setVisibility(View.GONE);
                    ((TextView) item.findViewById(R.id.visitor_item_purpose_tv)).setVisibility(View.GONE);
                }
//                ((TextView) item.findViewById(R.id.visitor_item_purpose_tv)).setText(String.valueOf(v.reasonForVisit));
                String time = new SimpleDateFormat("hh:mm:ss a").format(v.visit_date);
                ((TextView) item.findViewById(R.id.visitor_item_time_tv)).setText(time);
                Button checkIn = item.findViewById(R.id.check_in_visitor);
                checkIn.setVisibility(currentTab == 0 ? View.VISIBLE : View.GONE);
                Button status = item.findViewById(R.id.status_lbl);
                status.setVisibility(currentTab == 0 ? View.GONE : View.VISIBLE);
                status.setText(String.valueOf(v.status).replace("_"," "));
                if(String.valueOf(v.status).equals("GUARD_REJECTED")) status.setBackgroundTintList(ColorStateList.valueOf(Color.RED));

                checkIn.setOnClickListener(view -> {
                    binding.swiperefresh.setRefreshing(true);
                    VisitorDialog(v,this, refreshListener);
                });
                lv.addView(item);
                View space = new View(getBaseContext());
                space.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 30));
                lv.addView(space);
            }
        }
        else {
            lv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            lv.setGravity(Gravity.CENTER);
            TextView ttv = new TextView(getBaseContext());
            ttv.setGravity(Gravity.CENTER);
            ttv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            ttv.setTextSize(20);
            ttv.setTypeface(null, Typeface.BOLD);
            ttv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            ttv.setText("No record for " + date);
            lv.addView(ttv);
        }
        binding.visitListSv.addView(lv);
    }

}