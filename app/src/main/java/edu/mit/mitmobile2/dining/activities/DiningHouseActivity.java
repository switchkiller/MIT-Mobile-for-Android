package edu.mit.mitmobile2.dining.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.format.DateFormat;
import android.view.Menu;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import edu.mit.mitmobile2.Constants;
import edu.mit.mitmobile2.R;
import edu.mit.mitmobile2.dining.adapters.HouseMenuPagerAdapter;
import edu.mit.mitmobile2.dining.model.MITDiningHouseDay;
import edu.mit.mitmobile2.dining.model.MITDiningHouseVenue;
import edu.mit.mitmobile2.dining.model.MITDiningMeal;
import edu.mit.mitmobile2.shared.logging.LoggingManager;

public class DiningHouseActivity extends AppCompatActivity  {

    @InjectView(R.id.house_image_view)
    ImageView houseImageView;
    @InjectView(R.id.house_name_text_view)
    TextView houseNameTextView;
    @InjectView(R.id.house_hours_text_view)
    TextView houseHoursTextView;
    @InjectView(R.id.dining_house_menu_viewpager)
    ViewPager houseMenuViewpager;
    @InjectView(R.id.date_text_text_view)
    TextView dateTextView;
    @InjectView(R.id.info_text_view)
    TextView infoTextView;

    @OnClick(R.id.info_image_view)
    public void gotoHouseInfo() {
        Intent intent = new Intent(this, DiningHouseInfoActivity.class);
        intent.putExtra(Constants.Dining.HOUSE_INFO, venue);
        startActivity(intent);
    }

    @OnClick(R.id.forward_image_view)
    public void goToNextHouseMeal() {
        if (houseMenuViewpager.getCurrentItem() < (diningMeals.size() - 1)) {
            houseMenuViewpager.setCurrentItem(houseMenuViewpager.getCurrentItem() + 1);
        }
    }

    @OnClick(R.id.back_image_view)
    public void goToPreviousHouseMeal() {
        if (houseMenuViewpager.getCurrentItem() > 0) {
            houseMenuViewpager.setCurrentItem(houseMenuViewpager.getCurrentItem() - 1);
        }
    }

    private HouseMenuPagerAdapter houseMenuPagerAdapter;
    private List<MITDiningMeal> diningMeals;
    private MITDiningHouseVenue venue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dining_house);
        ButterKnife.inject(this);

        venue = getIntent().getParcelableExtra(Constants.Dining.DINING_HOUSE);

        setTitle(venue.getShortName());

        buildHouseMenuPager();

        houseNameTextView.setText(venue.getName());
        dateTextView.setText("Today, " + getCurrentDate());

        try {
            Picasso.with(this).load(venue.getIconURL()).placeholder(R.drawable.grey_rect).into(houseImageView);
        } catch (NullPointerException e) {
            Picasso.with(this).load(R.drawable.grey_rect).placeholder(R.drawable.grey_rect).into(houseImageView);
        }
    }



    private void buildHouseMenuPager() {
        diningMeals = new ArrayList<>();
        for (MITDiningHouseDay diningHouseDay : venue.getMealsByDay()) {
            for (MITDiningMeal mitDiningMeal : diningHouseDay.getMeals()) {
                mitDiningMeal.setHouseDay(diningHouseDay);
                diningMeals.add(mitDiningMeal);
            }
        }
        houseMenuPagerAdapter = new HouseMenuPagerAdapter(getFragmentManager(), diningMeals);
        houseMenuViewpager.setAdapter(houseMenuPagerAdapter);
        houseMenuViewpager.setCurrentItem(findCurrentMeal(diningMeals));
        houseMenuViewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (diningMeals.get(position).getHouseDay().getDateString().equals(getCurrentDate())) {
                    dateTextView.setText("Today, " + formatSimpletDate(diningMeals.get(position).getHouseDay().getDateString()));
                } else {
                    dateTextView.setText(formatDate(diningMeals.get(position).getHouseDay().getDateString()));
                }

                infoTextView.setText(diningMeals.get(position).getName() + " "
                        + DateFormat.format("h:mm a", formatMealTime(diningMeals.get(position).getStartTimeString())) + "-"
                        + DateFormat.format("h:mm a", formatMealTime(diningMeals.get(position).getEndTimeString())));
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private int findCurrentMeal(List<MITDiningMeal> meals) {
        int index = 0;
        String currentDate = getCurrentDate();

        for (int i = 0; i < meals.size() - 1; i++) {
            if (currentDate.equals(meals.get(i).getHouseDay().getDateString())) {
                String startTime = meals.get(i).getStartTimeString().replace(":", "");
                String endTime = meals.get(i).getEndTimeString().replace(":", "");
                if ((Integer.parseInt(getCurrentTime()) >= Integer.parseInt(startTime)) && (Integer.parseInt(getCurrentTime()) <= Integer.parseInt(endTime))) {
                    index = i;
                    houseHoursTextView.setText("Open until " + DateFormat.format("h:mm a", formatMealTime(meals.get(index).getEndTimeString())));
                    houseHoursTextView.setTextColor(getResources().getColor(R.color.status_green));
                    break;
                } else if (Integer.parseInt(getCurrentTime()) < Integer.parseInt(startTime)){
                    index = i + 1;
                    houseHoursTextView.setText("Opens at " + DateFormat.format("h:mm a", formatMealTime(meals.get(index).getStartTimeString())));
                    houseHoursTextView.setTextColor(getResources().getColor(R.color.status_red));
                    break;
                } else if ((Integer.parseInt(getCurrentTime()) > Integer.parseInt(endTime))
                        && (!currentDate.equals(meals.get(i + 1).getHouseDay().getDateString()))) {
                    index = i;
                    houseHoursTextView.setText(getResources().getString(R.string.close_today));
                    houseHoursTextView.setTextColor(getResources().getColor(R.color.status_red));
                    break;
                } else {
                    continue;
                }
            }
        }

        return index;
    }

    private String getCurrentDate() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(cal.getTime());

        return date;
    }

    private String getCurrentTime() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
        String time = sdf.format(cal.getTime());

        return time;
    }


    private String formatDate(String dateString) {
        Date date = new Date();
        SimpleDateFormat formatedDate = new SimpleDateFormat("EEEE, LLL dd");
        SimpleDateFormat originalDate = new SimpleDateFormat("yyyy-MM-dd");

        try {
            date = originalDate.parse(dateString);
            System.out.println(date);
        } catch (ParseException e) {
            LoggingManager.Timber.e(e, "___________DateFormatError___________");
        }

        String formattedString = formatedDate.format(date);

        return formattedString;
    }

    private String formatSimpletDate(String dateString) {
        Date date = new Date();
        SimpleDateFormat formatedDate = new SimpleDateFormat("LLL dd");
        SimpleDateFormat originalDate = new SimpleDateFormat("yyyy-MM-dd");

        try {
            date = originalDate.parse(dateString);
            System.out.println(date);
        } catch (ParseException e) {
            LoggingManager.Timber.e(e, "___________DateFormatError___________");
        }

        String formattedString = formatedDate.format(date);

        return formattedString;
    }


    private Date formatMealTime(String timeString) {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        try {
            date = format.parse(timeString);
            System.out.println(date);
        } catch (ParseException e) {
            LoggingManager.Timber.e(e, "___________DateFormatError___________");
        }
        return date;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dining_house, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_filter) {
            Intent intent = new Intent(this, FiltersActivity.class);
            startActivity(intent);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}