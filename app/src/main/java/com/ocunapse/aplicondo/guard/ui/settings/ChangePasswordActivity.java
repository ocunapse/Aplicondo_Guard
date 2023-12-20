package com.ocunapse.aplicondo.guard.ui.settings;

import static com.ocunapse.aplicondo.guard.util.GeneralComponent.AlertBox;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ocunapse.aplicondo.guard.R;
import com.ocunapse.aplicondo.guard.api.ChangePasswordRequest;
import com.ocunapse.aplicondo.guard.databinding.ActivityChangePasswordBinding;

import java.util.Objects;

public class ChangePasswordActivity extends AppCompatActivity {

    private ActivityChangePasswordBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Change Password");
        binding.oldPasswordEdittext.addTextChangedListener(handle(binding.oldPasswordEdittext));
        binding.newPasswordEdittext.addTextChangedListener(handle(binding.newPasswordEdittext));
        binding.confirmPasswordEdittext.addTextChangedListener(handle(binding.confirmPasswordEdittext));


        binding.changePwdBtn.setOnClickListener(view -> {
            String old = Objects.requireNonNull(binding.oldPasswordEdittext.getText()).toString();
            String newPwd = Objects.requireNonNull(binding.newPasswordEdittext.getText()).toString();

            new ChangePasswordRequest(old,newPwd,res -> {
                if(res.success) AlertBox(this,"Password Update Success!",(dialogInterface, i) -> finish());
                else if(res.error.code == 1060) AlertBox(this,"Current password is wrong!");
                else AlertBox(this,"Password Update Fail! Please try again later.",(dialogInterface, i) -> finish());
            }).execute();
        });
    }

    private void verify(EditText et){
        String val = et.getText().toString().trim();
        int l = val.length();
        if(l < 8){
            et.setError("Password minimum 8 characters");
            return;
        }
        if(et.getId() != R.id.old_password_edittext) {
            checkString(et, val);
            boolean isErrorFree = binding.oldPasswordEdittext.getError() == null && binding.newPasswordEdittext.getError() == null && binding.confirmPasswordEdittext.getError() == null;
            String newP = Objects.requireNonNull(binding.newPasswordEdittext.getText()).toString();
            String cfmP = Objects.requireNonNull(binding.confirmPasswordEdittext.getText()).toString();
            binding.confirmPasswordEdittext.setError(newP.equals(cfmP) ? null : "New password is not matching");
            isErrorFree = isErrorFree && newP.equals(cfmP);
            binding.changePwdBtn.setEnabled(isErrorFree);
        }
    }

    public TextWatcher handle(EditText et){
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                verify(et);
            }
        };
    }

    private static boolean checkString(EditText et, String str) {
        char ch;
        boolean capitalFlag = false;
        boolean lowerCaseFlag = false;
        boolean numberFlag = false;
        boolean specialFlag = false;
        String allowedString = "!@#$%^&*?.,+-_<>':;/()=~`";
        for(int i=0;i < str.length();i++) {
            ch = str.charAt(i);
            if( Character.isDigit(ch)) {
                numberFlag = true;
            }
            else if (Character.isUpperCase(ch)) {
                capitalFlag = true;
            }
            else if (Character.isLowerCase(ch)) {
                lowerCaseFlag = true;
            }
            else {
                char finalCh = ch;
                if (allowedString.chars().filter(a-> ((char)a) == finalCh).toArray().length > 0) {
                    specialFlag = true;
                }
            }

            if(!capitalFlag) et.setError("minimum 1 uppercase letter required");
            else if(!lowerCaseFlag) et.setError("minimum 1 lowercase letter required");
            else if(!numberFlag) et.setError("minimum 1 number required");
            else if(!specialFlag) et.setError("minimum 1 special character required");

            if(numberFlag && capitalFlag && lowerCaseFlag && specialFlag){
                et.setError(null);
                return true;
            }
        }
        return false;
    }
}