package com.example.luanvantotnghiep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class SignUpActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword, edtConfirmPassword;
    private Button btnSignUp;
    private ProgressDialog progressDialog;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        initUi();
        initListener();
    }

    private void initUi() {
        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_password);
        edtConfirmPassword = findViewById(R.id.edt_confirm_password);
        btnSignUp = findViewById(R.id.btn_sign_up);

        progressDialog = new ProgressDialog(this);
        calendar = Calendar.getInstance();
    }

    private void initListener(){
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickSignUp();
            }
        });
    }

    private void onClickSignUp() {
        String strEmail = edtEmail.getText().toString().trim();
        String strPassword = edtPassword.getText().toString().trim();
        String strConfirmPassword = edtConfirmPassword.getText().toString().trim();
        // srtEmail remove @gmail.com
        if(strEmail.length()>0&&strPassword.length()>0&&strConfirmPassword.length()>0) {
            // so sanh co phai duoi @gmail.com khong
            String strGmail = strEmail.substring((strEmail.length()-10), strEmail.length());
            if(strGmail.equals("@gmail.com")) {
                if (strConfirmPassword.equals(strPassword)) {
                    String strEmailRemove = strEmail.substring(0, (strEmail.length() - 10));
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    progressDialog.show();
                    auth.createUserWithEmailAndPassword(strEmail, strPassword)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressDialog.dismiss();
                                    if (task.isSuccessful()) {
                                        // Sent data on realtime database
                                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                                        DatabaseReference myRef = database.getReference(strEmailRemove);
                                        User user = new User(calendar.get(Calendar.DATE), calendar.get(Calendar.MONTH) + 1,
                                                calendar.get(Calendar.YEAR), false);
                                        myRef.setValue(user);

                                        // Sign in success, update UI with the signed-in user's information
                                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finishAffinity();
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(SignUpActivity.this, "SignUp fail!! Please try again",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    Toast.makeText(SignUpActivity.this, "Enter the password",
                            Toast.LENGTH_SHORT).show();
                }
            }else {
                Toast.makeText(SignUpActivity.this,"Please enter email with address @gmail.com",Toast.LENGTH_SHORT).show();
            }
        }else {Toast.makeText(SignUpActivity.this,"No Email or Password",Toast.LENGTH_SHORT).show();}
    }

}