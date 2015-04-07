package com.uni.ailab.scp.receiver;

import com.uni.ailab.scp.cnf.Formula;
import com.uni.ailab.scp.policy.Permissions;
import com.uni.ailab.scp.policy.Policy;
import com.uni.ailab.scp.policy.Scope;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class SQLiteHelper extends SQLiteOpenHelper {

    public static final String TABLE_COMPONENTS = "components";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_ACTION = "action";
    public static final String COLUMN_SCHEME = "scheme";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_POLICIES = "policies";
    public static final String COLUMN_PERMISSIONS = "permissions";

    private static final String DATABASE_NAME = "components.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_COMPONENTS + "(" +
            COLUMN_ID + " text primary key, " +
            COLUMN_NAME + " text not null, " +
            COLUMN_TYPE + " text not null, " +
            COLUMN_POLICIES + " text, " +
            COLUMN_PERMISSIONS + " text);";

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
        
        /*
         * TEST: fill DB with example components
         */
        this.insertComponent("MainActivity", "Activity", new Policy[0], new String[0]);
        this.insertComponent("LoginActivity", "Activity", 
        		new Policy[] {new Policy(Scope.GLOBAL, 
        				Formula.not(Formula.or(Formula.lit(Permissions.MIC), Formula.lit(Permissions.CAM))), false)}, 
        				new String[0]);
        this.insertComponent("ContactPayRec.", "BroadcastReceiver", 
        		new Policy[] {
        		new Policy(Scope.DIRECT, Formula.imply(Formula.not(Formula.lit(Permissions.APP)), Formula.lit(Permissions.UAP)), false),
        		new Policy(Scope.LOCAL, Formula.and(Formula.lit(Permissions.RCP), Formula.lit(Permissions.GAP)), false)
        },
        		new String[0]);
        this.insertComponent("BalanceActivity", "Activity", 
        		new Policy[] {
        		new Policy(Scope.LOCAL, Formula.imply(
        				Formula.not(Formula.lit(Permissions.ACP)), 
        				Formula.not(Formula.or(Formula.or(Formula.lit(Permissions.NET), Formula.lit(Permissions.WSD)),
        						Formula.lit(Permissions.BTT)))), 
        				true)
        }, new String[0]);
        this.insertComponent("PaymentActivity", "Activity", new Policy[0], new String[0]);
        this.insertComponent("NormalPayRec.", "BroadcastReceiver", 
        		new Policy[] {
        		new Policy(Scope.DIRECT, Formula.and(Formula.lit(Permissions.NPP), Formula.lit(Permissions.UAP)), false)
        },
        		new String[0]);
        this.insertComponent("MicroPayRec.", "BroadcastReceiver", 
        		new Policy[] {
        		new Policy(Scope.DIRECT, 
        				Formula.and(Formula.lit(Permissions.MPP), 
        						Formula.or(Formula.lit(Permissions.UAP), Formula.lit(Permissions.APP))), 
        						false)
        },
        		new String[0]);
        this.insertComponent("ConnectionSer.", "Service", new Policy[0], new String[] {Permissions.NET, Permissions.ACP});
        this.insertComponent("HistoryProvider", "Provider", new Policy[0], new String[] {Permissions.RSD, Permissions.WSD, Permissions.ACP});
    }
    
    public void insertComponent(String name, String type, Policy[] policies, String[] permissions) {
    	SQLiteDatabase database = this.getWritableDatabase(); 
    	
    	String pol = formatPolicies(policies);
    	String per = formatPermissions(permissions);
    	
    	ContentValues values = new ContentValues(); 
    	values.put(COLUMN_NAME, name); 
    	values.put(COLUMN_TYPE, type);  
    	values.put(COLUMN_POLICIES, pol);
    	values.put(COLUMN_PERMISSIONS, per);
    	
    	database.insert(TABLE_COMPONENTS, null, values); 
    	database.close();
    }

    private String formatPolicies(Policy[] policies) {
		String s = "";
		for(int i = 0; i < policies.length; i++) {
			s += policies[i].toString() + ":";
		}
		return s;
	}

	private String formatPermissions(String[] permissions) {
		String s = "";
		for(int i = 0; i < permissions.length; i++) {
			s += permissions[i].toString() + ":";
		}
		return s;
	}

	@Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(SQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPONENTS);
        onCreate(db);
    }

    public String getQuery(String action, Uri data) {
        return "SELECT * FROM " + TABLE_COMPONENTS +" WHERE " + COLUMN_ACTION +" = "+ action;
    }

    public Cursor doQuery(String query) {

        SQLiteDatabase database = getReadableDatabase();

        // TODO: should check Uri scheme
        return database.rawQuery(query, null);
    }

    public Cursor getReceivers(String action, Uri data) {
        SQLiteDatabase database = getReadableDatabase();

        // TODO: should check Uri scheme
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_COMPONENTS +" WHERE " + COLUMN_ACTION +" = "+ action, null);

        return cursor;
    }

} 