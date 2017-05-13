package cn.hulong.netmanager.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import cn.hulong.netmanager.R;
import cn.hulong.netmanager.ui.bean.PMAppInfo;

/**
 * Created by wuhang on 16/11/17.
 */

public class AppInfoAdapter extends BaseAdapter {
    private ArrayList<PMAppInfo> datas;
    private Context mContext;

    public AppInfoAdapter(ArrayList<PMAppInfo> datas, Context context) {
        this.datas = datas;
        mContext = context;
    }

    @Override
    public int getCount() {
        return datas == null ? 0 : datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas == null ? null : datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.listview_item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        PMAppInfo appInfo = datas.get(position);
        if (appInfo != null) {
            viewHolder.app_icon.setImageDrawable(appInfo.getAppIcon());
            viewHolder.app_name.setText(appInfo.getAppLabel());
            viewHolder.app_package_name.setText(appInfo.getPkgName());
        }
        return convertView;
    }

    private static class ViewHolder {
        ImageView app_icon;
        TextView app_name;
        TextView app_package_name;

        ViewHolder(View itemView) {
            app_icon = (ImageView) itemView.findViewById(R.id.app_icon);
            app_name = (TextView) itemView.findViewById(R.id.app_name);
            app_package_name = (TextView) itemView.findViewById(R.id.app_package_name);
        }
    }
}
