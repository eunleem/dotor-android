package net.team88.dotor.reviews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;


import net.team88.dotor.R;
import net.team88.dotor.hospitals.Hospital;

import java.util.ArrayList;

/**
 * Created by Eun Leem on 3/6/2016.
 */
public class HospitalAutoCompleteTextViewAdapter extends ArrayAdapter<Hospital> {
    public HospitalAutoCompleteTextViewAdapter(Context context, int resource, ArrayList<Hospital> objects) {
        super(context, resource, objects);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.place_autocomplete_item,
                    parent, false);
        }

        TextView textPlaceName = (TextView) convertView.findViewById(R.id.textPlaceName);
        TextView textPlaceAddress = (TextView) convertView.findViewById(R.id.textPlaceAddress);

        Hospital hospital = this.getItem(position);

        textPlaceName.setText(hospital.name);
        textPlaceAddress.setText(hospital.address);

        return convertView;
        //return super.getView(position, convertView, parent);
    }

    @Override
    public Filter getFilter() {
        return this.filter;
    }

    Filter filter = new Filter() {
        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((Hospital) resultValue).name;
            //return super.convertResultToString(resultValue);
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint != null) {
                ArrayList<Hospital> suggestions = new ArrayList<>();
                for (int i = 0; i < getCount(); i++) {
                    Hospital hospital = getItem(i);
                    if (hospital.name.toLowerCase().contains(constraint.toString().toLowerCase())) {
                        suggestions.add(hospital);
                    }
                }

                results.values = suggestions;
                results.count = suggestions.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            if (results != null && results.count > 0) {
                addAll((ArrayList<Hospital>) results.values);

            } else {
                for (int i = 0; i < getCount(); i++) {
                    Hospital hospital = getItem(i);
                    add(hospital);
                }
            }

            notifyDataSetChanged();
        }
    };
}
