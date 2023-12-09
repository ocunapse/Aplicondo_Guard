package com.ocunapse.aplicondo.guard.ui.dashboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ocunapse.aplicondo.guard.R;
import com.ocunapse.aplicondo.guard.api.VisitorCheckInRequest;

import java.util.List;

public class VisitorListAdapter extends ArrayAdapter<VisitorCheckInRequest.Visitor> {

    private static class ViewHolder {
        TextView name;
        TextView unit;
    }

    public VisitorListAdapter(@NonNull Context context, int resource, @NonNull List<VisitorCheckInRequest.Visitor> objects) {
        super(context, resource, objects);
    }

    public VisitorListAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Get the data item for this position
        VisitorCheckInRequest.Visitor visit = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            // If there's no view to re-use, inflate a brand new view for row
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.visitor_list_item, parent, false);
            viewHolder.name = (TextView) convertView.findViewById(R.id.visitor_item_name_tv);
            viewHolder.unit = (TextView) convertView.findViewById(R.id.visitor_item_unit_tv);
            // Cache the viewHolder object inside the fresh view
            convertView.setTag(viewHolder);
        } else {
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Populate the data from the data object via the viewHolder object
        // into the template view.
        viewHolder.name.setText(visit.name);
        viewHolder.unit.setText(visit.unit.unit_label);
        // Return the completed view to render on screen
        return convertView;
    }
}
