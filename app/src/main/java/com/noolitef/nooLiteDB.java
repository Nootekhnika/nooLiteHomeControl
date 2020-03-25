package com.noolitef;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class nooLiteDB extends SQLiteOpenHelper {
    String[] tableLocations_rowNames = {"НООТЕХНИКА", "Демо", "Квартира"};
    String[] tableRooms_rowNames = {"Стенд PR1132", "Комната", "Кухня", "Прихожая", "Санузел"};
    int[] tableRooms_rowLocationsID = {1, 3, 3, 3, 3};

    ContentValues contentValues;

    public nooLiteDB(Context context) {
        super(context, "nooLiteDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        contentValues = new ContentValues();
        db.execSQL("CREATE TABLE locations ("
                + "id integer PRIMARY KEY AUTOINCREMENT,"
                + "name text" + ");");
        for (int l = 0; l < tableLocations_rowNames.length; l++) {
            contentValues.put("name", tableLocations_rowNames[l]);
            db.insert("locations", null, contentValues);
        }
        db.execSQL("CREATE TABLE rooms ("
                + "id integer PRIMARY KEY AUTOINCREMENT,"
                + "name text,"
                + "location_id integer,"
                + "FOREIGN KEY(location_id) REFERENCES locations(id)" + ");");
        for (int r = 0; r < tableRooms_rowNames.length; r++) {
            contentValues.put("name", tableRooms_rowNames[r]);
            contentValues.put("location_id", tableRooms_rowLocationsID[r]);
            db.insert("rooms", null, contentValues);
        }
        contentValues.clear();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
