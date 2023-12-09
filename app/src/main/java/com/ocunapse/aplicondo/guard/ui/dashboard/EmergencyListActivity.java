package com.ocunapse.aplicondo.guard.ui.dashboard;

import static com.ocunapse.aplicondo.guard.api.RequestBase.LOG;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ocunapse.aplicondo.guard.R;
import com.ocunapse.aplicondo.guard.api.EmergencyListRequest;
import com.ocunapse.aplicondo.guard.api.VisitorCheckInRequest;
import com.ocunapse.aplicondo.guard.api.VisitorListRequest;
import com.ocunapse.aplicondo.guard.databinding.ActivityEmergencyListBinding;
import com.ocunapse.aplicondo.guard.databinding.ActivityVisitorListBinding;

import java.util.Arrays;
import java.util.Objects;

public class EmergencyListActivity extends AppCompatActivity {

    ActivityEmergencyListBinding binding;
    EmergencyListRequest.Emergency[] list = null;
    EmergencyListRequest.Emergency[] ogList = null;

    int currentTab = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmergencyListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        requestEmergencyData(false);

        // Expected Visitor
        Objects.requireNonNull(binding.emergencyTabs.getTabAt(0)).view.setOnClickListener(view -> {
            currentTab = 0;
            list = Arrays.stream(ogList).filter(emergency -> emergency.reported_id == null).toArray(EmergencyListRequest.Emergency[]::new);
            doList();
        });

        // Checked-In Visitor
        Objects.requireNonNull(binding.emergencyTabs.getTabAt(1)).view.setOnClickListener(view -> {
            currentTab = 1;
            list = Arrays.stream(ogList).filter(emergency -> emergency.reported_id != null).toArray(EmergencyListRequest.Emergency[]::new);
            doList();
        });

        binding.emergencySwiper.setOnRefreshListener(() -> requestEmergencyData(true));

    }

    protected void requestEmergencyData(boolean isSwipe){
        ProgressDialog dialog;
        if(!isSwipe){
            dialog = ProgressDialog.show(this, "Loading...",
                    "Loading. Please wait...", true);
        } else {
            dialog = null;
        }

        new EmergencyListRequest( res -> {
            if(res.success) {
                ogList = res.data;
                if(currentTab == 0) list = Arrays.stream(ogList).filter(emergency -> emergency.reported_id == null).toArray(EmergencyListRequest.Emergency[]::new);
                else list = Arrays.stream(ogList).filter(emergency -> emergency.reported_id != null).toArray(EmergencyListRequest.Emergency[]::new);
                doList();
            }
            if(!isSwipe) dialog.dismiss();
            else binding.emergencySwiper.setRefreshing(false);
        }).execute();
    }


    protected void doList(){
        binding.emergencyListSv.removeAllViews();
        LinearLayout lv = new LinearLayout(this);
        lv.setOrientation(LinearLayout.VERTICAL);
        lv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        if(list.length > 0) {
            for (EmergencyListRequest.Emergency v : list) {
                LayoutInflater inflater = LayoutInflater.from(getBaseContext());
                View item = inflater.inflate(R.layout.emergency_list_item, lv, false);
                item.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                ((TextView) item.findViewById(R.id.unit_no_tv)).setText(v.unit.unit_label);
                ((TextView) item.findViewById(R.id.name_tv)).setText(v.user==null? "": v.user.profiles.full_name);
                ((TextView) item.findViewById(R.id.contact_tv)).setText(v.user.profiles.phone_number);
                Button report = item.findViewById(R.id.report_btn);
                report.setVisibility(currentTab == 0 ? View.VISIBLE : View.GONE);
                report.setOnClickListener(view -> {
                    Intent i = new Intent(this, ReportActivity.class);
                    i.putExtra("sos_id", v.id);
                    i.putExtra("unit_label", v.unit.unit_label);
                    i.putExtra("name", v.user.profiles.full_name);
                    startActivity(i);
                });
                lv.addView(item);
                View space = new View(getBaseContext());
                space.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 35));
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
            ttv.setText("No record");
            lv.addView(ttv);
        }
        binding.emergencyListSv.addView(lv);
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestEmergencyData(true);
    }
}