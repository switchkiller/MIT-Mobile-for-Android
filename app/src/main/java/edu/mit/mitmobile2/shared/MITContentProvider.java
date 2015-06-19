package edu.mit.mitmobile2.shared;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import java.util.ArrayList;

import edu.mit.mitmobile2.DBAdapter;
import edu.mit.mitmobile2.Schema;
import edu.mit.mitmobile2.shared.logging.LoggingManager.Timber;

public class MITContentProvider extends ContentProvider {

    // Shuttles
    public static final int ROUTES = 10;
    public static final int ROUTE_ID = 20;
    public static final int STOPS = 30;
    public static final int STOPS_ID = 40;
    public static final int CHECK_ROUTES = 50;
    public static final int CHECK_STOPS = 60;
    public static final int PREDICTIONS = 70;
    public static final int LOCATION = 80;
    public static final int SINGLE_STOP = 90;
    public static final int INTERSECTING_ROUTES = 100;
    public static final int ALERTS = 110;

    //Bookmarks
    public static final int BOOKMARKS = 120;

    //People
    public static final int PEOPLE = 130;
    public static final int PEOPLE_COUNT = 140;

    //QR Reader
    public static final int QRREADER = 150;

    private static final String AUTHORITY = "edu.mit.mitmobile2.provider";

    private static final String SHUTTLES_BASE_PATH = "shuttles";
    private static final String BOOKMARKS_BASE_PATH = "bookmarks";
    private static final String PEOPLE_BASE_PATH = "people";
    private static final String QRREADER_BASE_PATH = "qrreader";

    private static final Uri SHUTTLES_CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + SHUTTLES_BASE_PATH);
    private static final Uri BOOKMARKS_CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + BOOKMARKS_BASE_PATH);
    private static final Uri PEOPLE_CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + PEOPLE_BASE_PATH);
    private static final Uri QRREADER_CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + QRREADER_BASE_PATH);

    //Shuttles
    public static final Uri ALL_ROUTES_URI = Uri.parse(SHUTTLES_CONTENT_URI.toString() + "/routes");
    public static final Uri STOPS_URI = Uri.parse(SHUTTLES_CONTENT_URI.toString() + "/stops");
    public static final Uri CHECK_ROUTES_URI = Uri.parse(ALL_ROUTES_URI.toString() + "/check");
    public static final Uri CHECK_STOPS_URI = Uri.parse(STOPS_URI.toString() + "/check");
    public static final Uri PREDICTIONS_URI = Uri.parse(SHUTTLES_CONTENT_URI.toString() + "/predictions");
    public static final Uri VEHICLES_URI = Uri.parse(SHUTTLES_CONTENT_URI.toString() + "/vehicles");
    public static final Uri LOCATION_URI = Uri.parse(SHUTTLES_CONTENT_URI.toString() + "/location");
    public static final Uri SINGLE_STOP_URI = Uri.parse(SHUTTLES_CONTENT_URI.toString() + "/single-stop");
    public static final Uri INTERSECTING_ROUTES_URI = Uri.parse(SHUTTLES_CONTENT_URI.toString() + "/intersecting-routes");
    public static final Uri ALERTS_URI = Uri.parse(SHUTTLES_CONTENT_URI.toString() + "/alerts");

    //Bookmarks
    public static final Uri BOOKMARKS_URI = BOOKMARKS_CONTENT_URI;

    //People
    public static final Uri PEOPLE_URI = PEOPLE_CONTENT_URI;
    public static final Uri PEOPLE_COUNT_URI = Uri.parse(PEOPLE_CONTENT_URI.toString() + "/count");

    //QR Reader
    public static final Uri QRREADER_URI = QRREADER_CONTENT_URI;

    private static final String DB_SELECT_STRING = "SELECT route_stops._id AS rs_id, stops._id AS s_id, routes._id, routes.route_id, routes.route_url, routes.route_title, routes.agency, routes.scheduled, routes.predictable, routes.route_description, routes.predictions_url, routes.vehicles_url, routes.path_id, stops.stop_id, stops.stop_url, stops.stop_title, stops.stop_lat, stops.stop_lon, stops.stop_number, stops.distance, stops.predictions, stops.timestamp ";

    private static final String queryString = DB_SELECT_STRING +
            "FROM routes " +
            "INNER JOIN route_stops ON routes.route_id = route_stops.route_id " +
            "JOIN stops ON route_stops.stop_id = stops.stop_id " +
            "ORDER BY routes._id ASC, stops.distance ASC";

    public static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        //Shuttles
        sURIMatcher.addURI(AUTHORITY, SHUTTLES_BASE_PATH + "/routes", ROUTES);
        sURIMatcher.addURI(AUTHORITY, SHUTTLES_BASE_PATH + "/stops", STOPS);
        sURIMatcher.addURI(AUTHORITY, SHUTTLES_BASE_PATH + "/routes/check", CHECK_ROUTES);
        sURIMatcher.addURI(AUTHORITY, SHUTTLES_BASE_PATH + "/stops/check", CHECK_STOPS);
        sURIMatcher.addURI(AUTHORITY, SHUTTLES_BASE_PATH + "/routes/*", ROUTE_ID);
        sURIMatcher.addURI(AUTHORITY, SHUTTLES_BASE_PATH + "/stops/*", STOPS_ID);
        sURIMatcher.addURI(AUTHORITY, SHUTTLES_BASE_PATH + "/predictions", PREDICTIONS);
        sURIMatcher.addURI(AUTHORITY, SHUTTLES_BASE_PATH + "/location", LOCATION);
        sURIMatcher.addURI(AUTHORITY, SHUTTLES_BASE_PATH + "/single-stop", SINGLE_STOP);
        sURIMatcher.addURI(AUTHORITY, SHUTTLES_BASE_PATH + "/intersecting-routes", INTERSECTING_ROUTES);
        sURIMatcher.addURI(AUTHORITY, SHUTTLES_BASE_PATH + "/alerts", ALERTS);

        //Bookmarks
        sURIMatcher.addURI(AUTHORITY, BOOKMARKS_BASE_PATH, BOOKMARKS);

        //People
        sURIMatcher.addURI(AUTHORITY, PEOPLE_BASE_PATH, PEOPLE);
        sURIMatcher.addURI(AUTHORITY, PEOPLE_BASE_PATH + "/count", PEOPLE_COUNT);

        //QR Reader
        sURIMatcher.addURI(AUTHORITY, QRREADER_BASE_PATH, QRREADER);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        Timber.d("Query");

        int uriType = sURIMatcher.match(uri);
        Timber.d("Uri= " + uriType);
        Timber.d("Selection= " + selection);

        switch (uriType) {
            case CHECK_ROUTES:
                cursor = DBAdapter.getInstance().db.query(Schema.Route.TABLE_NAME, new String[]{Schema.Route.ID_COL}, selection, null, null, null, null);
                break;
            case CHECK_STOPS:
                cursor = DBAdapter.getInstance().db.query(Schema.Stop.TABLE_NAME, new String[]{Schema.Stop.ID_COL}, selection, null, null, null, null);
                break;
            case ROUTES:
                cursor = DBAdapter.getInstance().db.rawQuery(queryString, null, null);
                break;
            case STOPS_ID:
                cursor = DBAdapter.getInstance().db.rawQuery(buildStopsQueryString(selection), null, null);
                break;
            case ROUTE_ID:
                cursor = DBAdapter.getInstance().db.rawQuery(buildQueryString(Schema.Route.TABLE_NAME + "." + selection), null, null);
                break;
            case SINGLE_STOP:
                cursor = DBAdapter.getInstance().db.rawQuery(buildSingleStopQueryString(selection), null, null);
                break;
            case INTERSECTING_ROUTES:
                cursor = DBAdapter.getInstance().db.rawQuery(buildIntersectingRoutesQueryString(selection), null, null);
                break;
            case ALERTS:
                cursor = DBAdapter.getInstance().db.query(Schema.Alerts.TABLE_NAME, Schema.Alerts.ALL_COLUMNS, selection, null, null, null, null);
                break;
            case BOOKMARKS:
                cursor = DBAdapter.getInstance().db.query(Schema.MapPlace.TABLE_NAME, Schema.MapPlace.ALL_COLUMNS, selection, selectionArgs, null, null, null);
                break;
            case PEOPLE:
                cursor = DBAdapter.getInstance().db.query(Schema.Person.TABLE_NAME, Schema.Person.ALL_COLUMNS, selection, selectionArgs, null, null, null);
                break;
            case PEOPLE_COUNT:
                cursor = DBAdapter.getInstance().db.rawQuery("SELECT count(*) FROM " + Schema.Person.TABLE_NAME, null);
                break;
            case QRREADER:
                cursor = DBAdapter.getInstance().db.query(Schema.QrReaderResult.TABLE_NAME, Schema.QrReaderResult.ALL_COLUMNS, selection, selectionArgs, null, null, null);
                break;
        }

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return new String();
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Timber.d("Insert");

        int uriType = sURIMatcher.match(uri);
        Timber.d("Uri= " + uriType);
        long newID = 0;

        switch (uriType) {
            case ROUTES:
                newID = DBAdapter.getInstance().db.insertWithOnConflict(Schema.Route.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_FAIL);
                break;
            case STOPS:
                newID = DBAdapter.getInstance().db.insertWithOnConflict(Schema.Stop.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_FAIL);
                break;
            case LOCATION:
                newID = DBAdapter.getInstance().db.insertWithOnConflict(Schema.Location.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                break;
            case ALERTS:
                newID = DBAdapter.getInstance().db.insertWithOnConflict(Schema.Alerts.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_FAIL);
                break;
            case BOOKMARKS:
                newID = DBAdapter.getInstance().db.insertWithOnConflict(Schema.MapPlace.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_FAIL);
                break;
            case PEOPLE:
                newID = DBAdapter.getInstance().db.insertWithOnConflict(Schema.Person.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_FAIL);
                break;
            case QRREADER:
                newID = DBAdapter.getInstance().db.insertWithOnConflict(Schema.QrReaderResult.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_FAIL);
                break;
        }
        if (newID <= 0) {
            throw new SQLException("Error");
        }
        Timber.d("DB id= " + newID);

        return Uri.parse(String.valueOf(newID));
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        Timber.d("Bulk Insert");

        int uriType = sURIMatcher.match(uri);
        Timber.d("Uri= " + uriType);

        long newID;
        switch (uriType) {
            case ROUTES:
                for (ContentValues v : values) {
                    newID = DBAdapter.getInstance().db.insertWithOnConflict(Schema.Route.TABLE_NAME, null, v, SQLiteDatabase.CONFLICT_FAIL);
                    Timber.d("Bulk Insert - ID = " + newID);
                    if (newID <= 0) {
                        throw new SQLException("Error");
                    }
                }
                break;
            case STOPS:
                for (ContentValues v : values) {
                    newID = DBAdapter.getInstance().db.insertWithOnConflict(Schema.Stop.TABLE_NAME, null, v, SQLiteDatabase.CONFLICT_FAIL);
                    Timber.d("Bulk Insert - ID = " + newID);
                    if (newID <= 0) {
                        throw new SQLException("Error");
                    }
                }
                break;

        }
        return 0;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Timber.d("Delete");

        int uriType = sURIMatcher.match(uri);
        Timber.d("Uri= " + uriType);

        int rows = 0;
        switch (uriType) {
            case ALERTS:
                rows = DBAdapter.getInstance().db.delete(Schema.Alerts.TABLE_NAME, selection, null);
                break;
            case BOOKMARKS:
                rows = DBAdapter.getInstance().db.delete(Schema.MapPlace.TABLE_NAME, selection, selectionArgs);
                rows += DBAdapter.getInstance().db.delete(Schema.MapPlaceContent.TABLE_NAME, selection, selectionArgs);
                break;
            case PEOPLE:
                rows = DBAdapter.getInstance().db.delete(Schema.Person.TABLE_NAME, selection, selectionArgs);
                break;
            case QRREADER:
                rows = DBAdapter.getInstance().db.delete(Schema.QrReaderResult.TABLE_NAME, selection, null);
                break;
        }

        return rows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Timber.d("Update");

        int uriType = sURIMatcher.match(uri);
        Timber.d("Uri= " + uriType);

        int rows = 0;

        switch (uriType) {
            case ROUTES:
                rows = DBAdapter.getInstance().db.update(Schema.Route.TABLE_NAME, values, Schema.Route.ROUTE_ID + " = \'" + values.get(Schema.Route.ROUTE_ID) + "\'", null);
                break;
            case STOPS:
                if (values.containsKey(Schema.Stop.STOP_ID)) {
                    rows = DBAdapter.getInstance().db.update(Schema.Stop.TABLE_NAME, values, Schema.Stop.STOP_ID + " = \'" + values.get(Schema.Stop.STOP_ID) + "\'", null);
                } else {
                    rows = DBAdapter.getInstance().db.update(Schema.Stop.TABLE_NAME, values, null, null);
                }
                break;
            case PREDICTIONS:
                rows = DBAdapter.getInstance().db.update(Schema.Stop.TABLE_NAME, values, selection, null);
                break;
            case ROUTE_ID:
                rows = DBAdapter.getInstance().db.update(Schema.Route.TABLE_NAME, values, selection, null);
                break;
            case ALERTS:
                rows = DBAdapter.getInstance().db.update(Schema.Alerts.TABLE_NAME, values, selection, null);
                break;
        }

        return rows;
    }

    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
        Timber.d("Batch Update");

        for (ContentProviderOperation operation : operations) {
            operation.apply(this, null, 1);
        }

        return new ContentProviderResult[operations.size()];
    }

    public String buildQueryString(String selection) {
        return DB_SELECT_STRING +
                "FROM routes " +
                "INNER JOIN route_stops ON routes.route_id = route_stops.route_id " +
                "JOIN stops ON route_stops.stop_id = stops.stop_id " +
                "WHERE " + selection +
                "ORDER BY routes._id ASC, rs_id ASC";
    }

    public String buildStopsQueryString(String selection) {
        return DB_SELECT_STRING +
                "FROM stops " +
                "INNER JOIN route_stops ON stops.stop_id = route_stops.stop_id " +
                "JOIN routes ON route_stops.route_id = routes.route_id " +
                "WHERE " + selection +
                "ORDER BY routes._id ASC, rs_id ASC";
    }

    public String buildSingleStopQueryString(String selection) {
        return "SELECT * " +
                "FROM stops " +
                "WHERE " + selection;
    }

    public String buildIntersectingRoutesQueryString(String selection) {
        return "SELECT route_stops._id AS rs_id, stops._id AS s_id, routes._id, routes.route_id, routes.route_url, routes.route_title, routes.agency, routes.scheduled, routes.predictable, stops.stop_id, stops.stop_url, stops.stop_title, stops.timestamp " +
                "FROM routes " +
                "INNER JOIN route_stops ON routes.route_id = route_stops.route_id " +
                "JOIN stops ON route_stops.stop_id = stops.stop_id " +
                "WHERE " + selection +
                "ORDER BY routes._id ASC, rs_id ASC";
    }
}