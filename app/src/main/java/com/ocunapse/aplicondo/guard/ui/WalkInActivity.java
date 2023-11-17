package com.ocunapse.aplicondo.guard.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.ocunapse.aplicondo.guard.R;
import com.ocunapse.aplicondo.guard.api.UnitListRequest;

import com.ocunapse.aplicondo.guard.databinding.ActivityWalkInBinding;

import java.util.ArrayList;

public class WalkInActivity extends AppCompatActivity {

    private ActivityWalkInBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityWalkInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ArrayList<String> units = new ArrayList<String>();

        new UnitListRequest(res -> {
           if(res.success){
               System.out.println(res.data);
               for(UnitListRequest.Unit o :res.data){
                   units.add(o.unit_label.toUpperCase());
               }
           }
        }).execute();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, android.R.layout.select_dialog_item, units);
        //Getting the instance of AutoCompleteTextView
        AutoCompleteTextView actv = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        actv.setThreshold(2);//will start working from first character
        actv.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
        actv.setTextColor(Color.BLACK);
    }
}