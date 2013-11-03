package com.thoughts.apps.reader.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.thoughts.apps.reader.Constants;
import com.thoughts.apps.reader.R;

/**
 * Created by Daniel on 8/17/13.
 */
public class DrawerAdapter extends BaseExpandableListAdapter {

    private static final int HOME = 0;
    private static final int VIDEO_GAMES = 1;
    private static final int TOURISM = 2;
    private static final int ABOUT = 3;
    private static final int CONTACT = 4;

    private LayoutInflater mInflater;
    private String[] mainNavItems, videoGameItems, tourismItems;

    public DrawerAdapter (Activity context) {
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mainNavItems = context.getResources().getStringArray(R.array.drawer_items);
        this.videoGameItems = context.getResources().getStringArray(R.array.video_games);
        this.tourismItems = context.getResources().getStringArray(R.array.tourism);
    }

    public Object getChild(int groupPosition, int childPosition) {

        switch (groupPosition) {
            case VIDEO_GAMES:
                return videoGameItems[childPosition];
            case TOURISM:
                return tourismItems[childPosition];
            default:
                return "";
        }
    }

    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.drawer_child_list_item, null);
        }

        TextView childText = (TextView) convertView.findViewById(R.id.drawer_child_list_item_text);
        childText.setText((String) getChild(groupPosition, childPosition));

        return convertView;
    }

    public int getChildrenCount(int groupPosition) {
        switch (groupPosition) {
            case VIDEO_GAMES:
                return videoGameItems.length;
            case TOURISM:
                return tourismItems.length;
            default:
                return 0;
        }
    }

    public Object getGroup(int groupPosition) {
        return mainNavItems[groupPosition];
    }

    public int getGroupCount() {
        return mainNavItems.length;
    }

    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.drawer_list_item, null);
        }

        TextView groupText = (TextView) convertView.findViewById(R.id.drawer_list_item_text);
        groupText.setText((String) getGroup(groupPosition));
        return convertView;
    }

    public boolean hasStableIds() {
        return true;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}
