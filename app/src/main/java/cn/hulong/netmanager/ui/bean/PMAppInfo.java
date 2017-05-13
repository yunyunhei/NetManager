package cn.hulong.netmanager.ui.bean;

import android.graphics.drawable.Drawable;

public class PMAppInfo {
    private String appLabel;
    private Drawable appIcon;
    private String pkgName;

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public String getAppLabel() {

        return appLabel;
    }

    public void setAppLabel(String appLabel) {
        this.appLabel = appLabel;
    }
}
