package muc.project;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import muc.project.model.DaoSession;

/**
 * Created by peterus on 14.11.2015.
 */
public class ApplicationContext extends Application {
    private static ApplicationContext _INSTANCE = null;
    private DBHelper _dbHelper;

    @Override
    public void onCreate(){
        super.onCreate();
        _INSTANCE = this;
        _dbHelper = new DBHelper(_INSTANCE);
    }

    public static ApplicationContext getInstance(){
        return _INSTANCE;
    }


    // When you want a new session with cleared cache.
    public static DaoSession getNewSession() {
        return getInstance()._dbHelper.getSession(true);
    }

    public static DaoSession getSession() {
        return getInstance()._dbHelper.getSession(false);
    }
}
