package com.ocunapse.aplicondo.guard.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.ocunapse.aplicondo.guard.databinding.FragmentDashboardBinding;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.visitorListBtn.setOnClickListener(view -> {
            view.setEnabled(false);
            startActivity(new Intent(requireActivity(), VisitorListActivity.class));
        });

        binding.emerListBtn.setOnClickListener(view -> {
            view.setEnabled(false);
            startActivity(new Intent(requireActivity(), EmergencyListActivity.class));
        });

        binding.reportListBtn.setOnClickListener(view -> {
            view.setEnabled(false);
            startActivity(new Intent(requireActivity(), ReportActivity.class));
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.emerListBtn.setEnabled(true);
        binding.visitorListBtn.setEnabled(true);
        binding.reportListBtn.setEnabled(true);
    }
}