package edu.mit.mitmobile2.facilities;

import android.app.Activity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import edu.mit.mitmobile2.Constants;
import edu.mit.mitmobile2.MITAPIClient;
import edu.mit.mitmobile2.RetrofitManager;
import edu.mit.mitmobile2.facilities.model.FacilitiesCategory;
import edu.mit.mitmobile2.facilities.model.FacilityPlace;
import edu.mit.mitmobile2.facilities.model.FacilityPlaceCategory;
import edu.mit.mitmobile2.libraries.model.MITLibrariesLink;
import edu.mit.mitmobile2.maps.model.MITMapPlace;
import edu.mit.mitmobile2.shared.logging.LoggingManager;
import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;

public class FacilitiesManager extends RetrofitManager {
    private static final MitFacilityService MIT_FACILITY_SERVICE = MIT_REST_ADAPTER.create(MitFacilityService.class);

    @SuppressWarnings("unused")
    public static void makeHttpCall(String apiType, String path, HashMap<String, String> pathParams, HashMap<String, String> queryParams, Object callback)
            throws NoSuchFieldException, NoSuchMethodException, ClassNotFoundException, IllegalAccessException, InvocationTargetException {

        Method m = findMethodViaDirectReflection(MitFacilityService.class, path, pathParams, queryParams, Callback.class);
        LoggingManager.Timber.d("Method = " + m);
        m.invoke(MIT_FACILITY_SERVICE, callback);
    }

    @SuppressWarnings("unused")
    public static Object makeHttpCall(String apiType, String path, HashMap<String, String> pathParams, HashMap<String, String> queryParams)
            throws NoSuchFieldException, NoSuchMethodException, ClassNotFoundException, IllegalAccessException, InvocationTargetException {

        Method m = findMethodViaDirectReflection(MitFacilityService.class, path, pathParams, queryParams);
        LoggingManager.Timber.d("Method = " + m);
        return m.invoke(MIT_FACILITY_SERVICE);
    }

    /* GET requests */

    public static FacilityManagerCall getLocationCategories(Activity activity, Callback<HashMap<String, FacilitiesCategory>> callback) {
        LibraryManagerCallWrapper<?> returnValue = new LibraryManagerCallWrapper<>(new MITAPIClient(activity), callback);

        returnValue.getClient().get(Constants.FACILITIES, Constants.Facilities.FACILITIES_LOCATION_CATEGORIES_PATH, null, null, returnValue);

        return returnValue;
    }

    public static FacilityManagerCall getProblemTypes(Activity activity, Callback<List<String>> callback) {
        LibraryManagerCallWrapper<?> returnValue = new LibraryManagerCallWrapper<>(new MITAPIClient(activity), callback);

        returnValue.getClient().get(Constants.FACILITIES, Constants.Facilities.FACILITIES_PROBLEM_TYPES_PATH, null, null, returnValue);

        return returnValue;
    }

    public static FacilityManagerCall getLocationProperties(Activity activity, Callback<HashMap<String, HashMap<String, String>>> callback) {
        LibraryManagerCallWrapper<?> returnValue = new LibraryManagerCallWrapper<>(new MITAPIClient(activity), callback);

        returnValue.getClient().get(Constants.FACILITIES, Constants.Facilities.FACILITIES_LOCATION_PROPERTIES_PATH, null, null, returnValue);

        return returnValue;
    }

    public static FacilityManagerCall getPlaces(Activity activity, Callback<List<FacilityPlace>> callback) {
        LibraryManagerCallWrapper<?> returnValue = new LibraryManagerCallWrapper<>(new MITAPIClient(activity), callback);

        returnValue.getClient().get(Constants.FACILITIES, Constants.Facilities.FACILITIES_PLACES_PATH, null, null, returnValue);

        return returnValue;
    }

    public static FacilityManagerCall getPlaceCategories(Activity activity, Callback<List<FacilityPlaceCategory>> callback) {
        LibraryManagerCallWrapper<?> returnValue = new LibraryManagerCallWrapper<>(new MITAPIClient(activity), callback);

        returnValue.getClient().get(Constants.FACILITIES, Constants.Facilities.FACILITIES_PLACE_CATEGORIES_PATH, null, null, returnValue);

        return returnValue;
    }

    /* POST requests */

    // http://m.mit.edu/apis/building_services/problems

    public interface MitFacilityService {
        @GET(Constants.Facilities.FACILITIES_LOCATION_CATEGORIES_PATH)
        void _get_facilities(Callback<HashMap<String, FacilitiesCategory>> callback);

        @GET(Constants.Facilities.FACILITIES_PROBLEM_TYPES_PATH)
        void _get_problem_types(Callback<List<String>> callback);

        @GET(Constants.Facilities.FACILITIES_LOCATION_PROPERTIES_PATH)
        void _get_location_properties(Callback<HashMap<String, HashMap<String, String>>> callback);

        @GET(Constants.Facilities.FACILITIES_PLACES_PATH)
        void _get_places(Callback<List<FacilityPlace>> callback);

        @GET(Constants.Facilities.FACILITIES_PLACE_CATEGORIES_PATH)
        void _get_place_categories(Callback<List<FacilityPlaceCategory>> callback);

        @POST(Constants.Facilities.FACILITIES_PROBLEMS_PATH)
        void _post_problem(@Field("email") String email,
                           @Field("message") String message,
                           @Field("problem_type") String problemType,
                           @Field("building") String building,                  // optional
                           @Field("building_by_user") String buildingByUser,    // optional
                           @Field("room") String room,                          // optional
                           @Field("room_by_user") String roomByUser,            // optional
                           @Field("image") String image,                        // optional, base64 (multipart - ?)
                           Response callback);
    }

    public static class LibraryManagerCallWrapper<T> extends MITAPIClient.ApiCallWrapper<T> implements FacilityManagerCall, Callback<T> {
        public LibraryManagerCallWrapper(MITAPIClient client, Callback<T> callback) {
            super(client, callback);
        }
    }

    public interface FacilityManagerCall extends MITAPIClient.ApiCall {
    }
}
