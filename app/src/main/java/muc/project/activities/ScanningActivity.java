package muc.project.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import muc.project.DBHelper;
import muc.project.R;
import muc.project.helpers.Constants;
import muc.project.model.Client;
import muc.project.model.ClientDao;
import muc.project.model.DaoSession;
import muc.project.services.WifiSensingIS;

public class ScanningActivity extends AppCompatActivity {

    private static final int CONTEXT_MENU_SUBSCRIBE = 0;
    private static final int CONTEXT_MENU_UNSUBSCRIBE = 0;

    private ClientDetectedBroadcastReceiver _broadcastReceiver;
    private ClientDao _clientDao;
    private ClientArrayAdapter _unsubscribedClientsAdapter;
    private ClientArrayAdapter _subscribedClientsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);

        // Setup the viewPager
        ViewPager mViewPager = (ViewPager) findViewById(R.id.view_pager);
        MyPagerAdapter pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(pagerAdapter);

        // Setup the Tabs
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        // By using this method the tabs will be populated according to viewPager's count and
        // with the name from the pagerAdapter getPageTitle()
        tabLayout.setTabsFromPagerAdapter(pagerAdapter);
        // This method ensures that tab selection events update the ViewPager and page changes update the selected tab.
        tabLayout.setupWithViewPager(mViewPager);

        _broadcastReceiver = new ClientDetectedBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(_broadcastReceiver,
                new IntentFilter(Constants.SUBSCRIBED_CLIENT_DETECTED_BROADCAST_RESULT));

        DBHelper dbHelper = new DBHelper(this);
        DaoSession session = dbHelper.getSession(true);
        _clientDao = session.getClientDao();
        _subscribedClientsAdapter = new ClientArrayAdapter(getApplicationContext());
        _unsubscribedClientsAdapter = new ClientArrayAdapter(getApplicationContext());

        Intent wifiSensingServiceIntent = new Intent(getApplicationContext(), WifiSensingIS.class);
        startService(wifiSensingServiceIntent);
    }

    class ClientDetectedBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Long key = intent.getLongExtra("key", 0L);

            if (key != 0l) {
                Client client = _clientDao.load(key);

                if (client.getSubscribed()) {
                    _subscribedClientsAdapter.add(client);
                } else {
                    _unsubscribedClientsAdapter.add(client);
                }
            }
        }
    }

    static class ClientArrayAdapter extends ArrayAdapter<Client> {

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
            firstText.setTextColor(Color.BLACK);
            secondText.setText(client.getManufacturer());
            secondText.setTextColor(Color.BLACK);

            return convertView;
        }
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: {
                    SubscribedClientsListFragment fragment = new SubscribedClientsListFragment();
                    fragment.setListAdapter(_subscribedClientsAdapter);
                    return fragment;
                }
                case 1: {
                    UnsubscribedClientsListFragment fragment = new UnsubscribedClientsListFragment();
                    fragment.setListAdapter(_unsubscribedClientsAdapter);
                    return fragment;
                }
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getResources().getString(R.string.scaning_activity_unsubscribe_fragment_title);
                case 1:
                    return getResources().getString(R.string.detected_unsubscribed_clients_fragment_title);
                default:
                    return null;
            }
        }
    }

    public static class SubscribedClientsListFragment extends ListFragment {

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            registerForContextMenu(getListView());
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            super.onCreateContextMenu(menu, v, menuInfo);

            menu.add(0, CONTEXT_MENU_UNSUBSCRIBE, Menu.NONE, getResources().getString(
                    R.string.scanning_activity_unsubscribe_option));
        }
    }

    public static class UnsubscribedClientsListFragment extends ListFragment {

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            registerForContextMenu(getListView());
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            super.onCreateContextMenu(menu, v, menuInfo);

            menu.add(1, CONTEXT_MENU_UNSUBSCRIBE, Menu.NONE, getResources().getString(
                    R.string.scanning_activity_subscribe_option));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int listPosition = info.position;

        switch (item.getGroupId()) {
            case 0: {
                switch(item.getItemId()) {
                    case CONTEXT_MENU_UNSUBSCRIBE: {
                        Client client = _subscribedClientsAdapter.getItem(listPosition);
                        client.setSubscribed(false);
                        _clientDao.update(client);

                        _subscribedClientsAdapter.remove(client);
                        _unsubscribedClientsAdapter.add(client);
                        return true;
                    }
                }
                break;
            }
            case 1: {
                switch(item.getItemId()) {
                    case CONTEXT_MENU_SUBSCRIBE: {
                        Client client = _unsubscribedClientsAdapter.getItem(listPosition);
                        client.setSubscribed(true);
                        _clientDao.update(client);

                        _unsubscribedClientsAdapter.remove(client);
                        _subscribedClientsAdapter.add(client);
                        return true;
                    }
                }
                break;
            }
        }

        return super.onContextItemSelected(item);
    }
}
