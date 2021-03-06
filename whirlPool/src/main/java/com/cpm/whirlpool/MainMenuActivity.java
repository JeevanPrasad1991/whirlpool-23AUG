package com.cpm.whirlpool;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cpm.Constants.CommonString;
import com.cpm.Constants.CommonString1;
import com.cpm.dailyentry.DailyEntryScreen;
import com.cpm.database.GSKDatabase;
import com.cpm.delegates.CoverageBean;
import com.cpm.download.CompleteDownloadActivity;
import com.cpm.fragment.HelpFragment;
import com.cpm.fragment.MainFragment;
import com.cpm.message.AlertMessage;
import com.cpm.upload.CheckoutNUpload;
import com.cpm.upload.UploadDataActivity;
import com.cpm.upload.UploadOptionActivity;
import com.cpm.xmlGetterSetter.JourneyPlanGetterSetter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MainMenuActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    GSKDatabase database;
    ArrayList<JourneyPlanGetterSetter> jcplist;
    private SharedPreferences preferences = null;
    private String date, user_name, user_type;
    TextView tv_username, tv_usertype;
    FrameLayout frameLayout;
    NavigationView navigationView;
    ArrayList<CoverageBean> cdata = new ArrayList<CoverageBean>();
    JourneyPlanGetterSetter storestatus = new JourneyPlanGetterSetter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        frameLayout = (FrameLayout) findViewById(R.id.frame_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        user_name = preferences.getString(CommonString1.KEY_USERNAME, null);
        user_type = preferences.getString(CommonString1.KEY_USER_TYPE, null);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = LayoutInflater.from(this).inflate(R.layout.nav_header_main_menu2, navigationView, false);
        navigationView.addHeaderView(headerView);


        tv_username = (TextView) headerView.findViewById(R.id.nav_user_name);
        tv_usertype = (TextView) headerView.findViewById(R.id.nav_user_type);

        tv_username.setText(user_name);
        tv_usertype.setText(user_type);

        navigationView.setNavigationItemSelectedListener(this);

        database = new GSKDatabase(this);
        database.open();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        date = preferences.getString(CommonString1.KEY_DATE, null);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            // super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.empty_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
     /*   if (id == R.id.action_settings) {
            return true;
        }
*/
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_daily) {
            // Handle the camera action

            Intent startDownload = new Intent(this, DailyEntryScreen.class);
            startActivity(startDownload);

            overridePendingTransition(R.anim.activity_in, R.anim.activity_out);

        } else if (id == R.id.nav_download) {

            if (checkNetIsAvailable()) {
                if (database.isCoverageDataFilled(date)) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Parinaam");
                    builder.setMessage("Please Upload Previous Data First")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    Intent startUpload = new Intent(MainMenuActivity.this, CheckoutNUpload.class);
                                    startActivity(startUpload);
                                    finish();
                                }
                            });

                    AlertDialog alert = builder.create();
                    alert.show();

                } else {
                    Intent startDownload = new Intent(getApplicationContext(), CompleteDownloadActivity.class);
                    startActivity(startDownload);
                    finish();
                }
            } else {
                Snackbar.make(frameLayout, "No Network Available", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
            }

        } else if (id == R.id.nav_upload) {

            if (checkNetIsAvailable()) {
                jcplist = database.getJCPData(date);
                ArrayList<CoverageBean> cdata = new ArrayList<CoverageBean>();
                cdata = database.getCoverageData(date);
                boolean flag = true;
                if (jcplist.size() == 0) {
                    Snackbar.make(frameLayout, "Please Download Data First", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                } else {
                    if (cdata.size() > 0) {
                        for (int i = 0; i < cdata.size(); i++) {
                            if (cdata.get(i).getStatus().equals(CommonString1.KEY_CHECK_IN)) {
                                Snackbar.make(frameLayout, "First checkout of store", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                                flag = false;
                                break;
                            }
                        }
                        if (flag) {
                            if ((validate_data())) {
                                Intent i = new Intent(getBaseContext(), UploadDataActivity.class);
                                startActivity(i);
                                finish();
                            } else {
                                Snackbar.make(frameLayout, AlertMessage.MESSAGE_NO_DATA, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                            }
                        }
                    } else {
                        Snackbar.make(frameLayout, AlertMessage.MESSAGE_NO_DATA, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                    }
                }
            } else {
                Snackbar.make(frameLayout, "No Network Available", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
            }

        } else if (id == R.id.nav_exit) {
            Intent startDownload = new Intent(this, LoginActivity.class);
            startActivity(startDownload);
            finish();

        } else if (id == R.id.nav_help) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            HelpFragment cartfrag = new HelpFragment();
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_layout, cartfrag)
                    .commit();

        } else if (id == R.id.nav_export_database) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(MainMenuActivity.this);
            builder1.setMessage("Are you sure you want to take the backup of your data")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @SuppressWarnings("resource")
                        public void onClick(DialogInterface dialog,
                                            int id) {
                            try {
                                File sd = Environment.getExternalStorageDirectory();
                                File data = Environment.getDataDirectory();
                                if (sd.canWrite()) {
                                    long date = System.currentTimeMillis();
                                    SimpleDateFormat sdf = new SimpleDateFormat("MMM/dd/yy");
                                    String dateString = sdf.format(date);
                                    String currentDBPath = "//data//com.cpm.whirlpool//databases//" + GSKDatabase.DATABASE_NAME;
                                    String backupDBPath = "WHIRLPOOL_backup" + dateString.replace('/', '-');

                                    String path = Environment.getExternalStorageDirectory().getPath();

                                    File currentDB = new File(data, currentDBPath);
                                    File backupDB = new File(path, backupDBPath);

                                    Snackbar.make(frameLayout, "Database Exported Successfully", Snackbar.LENGTH_SHORT).show();

                                    if (currentDB.exists()) {
                                        @SuppressWarnings("resource")
                                        FileChannel src = new FileInputStream(currentDB).getChannel();
                                        FileChannel dst = new FileOutputStream(backupDB).getChannel();
                                        dst.transferFrom(src, 0, src.size());
                                        src.close();
                                        dst.close();
                                    }
                                }
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }

                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert1 = builder1.create();
            alert1.show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public boolean checkNetIsAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    @Override
    protected void onResume() {
        super.onResume();

        FragmentManager fragmentManager = getSupportFragmentManager();


        MainFragment cartfrag = new MainFragment();

        fragmentManager.beginTransaction()
                .replace(R.id.frame_layout, cartfrag)
                .commit();
    }

    public boolean validate_data() {
        boolean result = false;
        database.open();
        cdata = database.getCoverageData(date);
        for (int i = 0; i < cdata.size(); i++) {
            storestatus = database.getStoreStatus(cdata.get(i).getStoreId());
            if (!storestatus.getUploadStatus().get(0).equalsIgnoreCase(CommonString1.KEY_D)) {
                if ((storestatus.getCheckOutStatus().get(0).equalsIgnoreCase(
                        CommonString1.KEY_C)
                        || storestatus.getUploadStatus().get(0).equalsIgnoreCase(
                        CommonString1.KEY_P) || storestatus.getUploadStatus().get(0)
                        .equalsIgnoreCase(CommonString1.STORE_STATUS_LEAVE))) {
                    result = true;
                    break;

                }
            }
        }

        return result;
    }


}
