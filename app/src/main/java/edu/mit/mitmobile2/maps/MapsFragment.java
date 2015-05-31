package edu.mit.mitmobile2.maps;

import android.content.Intent;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.TextView;

import edu.mit.mitmobile2.MitMapFragment;
import edu.mit.mitmobile2.maps.activities.MapsCategoriesActivity;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.mit.mitmobile2.MITSearchAdapter;
import edu.mit.mitmobile2.MitMobileApplication;
import edu.mit.mitmobile2.OttoBusEvent;
import edu.mit.mitmobile2.PreferenceUtils;
import edu.mit.mitmobile2.R;
import edu.mit.mitmobile2.maps.activities.MapSearchResultActivity;
import edu.mit.mitmobile2.maps.model.MITMapPlace;
import edu.mit.mitmobile2.shared.callback.FullscreenMapCallback;
import edu.mit.mitmobile2.shared.fragment.FullscreenMapFragment;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MapsFragment extends FullscreenMapFragment implements FullscreenMapCallback {

    private static final String MAPS_SEARCH_HISTORY = "mapSearchHistory";

    private ListView recentsListview;
    private Mode mode;
    private MITSearchAdapter<String> recentSearchAdapter;
    private HashSet<String> recentSearches;
    private SharedPreferences sharedPreferences;
    private SearchView searchView;
    private TextView searchTextView;
    private List<MITMapPlace> places;

    public MapsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        Intent i = new Intent(getActivity(), MapsCategoriesActivity.class);
        startActivity(i);

        mapCallback = this;

        mitMapView.mapBoundsPadding = (int) getActivity().getResources().getDimension(R.dimen.map_bounds_padding);
        places = new ArrayList<>();

        this.setHasOptionsMenu(true);

        sharedPreferences = PreferenceUtils.getDefaultSharedPreferencesMultiProcess(getActivity());

        //noinspection ConstantConditions
        recentsListview = (ListView) view.findViewById(R.id.map_listview);

        recentSearches = new LinkedHashSet<>();
        Set<String> set = sharedPreferences.getStringSet(MAPS_SEARCH_HISTORY, null);

        if (set != null) {
            recentSearches.addAll(set);
        }

        recentSearchAdapter = new MITSearchAdapter<>(getActivity(), recentSearches, new MITSearchAdapter.FragmentCallback<String>() {
            @Override
            public void itemClicked(String item) { /* No-Op */ }

            @Override
            public void itemSearch(String searchText) {
                performSearch(recentsListview, new Pair<>(recentSearchAdapter, this), searchText);
                searchTextView.setText(searchText);
            }
        });

        recentsListview.setAdapter(recentSearchAdapter);

        return view;
    }

    private boolean searchTextChanged(View sender, Object handler, String s) {
        if (!TextUtils.isEmpty(s) && s.length() >= 1) {
            recentsListview.setVisibility(View.VISIBLE);
            recentSearchAdapter.getFilter().filter(s);
        } else {
            recentsListview.setVisibility(View.GONE);
        }
        return false;
    }

    private boolean performSearch(View sender, Object handler, String searchText) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (!recentSearches.contains(searchText)) {
            recentSearches.add(searchText);
            editor.putStringSet(MAPS_SEARCH_HISTORY, recentSearches);
            editor.apply();
            recentSearchAdapter.updateRecents(recentSearches);
        }

        this.setMode(Mode.LIST_BLANK);

        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("q", searchText);

        MapManager.getMapPlaces(getActivity(), queryParams, new Callback<ArrayList<MITMapPlace>>() {
            @Override
            public void success(ArrayList<MITMapPlace> mitMapPlaces, Response response) {
                for (MITMapPlace mapPlace : mitMapPlaces) {
                    int i = mitMapPlaces.indexOf(mapPlace);
                    String markerText = (i + 1) < 10 ? "   " + (i + 1) + "   " : "  " + (i + 1) + "  ";
                    mapPlace.setMarkerText(markerText);
                }

                updateMapItems(mitMapPlaces, true, true);
                setMode(Mode.NO_SEARCH);

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                places = mitMapPlaces;
            }

            @Override
            public void failure(RetrofitError error) {
                MitMobileApplication.bus.post(new OttoBusEvent.RetrofitFailureEvent(error));
            }
        });

        return true;
    }

    public void setMode(@NonNull Mode mode) {
        if (this.mode == mode) return;

        this.mode = mode;

        recentsListview.setVisibility(View.GONE);
    }

    @Override
    public View getInfoContents(Marker marker) {
        if (marker.getSnippet() != null) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.mit_map_info_window, null);
            TextView topTextView = (TextView) view.findViewById(R.id.top_textview);
            TextView bottomTextView = (TextView) view.findViewById(R.id.bottom_textview);

            topTextView.setTextSize(16f);
            bottomTextView.setTextSize(14f);

            Gson gson = new Gson();
            MITMapPlace.MITMapPlaceSnippet snippet = gson.fromJson(marker.getSnippet(), MITMapPlace.MITMapPlaceSnippet.class);

            if (!TextUtils.isEmpty(snippet.getBuildingNumber())) {
                topTextView.setText("Building " + snippet.getBuildingNumber());
            } else {
                topTextView.setVisibility(View.GONE);
            }
            bottomTextView.setText(snippet.getName());

            return view;
        } else {
            return null;
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Gson gson = new Gson();

        MITMapPlace.MITMapPlaceSnippet snippet = gson.fromJson(marker.getSnippet(), MITMapPlace.MITMapPlaceSnippet.class);
//        String type = snippet.type;
//        int index = snippet.index;

        // TODO: Go to detail screen

        /*Intent intent = new Intent(getActivity(), TourStopActivity.class);
        intent.putExtra(Constants.Tours.TOUR_KEY, tour);
        intent.putExtra(Constants.Tours.TOUR_STOP_TYPE, type);
        intent.putExtra(Constants.Tours.CURRENT_MAIN_LOOP_STOP, index);
        if (type.equals(Constants.Tours.SIDE_TRIP)) {
            intent.putExtra(Constants.Tours.TOUR_STOP, tour.getStops().get(index));
        }
        startActivity(intent);*/
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();

        inflater.inflate(R.menu.menu_search, menu);

        super.onCreateOptionsMenu(menu, inflater);

        MenuItem menuItem = menu.findItem(R.id.search);
        searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return performSearch(searchView, this, s);
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return searchTextChanged(searchView, this, s);
            }
        });


        MenuItemCompat.setOnActionExpandListener(menuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                if (mode != Mode.NO_SEARCH) {
                    setMode(Mode.NO_SEARCH);
                }
                updateMapItems(new ArrayList(), true, true);
                places.clear();
                return true;
            }
        });

        searchView.setQueryHint(getString(R.string.maps_search_hint));

        View searchPlate = searchView.findViewById(android.support.v7.appcompat.R.id.search_plate);

        //noinspection ConstantConditions IntelliJ/AndroidStudio incorrectly thinks this can never be null.
        assert searchPlate != null;

        searchTextView = (TextView) searchPlate.findViewById(android.support.v7.appcompat.R.id.search_src_text);

        assert searchTextView != null;

        searchTextView.setTextColor(Color.WHITE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.home) {
            if (this.mode != Mode.NO_SEARCH) {
                this.setMode(Mode.NO_SEARCH);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mode == null) {
            this.setMode(Mode.getDefault());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        animateFABs();
    }

    @Override
    public void switchViews(boolean toList) {
        Intent intent = new Intent(getActivity(), MapSearchResultActivity.class);
        //noinspection unchecked
        intent.putParcelableArrayListExtra("places", (ArrayList) places);
        startActivityForResult(intent, 100);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            int position = data.getIntExtra("position", -1);
            selectMarker(position);
            mitMapView.setToDefaultBounds(false, 0);
        }
    }

    public enum Mode {
        /**
         * This is the parimary or default screen that is shown with our quick dial and favorites
         */
        NO_SEARCH(false),
        /**
         * We are showing a list, but it is "blank" as no search has been performed.
         */
        LIST_BLANK(true),
        /**
         * We are showing a list, but it is "blank" as no data was found. (NYI?)
         */
        LIST_NODATA(true),
        /**
         * We are showing a list, with valid data.
         */
        LIST_DATA(true);

        private final boolean listViewVisible;

        Mode(boolean listViewVisible) {
            this.listViewVisible = listViewVisible;
        }

        public boolean isListViewVisible() {
            return listViewVisible;
        }

        public static Mode getDefault() {
            return NO_SEARCH;
        }
    }
}
