package com.ocunapse.aplicondo.guard.ui.settings;

import static com.ocunapse.aplicondo.guard.util.GeneralComponent.AlertBox;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.ocunapse.aplicondo.guard.GuardApp;
import com.ocunapse.aplicondo.guard.HomeActivity;
import com.ocunapse.aplicondo.guard.LoginActivity;
import com.ocunapse.aplicondo.guard.databinding.FragmentSettingsBinding;

import java.io.File;


public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private final GuardApp application = new GuardApp();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.userName.setText(application.getDecodedToken().full_name);

        binding.logoutBtn.setOnClickListener(view -> {
            AlertBox(getContext(), "Are you sure wanna log out?", (dialogInterface, i) -> {
                application.clear();
                startActivity(new Intent(getActivity(), LoginActivity.class));
                requireActivity().finish();
            });
        });

        binding.settingsBtn.setOnClickListener(view -> {
            view.setEnabled(false);
            startActivity(new Intent(getActivity(), ChangePasswordActivity.class));
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
        binding.settingsBtn.setEnabled(true);
    }
}