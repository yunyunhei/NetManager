package cn.hulong.netmanager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import cn.hulong.netmanager.core.Api;
import cn.hulong.netmanager.ui.adapter.AppInfoAdapter;
import cn.hulong.netmanager.ui.adapter.DroidAppAdapter;
import cn.hulong.netmanager.ui.bean.PMAppInfo;

@SuppressWarnings("EmptyCatchBlock")
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private static final int ALL_APP = 1;
    private static final int SYSTEM_APP = 2;
    private static final int THIRD_APP = 3;
    private static final int SDCARD_APP = 4;
    private PackageManager pm;


    private ListView appinfo_listview;
    private AppInfoAdapter mAppInfoAdapter;

    List<ApplicationInfo> listAppcations = null;
    private ArrayList<PMAppInfo> mPMAppInfos = new ArrayList<>();

    private ArrayList<PMAppInfo> sysAppInfos = new ArrayList<>();
    private ArrayList<PMAppInfo> trdAppInfos = new ArrayList<>();

    private void initGetAppInfo() {
        backgroundHandler.post(new Runnable() {
            @SuppressWarnings("deprecation")
            @Override
            public void run() {
                pm = MainActivity.this.getPackageManager();
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    listAppcations = pm.getInstalledApplications(PackageManager.MATCH_UNINSTALLED_PACKAGES);
                } else {
                    listAppcations = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
                }

                trdAppInfos = getAppInfo(THIRD_APP);
                load3RdAoo();

                sysAppInfos = getAppInfo(SYSTEM_APP);

            }
        });
    }

    private ArrayList<PMAppInfo> getAppInfo(int flag) {
        ArrayList<PMAppInfo> appInfos = new ArrayList<>();
        appInfos.clear();
        switch (flag) {
            case ALL_APP:
                for (ApplicationInfo app : listAppcations) {
                    appInfos.add(makeAppInfo(app));
                }
                break;
            case SYSTEM_APP:
                for (ApplicationInfo app : listAppcations) {
                    if ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                        appInfos.add(makeAppInfo(app));
                    }
                }
                break;
            case THIRD_APP:
                for (ApplicationInfo app : listAppcations) {
                    if ((app.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
                        appInfos.add(makeAppInfo(app));
                    } else if ((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                        appInfos.add(makeAppInfo(app));
                    }
                }
                break;
            case SDCARD_APP:
                for (ApplicationInfo app : listAppcations) {
                    if ((app.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
                        appInfos.add(makeAppInfo(app));
                    }
                }
                break;
        }
        return appInfos;
    }


    private PMAppInfo makeAppInfo(ApplicationInfo app) {
        PMAppInfo appInfo = new PMAppInfo();
        appInfo.setAppLabel((String) app.loadLabel(pm));
        appInfo.setAppIcon(app.loadIcon(pm));
        appInfo.setPkgName(app.packageName);
        return appInfo;
    }

    private static final int HANDLER_MSG_REFRRESH = 0x00000001;
    private static final int HANDLER_MSG_REFRRESH_SYS = 0x00000002;
    private static final int HANDLER_MSG_REFRRESH_3RD = 0x00000003;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_MSG_REFRRESH:
                    mAppInfoAdapter.notifyDataSetChanged();
                    return true;
                case HANDLER_MSG_REFRRESH_3RD:
                    mPMAppInfos.clear();
                    mPMAppInfos.addAll(trdAppInfos);
                    mAppInfoAdapter.notifyDataSetChanged();
                    return true;
                case HANDLER_MSG_REFRRESH_SYS:
                    mPMAppInfos.clear();
                    mPMAppInfos.addAll(sysAppInfos);
                    mAppInfoAdapter.notifyDataSetChanged();
                    return true;
            }
            return false;
        }
    });


    private static HandlerThread mHandlerThread = new HandlerThread("loadPMThread");
    private static Handler backgroundHandler;

    static {
        mHandlerThread.start();
        backgroundHandler = new Handler(mHandlerThread.getLooper());
    }


    MenuItem whiteList;
    MenuItem blackList;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkPreferences();

        setContentView(R.layout.activity_main);

        appinfo_listview = (ListView) findViewById(R.id.appinfo_listview);

//        findViewById(R.id.label_mode_parent).setOnClickListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Menu menu = navigationView.getMenu();
        whiteList = menu.getItem(0);
        blackList = menu.getItem(1);

        mAppInfoAdapter = new AppInfoAdapter(mPMAppInfos, this);
        appinfo_listview.setAdapter(mAppInfoAdapter);

//        initGetAppInfo();

        Api.assertBinaries(this, true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Api.applications = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshHeader();
        showOrLoadApplications();
    }

    private void showOrLoadApplications() {
        final Resources res = getResources();
        if (Api.applications == null) {
            final ProgressDialog progress = ProgressDialog
                    .show(this,
                            res.getString(R.string.working),
                            res.getString(R.string.reading_apps), true);
            backgroundHandler.post(new Runnable() {
                @Override
                public void run() {
                    Api.getApps(MainActivity.this);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                progress.dismiss();
                            } catch (Exception ex) {
                            }
                            showApplications();
                        }
                    });
                }
            });
        } else {
            showApplications();
        }
    }

    private void showApplications() {
        final Api.DroidApp[] apps = Api.getApps(this);

        if (apps == null) {
            return;
        }

        Arrays.sort(apps, new Comparator<Api.DroidApp>() {
            @Override
            public int compare(Api.DroidApp o1, Api.DroidApp o2) {
                if ((o1.selected_wifi | o1.selected_3g) == (o2.selected_wifi | o2.selected_3g)) {
                    return o1.names[0].compareTo(o2.names[0]);
                }
                if (o1.selected_wifi || o1.selected_3g) return -1;
                return 1;
            }
        });

        DroidAppAdapter adapter = new DroidAppAdapter(Arrays.asList(apps), this);

        this.appinfo_listview.setAdapter(adapter);
    }

    private void checkPreferences() {
        final SharedPreferences prefs = getSharedPreferences(Api.PREFS_NAME, 0);
        final SharedPreferences.Editor editor = prefs.edit();
        boolean changed = false;
        if (prefs.getString(Api.PREF_MODE, "").length() == 0) {
            editor.putString(Api.PREF_MODE, Api.MODE_WHITELIST);
            changed = true;
        }
        /* delete the old preference names */
        if (prefs.contains("AllowedUids")) {
            editor.remove("AllowedUids");
            changed = true;
        }
        if (prefs.contains("Interfaces")) {
            editor.remove("Interfaces");
            changed = true;
        }
        if (changed) editor.apply();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_fw_Settings:
                disableOrEnable();
                break;
            case R.id.action_log_settings:
                showLog();
                break;
            case R.id.action_rule_settings:
                showRules();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.getItem(0);
        final boolean enabled = Api.isEnabled(this);
        if (enabled) {
            item.setTitle(R.string.fw_close);
        } else {
            item.setTitle(R.string.fw_open);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void showRules() {
        final Resources res = getResources();
        final ProgressDialog progress = ProgressDialog.show(this,
                res.getString(R.string.working),
                res.getString(R.string.please_wait), true);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    progress.dismiss();
                } catch (Exception ex) {
                }
                if (!Api.hasRootAccess(MainActivity.this, true)) return;
                Api.showIptablesRules(MainActivity.this);
            }
        }, 100);
    }

    private void showLog() {
        final Resources res = getResources();
        final ProgressDialog progress = ProgressDialog.show(this,
                res.getString(R.string.working),
                res.getString(R.string.please_wait), true);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    progress.dismiss();
                } catch (Exception ex) {
                }
                Api.showLog(MainActivity.this);
            }
        }, 100);
    }

    private void disableOrEnable() {
        final boolean enabled = !Api.isEnabled(this);
        Log.d("DroidWall", "Changing enabled status to: " + enabled);
        Api.setEnabled(this, enabled);
        if (enabled) {
            applyOrSaveRules();
        } else {
            purgeRules();
        }
        refreshHeader();
    }

    private void purgeRules() {
        final Resources res = getResources();
        final ProgressDialog progress = ProgressDialog.show(this,
                res.getString(R.string.working),
                res.getString(R.string.deleting_rules), true);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    progress.dismiss();
                } catch (Exception ex) {
                }
                if (!Api.hasRootAccess(MainActivity.this, true)) return;
                if (Api.purgeIptables(MainActivity.this, true)) {
                    Toast.makeText(MainActivity.this, R.string.rules_deleted, Toast.LENGTH_SHORT).show();
                }
            }
        }, 100);
    }

    private void applyOrSaveRules() {
        final Resources res = getResources();
        final boolean enabled = Api.isEnabled(this);
        final ProgressDialog progress = ProgressDialog.show(this, res.getString(R.string.working),
                res.getString(enabled ? R.string.applying_rules : R.string.saving_rules), true);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    progress.dismiss();
                } catch (Exception ex) {
                }
                if (enabled) {
                    if (Api.hasRootAccess(MainActivity.this, true) && Api.applyIptablesRules(MainActivity.this, true)) {
                        Toast.makeText(MainActivity.this, R.string.rules_applied, Toast.LENGTH_SHORT).show();
                    } else {
                        Api.setEnabled(MainActivity.this, false);
                    }
                } else {
                    Api.saveRules(MainActivity.this);
                    Toast.makeText(MainActivity.this, R.string.rules_saved, Toast.LENGTH_SHORT).show();
                }
            }
        }, 100);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_sys_apps) {
//            loadSystemApp();
            final String mode = Api.MODE_BLACKLIST;
            final SharedPreferences.Editor editor = getSharedPreferences(Api.PREFS_NAME, 0).edit();
            editor.putString(Api.PREF_MODE, mode);
            editor.apply();
            refreshHeader();
        } else if (id == R.id.nav_3rd_apps) {
//            load3RdAoo();
            final String mode = Api.MODE_WHITELIST;
            final SharedPreferences.Editor editor = getSharedPreferences(Api.PREFS_NAME, 0).edit();
            editor.putString(Api.PREF_MODE, mode);
            editor.apply();
            refreshHeader();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private void loadSystemApp() {
        mHandler.sendEmptyMessage(HANDLER_MSG_REFRRESH_SYS);
    }

    private void load3RdAoo() {
        mHandler.sendEmptyMessage(HANDLER_MSG_REFRRESH_3RD);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.appinfo_listview.setAdapter(null);
    }

    @Override
    protected void onDestroy() {
        mHandlerThread.quitSafely();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.label_mode_parent:
                selectMode();
                break;
        }
    }

    private void selectMode() {
        final Resources res = getResources();
        new AlertDialog.Builder(this).setItems(new String[]{res.getString(R.string.mode_whitelist), res.getString(R.string.mode_blacklist)}, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                final String mode = (which == 0 ? Api.MODE_WHITELIST : Api.MODE_BLACKLIST);
                final SharedPreferences.Editor editor = getSharedPreferences(Api.PREFS_NAME, 0).edit();
                editor.putString(Api.PREF_MODE, mode);
                editor.apply();
                refreshHeader();
            }
        }).setTitle("选择模式:").show();
    }

    /**
     * Refresh informative header
     */
    private void refreshHeader() {
        final SharedPreferences prefs = getSharedPreferences(Api.PREFS_NAME, 0);
        final String mode = prefs.getString(Api.PREF_MODE, Api.MODE_WHITELIST);
        final TextView labelmode = (TextView) this.findViewById(R.id.label_mode);
        final Resources res = getResources();
        int resid = (mode.equals(Api.MODE_WHITELIST) ? R.string.mode_whitelist : R.string.mode_blacklist);

        if (mode.equals(Api.MODE_WHITELIST)) {
            whiteList.setChecked(true);
            blackList.setChecked(false);
        }else {
            whiteList.setChecked(false);
            blackList.setChecked(true);
        }

        labelmode.setText(res.getString(R.string.mode_header, res.getString(resid)));
        resid = (Api.isEnabled(this) ? R.string.title_enabled : R.string.title_disabled);
        setTitle(res.getString(resid));
    }
}
