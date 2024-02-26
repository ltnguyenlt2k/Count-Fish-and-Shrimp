package com.example.luanvantotnghiep;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.luanvantotnghiep.ui.ChangePasswordFragment;
import com.example.luanvantotnghiep.ui.MyProfileFragment;
import com.example.luanvantotnghiep.ui.gallery.GalleryFragment;
import com.example.luanvantotnghiep.ui.home.HomeFragment;
import com.example.luanvantotnghiep.ui.slideshow.SlideshowFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.luanvantotnghiep.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    public static final int MY_REQUEST_CODE = 10;

    private static final int FRAGMENT_HOME = 0;
    private static final int FRAGMENT_GALLERY = 1;
    private static final int FRAGMENT_SLIDESHOW = 2;
    private static final int FRAGMENT_MY_PROFILE = 3;
    private static final int FRAGMENT_CHANGE_PASSWORD = 4;


    private int currentPragment = FRAGMENT_HOME;

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ImageView imgAvatar;
    private TextView tvName;
    private TextView tvEmail;
    private boolean out_of_date = false;
    final private MyProfileFragment mMyProfileFragment = new MyProfileFragment();

    final private ActivityResultLauncher<Intent> mActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK){
                        Intent intent = result.getData();
                        if (intent == null){
                            return;
                        }
                        Uri uri = intent.getData();
                        mMyProfileFragment.setmUri(uri);
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            mMyProfileFragment.setBimapImageView(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });



    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        unitUi();

        mDrawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView.setNavigationItemSelectedListener(this);

        replaceFragment(new HomeFragment());
        mNavigationView.getMenu().findItem(R.id.nav_home).setChecked(true);

        showUserInformation();

        readOutOfDate();

    }

    private void unitUi() {
        mNavigationView = findViewById(R.id.nav_view);
        imgAvatar = mNavigationView.getHeaderView(0).findViewById(R.id.img_avatar);
        tvName = mNavigationView.getHeaderView(0).findViewById(R.id.tv_name);
        tvEmail = mNavigationView.getHeaderView(0).findViewById(R.id.tv_email);
    }

    @Override
    public void onBackPressed(){
        //tạo Dialog
        AlertDialog.Builder mydialog = new AlertDialog.Builder(MainActivity.this);
        mydialog.setTitle("Question");
        mydialog.setMessage("Are you sure You want to Exit ?");
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }   else {
            mydialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    FirebaseAuth.getInstance().signOut();
                    finish();
                }
            });
            mydialog.create().show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {


        int id = item.getItemId();
        if (id == R.id.nav_home){
            if (FRAGMENT_HOME != currentPragment){
                replaceFragment(new HomeFragment());
                currentPragment = FRAGMENT_HOME;
            }

        }else if (id == R.id.nav_gallery){

            if(out_of_date){
                replaceFragment(new HomeFragment());
                currentPragment = FRAGMENT_HOME;
            }else {
                Intent intent = new Intent(MainActivity.this, SplashHDFishActivity.class);
                startActivity(intent);
            }

        }else if (id == R.id.nav_slideshow){

            if(out_of_date){
                replaceFragment(new HomeFragment());
                currentPragment = FRAGMENT_HOME;
            }else {
                Intent intent = new Intent(MainActivity.this, SplashHDShrimpActivity.class);
                startActivity(intent);
            }

        }else if (id == R.id.nav_sign_out){
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
            finish();
        }

        else if (id == R.id.nav_my_profile){
            if (FRAGMENT_MY_PROFILE != currentPragment){
                replaceFragment(mMyProfileFragment);
                currentPragment = FRAGMENT_MY_PROFILE;
            }
        }

        else if (id == R.id.nav_change_password){
            if (FRAGMENT_CHANGE_PASSWORD != currentPragment){
                replaceFragment(new ChangePasswordFragment());
                currentPragment = FRAGMENT_CHANGE_PASSWORD;
            }
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void replaceFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment);
        fragmentTransaction.commit();
    }

    public void showUserInformation(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            return;
        }
        String name = user.getDisplayName();
        String email = user.getEmail();
        Uri photoUrl = user.getPhotoUrl();

        if (name == null){
            tvName.setVisibility(View.GONE);
        }else {
            tvName.setVisibility(View.VISIBLE);
            tvName.setText(name);
        }
        tvEmail.setText(email);
        Glide.with(this).load(photoUrl).error(R.drawable.ic_avatar_default).into(imgAvatar);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_REQUEST_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                openGallery();
            }
        }
    }

    public void openGallery(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        mActivityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    private void readOutOfDate(){
        String email = tvEmail.getText().toString().trim();
        // srtEmail remove @gmail.com
        String strEmailRemove = email.substring(0, (email.length()-10));

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(strEmailRemove + "/out_of_date");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                boolean value = dataSnapshot.getValue(boolean.class);

                out_of_date = value;

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });
    }

}