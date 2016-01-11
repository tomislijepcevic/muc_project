package muc.project.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import muc.project.model.Client;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "CLIENT".
*/
public class ClientDao extends AbstractDao<Client, Long> {

    public static final String TABLENAME = "CLIENT";

    /**
     * Properties of entity Client.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Mac = new Property(1, String.class, "mac", false, "MAC");
        public final static Property Manufacturer = new Property(2, String.class, "manufacturer", false, "MANUFACTURER");
        public final static Property Subscribed = new Property(3, Boolean.class, "subscribed", false, "SUBSCRIBED");
        public final static Property Name = new Property(4, String.class, "name", false, "NAME");
        public final static Property Counter = new Property(5, int.class, "counter", false, "COUNTER");
    };

    private DaoSession daoSession;


    public ClientDao(DaoConfig config) {
        super(config);
    }
    
    public ClientDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"CLIENT\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"MAC\" TEXT NOT NULL ," + // 1: mac
                "\"MANUFACTURER\" TEXT," + // 2: manufacturer
                "\"SUBSCRIBED\" INTEGER," + // 3: subscribed
                "\"NAME\" TEXT," + // 4: name
                "\"COUNTER\" INTEGER NOT NULL );"); // 5: counter
        // Add Indexes
        db.execSQL("CREATE INDEX " + constraint + "IDX_CLIENT_MAC ON CLIENT" +
                " (\"MAC\");");
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"CLIENT\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, Client entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getMac());
 
        String manufacturer = entity.getManufacturer();
        if (manufacturer != null) {
            stmt.bindString(3, manufacturer);
        }
 
        Boolean subscribed = entity.getSubscribed();
        if (subscribed != null) {
            stmt.bindLong(4, subscribed ? 1L: 0L);
        }
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(5, name);
        }
        stmt.bindLong(6, entity.getCounter());
    }

    @Override
    protected void attachEntity(Client entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public Client readEntity(Cursor cursor, int offset) {
        Client entity = new Client( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getString(offset + 1), // mac
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // manufacturer
            cursor.isNull(offset + 3) ? null : cursor.getShort(offset + 3) != 0, // subscribed
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // name
            cursor.getInt(offset + 5) // counter
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, Client entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setMac(cursor.getString(offset + 1));
        entity.setManufacturer(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setSubscribed(cursor.isNull(offset + 3) ? null : cursor.getShort(offset + 3) != 0);
        entity.setName(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setCounter(cursor.getInt(offset + 5));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(Client entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(Client entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}