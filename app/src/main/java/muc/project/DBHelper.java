package muc.project;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import muc.project.model.DaoMaster;
import muc.project.model.DaoSession;

/**
 * Created by peterus on 14.11.2015.
 */
public class DBHelper {
    private String TAG = "DBHelper";

    private String DB_NAME = "MonitoringDB";
    private Context mContext;

    private SQLiteDatabase _db = null;
    private DaoSession _session = null;

    public DBHelper(Context context){
        Log.d("TEST", "test");
        mContext =  context;
        Log.d("TEST", "test");
    }

    private DaoMaster getMaster(){
        if (_db == null)
            _db = getDatabase(DB_NAME, false);

        return new DaoMaster(_db);
    }

    public DaoSession getSession(boolean newSession){

        if (newSession)
            return getMaster().newSession();
        if (_session == null)
            _session = getMaster().newSession();

        return _session;
    }

    private synchronized SQLiteDatabase getDatabase(String name, boolean readOnly){
        SQLiteOpenHelper helper = new AppOpenHelper(mContext, name, null);
        return readOnly ? helper.getReadableDatabase() : helper.getWritableDatabase();

    }

    private class AppOpenHelper extends DaoMaster.OpenHelper{
        public AppOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory){
            super(context, name, factory);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.i(TAG, "Create DB-Schema (version " + Integer.toString(DaoMaster.SCHEMA_VERSION) + ")");
            super.onCreate(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(TAG, "Update DB-Schema to version: "+Integer.toString(oldVersion)+"->"+Integer.toString(newVersion));
        }
    }

}
