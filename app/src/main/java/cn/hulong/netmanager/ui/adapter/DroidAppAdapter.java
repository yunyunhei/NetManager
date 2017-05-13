package cn.hulong.netmanager.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.hulong.netmanager.R;
import cn.hulong.netmanager.core.Api;

/**
 * Created by wuhang on 17/5/13.
 */

public class DroidAppAdapter extends BaseAdapter {

    private List<Api.DroidApp> datas;
    private Context mContext;

    public DroidAppAdapter(List<Api.DroidApp> datas, Context context) {
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
        Api.DroidApp appInfo = datas.get(position);
        if (appInfo != null) {
            viewHolder.app_icon.setImageDrawable(appInfo.getAppIcon());
            viewHolder.app_name.setText(appInfo.toString());
            viewHolder.app_package_name.setText(appInfo.getPkgName());
            viewHolder.box_wifi.setTag(appInfo);
            viewHolder.box_wifi.setChecked(appInfo.selected_wifi);

            viewHolder.box_3g.setTag(appInfo);
            viewHolder.box_3g.setChecked(appInfo.selected_wifi);

        }
        return convertView;
    }

    private static class ViewHolder implements CompoundButton.OnCheckedChangeListener {
        ImageView app_icon;
        TextView app_name;
        TextView app_package_name;

        CheckBox box_wifi;
        CheckBox box_3g;

        ViewHolder(View itemView) {
            app_icon = (ImageView) itemView.findViewById(R.id.app_icon);
            app_name = (TextView) itemView.findViewById(R.id.app_name);
            app_package_name = (TextView) itemView.findViewById(R.id.app_package_name);
            box_wifi = (CheckBox) itemView.findViewById(R.id.itemcheck_wifi);
            box_3g = (CheckBox) itemView.findViewById(R.id.itemcheck_3g);

            box_wifi.setOnCheckedChangeListener(this);
            box_3g.setOnCheckedChangeListener(this);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            final Api.DroidApp app = (Api.DroidApp) buttonView.getTag();
            if (app != null) {
                switch (buttonView.getId()) {
                    case R.id.itemcheck_wifi:
                        app.selected_wifi = isChecked;
                        break;
                    case R.id.itemcheck_3g:
                        app.selected_3g = isChecked;
                        break;
                }
            }
        }
    }
}
