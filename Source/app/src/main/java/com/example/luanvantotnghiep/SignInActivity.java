package com.example.luanvantotnghiep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class SignInActivity extends AppCompatActivity {

    private LinearLayout layoutSignUp;
    private EditText edtEmail, edtPassword;
    private Button btnSignIn;
    private LinearLayout layoutForgotPassword;
    private ProgressDialog progressDialog;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        initUi();
        initListener();
    }

    private void initUi() {
        progressDialog = new ProgressDialog(this);

        layoutSignUp = findViewById(R.id.layout_sign_up);
        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_password);
        btnSignIn = findViewById(R.id.btn_sign_in);
        layoutForgotPassword = findViewById(R.id.layout_forgot_password);
        calendar = Calendar.getInstance();
    }

    private void initListener() {
        layoutSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickSignIn();
            }
        });

        layoutForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickForgotPassword();
            }
        });
    }


    private void onClickSignIn() {

        String strEmail = edtEmail.getText().toString().trim();
        String strPassword = edtPassword.getText().toString().trim();

        if(strEmail.length()>0&&strPassword.length()>0){
            String strGmail = strEmail.substring((strEmail.length()-10), strEmail.length());
            if (strGmail.equals("@gmail.com")) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                progressDialog.show();
                auth.signInWithEmailAndPassword(strEmail, strPassword)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressDialog.dismiss();

                                if (task.isSuccessful()) {
                                    // compare expiry date from realtime database and send result to out of date
                                    // srtEmail remove @gmail.com
                                    String strEmailRemove = strEmail.substring(0, (strEmail.length() - 10));

                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference myRef = database.getReference(strEmailRemove);
                                    myRef.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            // This method is called once with the initial value and again
                                            // whenever data at this location is updated.
                                            User user = dataSnapshot.getValue(User.class);
                                            if (((calendar.get(Calendar.DATE) - user.getDate()) > 7) ||
                                                    ((calendar.get(Calendar.MONTH) + 1 - user.getMonth()) > 0) || ((calendar.get(Calendar.YEAR) - user.getYear()) > 0)) {
                                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                                DatabaseReference myRef = database.getReference(strEmailRemove + "/out_of_date");
                                                myRef.setValue(true);
                                            } else if (((calendar.get(Calendar.DATE) - user.getDate()) < 7) &&
                                                    ((calendar.get(Calendar.MONTH) + 1 - user.getMonth()) == 0) && ((calendar.get(Calendar.YEAR) - user.getYear()) == 0)) {
                                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                                DatabaseReference myRef1 = database.getReference(strEmailRemove + "/out_of_date");
                                                myRef1.setValue(false);

                                            }

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError error) {
                                            // Failed to read value
                                        }
                                    });


                                    // Sign in success, update UI with the signed-in user's information
                                    Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finishAffinity();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(SignInActivity.this, "LogIn fail!! Please try again",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }else  Toast.makeText(SignInActivity.this,"Please enter email with address @gmail.com",Toast.LENGTH_SHORT).show();
        }
        else Toast.makeText(SignInActivity.this,"No Email or Password",Toast.LENGTH_SHORT).show();
    }

    private void onClickForgotPassword() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String strEmail = edtEmail.getText().toString().trim();
        if (strEmail.equals("")){
            Toast.makeText(SignInActivity.this, "Please enter your email",
                    Toast.LENGTH_SHORT).show();
        }else {
            progressDialog.show();
            auth.sendPasswordResetEmail(strEmail)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {
                                Toast.makeText(SignInActivity.this, "Check email "+strEmail,
                                        Toast.LENGTH_SHORT).show();

                            }else {
                                Toast.makeText(SignInActivity.this, "Email is not correct",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}