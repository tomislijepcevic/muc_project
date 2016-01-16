package muc.project.helpers;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import muc.project.activities.MainActivity;
import muc.project.model.Client;

public class ClientArrayAdapter extends ArrayAdapter<Client> {

    public ClientArrayAdapter(Context context) {
        super(context, 0, new ArrayList<Client>());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this _position
        Client client = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        TextView firstText = (TextView) convertView.findViewById(android.R.id.text1);
        TextView secondText = (TextView) convertView.findViewById(android.R.id.text2);

        firstText.setText(client.getName() != null ? client.getName() : client.getMac());
        firstText.append(" [" + client.getCounter() + "]");
        firstText.setTextColor(Color.BLACK);
        secondText.setText(client.getManufacturer());
        secondText.setTextColor(Color.BLACK);

        return convertView;
    }
}
