package com.example.hitchikersguide;

import androidx.appcompat.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Save List Activity holds the list of saved images
 * Includes fragment to view item details
 * Allows user to remove from list and database
 *
 * @author Brianna Guerin
 * @author Jenne Stamplecoskie
 */
public class SavedList extends BaseActivity {
    private ArrayList<SpacePic> pictures = new ArrayList<>();
    private SQLiteDatabase myDB;
    String imgDate, imgTitle, imgURL, imgDetails, imgHDURL;
    SpacePic pic;
    static boolean isTablet;

    /**
     * On Create Function initializes widgets and listeners
     *
     * @param savedInstanceState - the state that the app was last saved in
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the Main Activity layout into the Base activity frame
        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_saved_list, contentFrameLayout);

        isTablet = findViewById(R.id.fragmentLocation) != null;

        ListView imgList = findViewById(R.id.SL_ListOfImages);
        MyAdapter myAdapter = new MyAdapter();
        imgList.setAdapter(myAdapter);
        myAdapter.notifyDataSetChanged();
        loadSavedPics();

        Intent passImg = getIntent();
        imgTitle = passImg.getStringExtra("Title");
        imgDate = passImg.getStringExtra("Date");
        imgURL = passImg.getStringExtra("URL");
        imgHDURL = passImg.getStringExtra("HDURL");
        imgDetails = passImg.getStringExtra("Details");

        if (imgTitle != null) {
            ContentValues newRowValues = new ContentValues();
            newRowValues.put(MyDBOpener.COL_DATE, imgDate);
            newRowValues.put(MyDBOpener.COL_URL, imgURL);
            newRowValues.put(MyDBOpener.COL_HDURL, imgHDURL);
            newRowValues.put(MyDBOpener.COL_TITLE, imgTitle);
            newRowValues.put(MyDBOpener.COL_DETAIL, imgDetails);
            long newID = myDB.insert(MyDBOpener.TABLE_NAME, null, newRowValues);

            pic = new SpacePic(imgDate, imgTitle, imgURL, imgHDURL, imgDetails);
            pictures.add(pic);
            myAdapter.notifyDataSetChanged();
        }

        // Get details of an item on the list
        imgList.setOnItemClickListener((parent, view, position, id) -> {

            Bundle dataToPass = new Bundle();
            dataToPass.putString("Date", pictures.get(position).imgDate);
            dataToPass.putString("Title", pictures.get(position).imgTitle);
            dataToPass.putString("URL", pictures.get(position).imgURL);
            dataToPass.putString("HDURL", pictures.get(position).imgHDURL);
            dataToPass.putString("Details", pictures.get(position).imgDetails);

            if (isTablet) {
                DetailsFragment dFragment = new DetailsFragment();
                dFragment.setArguments(dataToPass);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentLocation, dFragment).commit();
            }
            else {
                Intent activityEM = new Intent(this, EmptyActivity.class);
                activityEM.putExtras(dataToPass);
                startActivity(activityEM);
            }
        });

        // Remove an item from the list
        imgList.setOnItemLongClickListener(
            // Create a Dialog
            (parent, view, position, id) -> {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle(getResources().getString(R.string.SL_Alert_Title))

                    // Message
                    .setMessage(getResources().getString(R.string.SL_Alert_msg1) + position + "\n"
                            + getResources().getString(R.string.SL_Alert_msg2) + id)

                    // Yes Action
                    .setPositiveButton(R.string.yes, (click, arg) -> {
                        pic = pictures.get(position);
                        pictures.remove(position);
                        deleteSpacePic(pic);
                        myAdapter.notifyDataSetChanged();
                        if (isTablet) {
                            this.getSupportFragmentManager().beginTransaction()
                                    .remove(getSupportFragmentManager().findFragmentById(R.id.fragmentLocation))
                                    .commit();
                        }
                    })

                    // No action
                    .setNegativeButton(R.string.no, (click, arg) -> { })

                    //Show the dialog
                    .create().show();
                return true;
            } );
    }

    /**
     * Loads the pictures saved in the database into the image list
     */
    private void loadSavedPics() {
        Cursor results;
        SpacePic curPic;
        // Connect to DB
        MyDBOpener dbOpen = new MyDBOpener(this);
        myDB = dbOpen.getWritableDatabase();

        // list of columns
        String[] columns = {MyDBOpener.COL_ID, MyDBOpener.COL_DATE, MyDBOpener.COL_URL,
                            MyDBOpener.COL_HDURL, MyDBOpener.COL_TITLE, MyDBOpener.COL_DETAIL};
        // get all entries
        results = myDB.query(false, MyDBOpener.TABLE_NAME, columns, null,
                null, null, null, null, null);

        // Get column indices
        int idColIdx = results.getColumnIndex(MyDBOpener.COL_ID);
        int dateColIdx = results.getColumnIndex(MyDBOpener.COL_DATE);
        int urlColIdx = results.getColumnIndex(MyDBOpener.COL_URL);
        int hdurlColIdx = results.getColumnIndex(MyDBOpener.COL_HDURL);
        int titleColIdx = results.getColumnIndex(MyDBOpener.COL_TITLE);
        int detailColIdx = results.getColumnIndex(MyDBOpener.COL_DETAIL);


        // Iterate over the results, return true if there is a next item:
        while (results.moveToNext()) {
            // Create an image and add it to the arrayList
            curPic = new SpacePic(results.getLong(idColIdx), results.getString(dateColIdx),
                    results.getString(urlColIdx));
            curPic.setHDURL(results.getString(hdurlColIdx));
            curPic.setTitle(results.getString(titleColIdx));
            curPic.setDetails(results.getString(detailColIdx));
            pictures.add(curPic);
        }
        results.close();
    }
    protected void deleteSpacePic(SpacePic pic) {
        myDB.delete(MyDBOpener.TABLE_NAME, MyDBOpener.COL_ID + "= ?", new String[] {Long.toString(pic.getImgID())});
    }

    /**
     * Adapter Class for ListView extends BaseAdapter
     */
    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return pictures.size();
        }

        @Override
        public Object getItem(int position) {

            return pictures.get(position);
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        @Override
        public View getView(int position, View myView, ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();

            // make a new row
            myView = inflater.inflate(R.layout.img_list_row, parent, false);

            //set text for new row
            TextView dateView = myView.findViewById(R.id.IL_Date);
            dateView.setText(pictures.get(position).imgDate);

            TextView urlView = myView.findViewById(R.id.IL_URL);
            urlView.setText(pictures.get(position).imgURL);

            TextView titleView = myView.findViewById(R.id.IL_Title);
            titleView.setText(pictures.get(position).imgTitle);

            return myView;
        }
    }
}