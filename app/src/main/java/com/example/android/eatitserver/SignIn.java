package com.example.android.eatitserver;

import android.app.ProgressDialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.eatitserver.Common.Common;
import com.example.android.eatitserver.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import info.hoang8f.widget.FButton;

public class SignIn extends AppCompatActivity {

    EditText edtPhone,edtPassword;
    FButton btnSignIn;

    FirebaseDatabase database;
    DatabaseReference users;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        edtPhone=findViewById(R.id.edtPhone);
        edtPassword=findViewById(R.id.edtPassword);
        btnSignIn=findViewById(R.id.btnSignIn);

        database=FirebaseDatabase.getInstance();
        users=database.getReference("users");

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInUser(edtPhone.getText().toString(),edtPassword.getText().toString());
            }
        });

    }

    private void signInUser(String phone, String password) {
        final ProgressDialog mDialog=new ProgressDialog(SignIn.this);
        mDialog.setMessage("Please Wait... ");
        mDialog.show();
        final String localphone=phone;
        final String localpassword=password;

        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(localphone).exists()){
                    mDialog.dismiss();
                    User user=dataSnapshot.child(localphone).getValue(User.class);
                    user.setPhone(localphone);
                    if(Boolean.parseBoolean(user.getIsStaff())){
                        if(user.getPass().equals(localpassword)){
                            //login ok
                            Intent homeIntent=new Intent(SignIn.this,Home.class);
                            Common.currentUser=user;
                            startActivity(homeIntent);
                            finish();

                        }else {
                            Toast.makeText(SignIn.this, "Wrong Password...", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(SignIn.this, "Please login with Staff account", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(SignIn.this, "User cannot exist in Database", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
