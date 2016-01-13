package muc.project.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import muc.project.ApplicationContext;
import muc.project.R;
import muc.project.helpers.Constants;
import muc.project.model.Client;
import muc.project.model.ClientDao;
import muc.project.model.DaoSession;
import muc.project.services.WifiSensingIS;

public class ScanningActivity extends ActionBarActivity {

    private ListView _detectedClientsListView;
    private ArrayAdapter<String> _detectedClientsAdapter;
    private ClientDetectedBroadcastReceiver _broadcastReceiver;
    private ApplicationContext _dbHelper;
    private ClientDao _clientDao;
    private List<Client> _detectedClients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);

        _detectedClientsListView = (ListView) findViewById(R.id.detectedDevicesListView);
        _detectedClientsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        _detectedClientsListView.setAdapter(_detectedClientsAdapter);
        _detectedClientsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Client client = _detectedClients.get(position);
            }
        });

        _broadcastReceiver = new ClientDetectedBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(_broadcastReceiver,
                new IntentFilter(Constants.CLIENT_DETECTED_BROADCAST_RESULT));

        DaoSession session = _dbHelper.getSession();
        _clientDao = session.getClientDao();
        _detectedClients = new ArrayList<>();

        Intent wifiSensingServiceIntent = new Intent(getApplicationContext(), WifiSensingIS.class);
        startService(wifiSensingServiceIntent);
    }

    class ClientDetectedBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Long key = intent.getLongExtra("key", 0L);

            if (key != 0l) {
                Client client = _clientDao.load(key);
                _detectedClients.add(client);

                String clientName = client.getName();

                if (clientName != null && clientName.length() > 0) {
                    _detectedClientsAdapter.add(clientName);
                } else {
                    String manufacturer = client.getManufacturer();

                    if (manufacturer != null && manufacturer.length() > 0) {
                        _detectedClientsAdapter.add(manufacturer);
                    } else {
                     _detectedClientsAdapter.add(client.getMac());
                    }
                }
            }
        }
    }

}
