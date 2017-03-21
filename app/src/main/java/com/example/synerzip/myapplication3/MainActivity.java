package com.example.synerzip.myapplication3;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.CalendarContract;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Comparator;
import java.util.Calendar;
import java.util.Map;

import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity{
    boolean currentEventFoundFlag = false;
    GoogleAccountCredential mCredential;
    private static final String[] SCOPES = {CalendarScopes.CALENDAR};
    String previousTitle = "";
    Runnable refresh;
    private static String TAG = MainActivity.class.getSimpleName();
    String emailID = "meeting.room.guide@synerzip.com";
    String meetingRoomName = "";
    String organizer = "";
    private int REQUEST_AUTHORIZATION = 11;
    JSONObject emp_obj = new JSONObject();
    ArrayList<String> res =  new ArrayList<>();
    Intent schedule;
    Date currentTime,endDate;
    Button button;
    String first_name,last_name,URL;
    int i,j,p;
    ArrayList<String> buttonText,data;
    String displayName,buttonName,meetingroomID,beginTime,endTime,timeStart,timeEnd;
    Date startdate = new Date();
    EventAttendee[] attendees;
    String calendarId = "primary";
    Event event;
    ArrayList<String> emp_names = new ArrayList<>();
    public class CalendarList {
        String calendarName;
        String title;
        Date start;
        Date end;
        String organizer;
    }
    List<CalendarList> calendarEventList;
    List<CalendarList> calendarData;
    private static final String DATE_TIME_FORMAT = "h:mm a";
    Map<String, String> calendarResources = new HashMap<String, String>();
    Map<String, String> meetingRoomNames = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        URL = "http://staging.hrms.synerzip.in/symfony/web/index.php/api/directory";

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                final Intent mainIntent = getIntent();
                finish();
                startActivity(mainIntent);


            }
        }, 300000);
        AsyncTaskRunner1 task = new AsyncTaskRunner1();
        task.execute();
        Toast.makeText(MainActivity.this, "Done", Toast.LENGTH_SHORT).show();
        System.out.println("*********************" + res);
        mCredential = GoogleAccountCredential.usingOAuth2(getBaseContext(), Arrays.asList(SCOPES));
        mCredential.setSelectedAccountName(emailID);

        /********** Display all calendar names in drop down START ****************/

        String[] EVENT_PROJECTION = new String[] {
                CalendarContract.Calendars._ID,                           // 0
                CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
                CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
        };
        buttonText = new ArrayList<>();

        final ArrayList<String> calendarNames = new ArrayList<String>();
        ContentResolver contentResolver = this.getContentResolver();
        Cursor cursorThirdFloor = contentResolver.query(Uri.parse("content://com.android.calendar/calendars"),
                EVENT_PROJECTION, "calendar_displayName LIKE '%3F%'", null, "name ASC");
        Cursor cursorFourthFloor = contentResolver.query(Uri.parse("content://com.android.calendar/calendars"),
                EVENT_PROJECTION, "calendar_displayName LIKE '%4F%'", null, "name ASC");


        if (cursorThirdFloor != null) {
            while (cursorThirdFloor.moveToNext()) {
                String displayNameOld = cursorThirdFloor.getString(2);
                displayName = displayNameOld.replace("3F", "");// Needs to update this logic for some other pattern
                buttonName = displayName;
                String[] separate = buttonName.split("-");
                buttonText.add("3F - "+separate[0]);
                displayName = "3F -" + displayName;

                meetingRoomNames.put(displayName, displayNameOld);
                String id = cursorThirdFloor.getString(0);
                String ACCOUNT_NAME = cursorThirdFloor.getString(1);
                String OWNER_ACCOUNT = cursorThirdFloor.getString(3);

                calendarResources.put(displayName, OWNER_ACCOUNT);
                System.out.println("calendar name = " + displayName + " id = " + id + " ACCOUNT_NAME = " + ACCOUNT_NAME + " OWNER_ACCOUNT = " + OWNER_ACCOUNT);
                calendarNames.add(displayName);

            }
            cursorThirdFloor.close();

            // To sort with case sensitive uncomment below
//            Collections.sort(calendarNames, CALENDAR_NAME_ORDER);
        }
            System.out.println("buttontext  = "+buttonText);

        if (cursorFourthFloor != null) {
            while (cursorFourthFloor.moveToNext()) {
                String displayNameOld = cursorFourthFloor.getString(2);
                displayName = displayNameOld.replace("4F", "");// Needs to update this logic for some other pattern
                displayName = "4F -" + displayName;
                meetingRoomNames.put(displayName, displayNameOld);
                String id = cursorFourthFloor.getString(0);
                String ACCOUNT_NAME = cursorFourthFloor.getString(1);
                String OWNER_ACCOUNT = cursorFourthFloor.getString(3);

                calendarResources.put(displayName, OWNER_ACCOUNT);

                System.out.println("calendar name = " + displayName + " id = " + id + " ACCOUNT_NAME = " + ACCOUNT_NAME + " OWNER_ACCOUNT = " + OWNER_ACCOUNT);

            }
            cursorFourthFloor.close();


            // To sort with case sensitive uncomment below
//            Collections.sort(calendarNames, CALENDAR_NAME_ORDER);
        }

        System.out.println("****************************"+displayName);
        // drop down adapter
//        Spinner spinner = (Spinner) findViewById(R.id.spinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, calendarNames) {

            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                Typeface externalFont = Typeface.createFromAsset(getAssets(), "DroidSans.ttf");
                ((TextView) v).setTypeface(externalFont);
                ((TextView) v).setTextSize(20);

                return v;
            }

            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);

                Typeface externalFont = Typeface.createFromAsset(getAssets(), "DroidSans.ttf");
                ((TextView) v).setTypeface(externalFont);
                ((TextView) v).setTextSize(20);

                return v;
            }
        };

        ///////
        int length = buttonText.size();
        int count = 0;
        TableLayout table = (TableLayout)findViewById(R.id.rooms);
        // for modifying data of Buttons runtime ***
        for( i = 0; i < table.getChildCount() && length != 0; i++) {
            View row = table.getChildAt(i);
            if (row instanceof TableRow) {
                // then, you can remove the the row you want...
                // for instance...
                final TableRow tableRow = (TableRow) row;
                for( j = 0; j < tableRow.getChildCount() && length != 0; j++) {
                    final View view1 = tableRow.getChildAt(j);
                    button = (Button) view1;
                    button.setTextSize(20);

                    // Set original Meeting Room Name in Button text //Commenting for time being
                    // button.setText(calendarNames.get(count));


                    final String roomName = calendarNames.get(count);
                    System.out.println("roomname"+roomName);

                    boolean isProjectorAvailable = false;

//                  String shortMeetingRoomName = getShortMeetingRoomName(meetingRoomName);
                    if (isProjectorAvailable(roomName)) {
                        isProjectorAvailable = true;
                    }

//                  String shortMeetingRoomName = getShortMeetingRoomName(roomName);

                    String size = giveMeetingRoomSize(roomName);
                    final String name = buttonText.get(count);
                    button.setText(name + "\n");

                    if (!size.isEmpty())
                        button.setText(button.getText() + "(" + size + ")");

                    if (isProjectorAvailable)
                        button.setText(button.getText() + "      (Projector)");

                    calendarEventList = getDataForListView(MainActivity.this, meetingRoomNames.get(calendarNames.get(count)).toString());
                    boolean isongoing = checkOngoingOrUpcomingMeetInHalfHour(calendarEventList);
                    System.out.println("calendarData=   "+calendarData);

//                  Button button = (Button) findViewById(R.id.room1);
                    if(isongoing) {
                        button.setBackgroundColor(Color.parseColor("#b30000"));
                    }
                    else {
                        button.setBackgroundColor(Color.parseColor("#80ff80"));
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(startdate);
                                int unroundedMinutes = calendar.get(Calendar.MINUTE);
                                int mod = unroundedMinutes % 30;
                                calendar.add(Calendar.MINUTE, mod < 8 ? -mod : -mod);
                                System.out.println("calendar"+calendar.getTime());
                                currentTime = calendar.getTime();
                                //  currentTime = new SimpleDateFormat(DATE_TIME_FORMAT).format(calendar.getTime());
                                System.out.println("current time string"+currentTime);
                                calendar.add(Calendar.MINUTE,30);
                                endDate = calendar.getTime();
                                String selectedRoomName = roomName;
                                meetingRoomName = selectedRoomName;
                                meetingroomID = calendarResources.get(meetingRoomName.toString());
                                System.out.println("Meetingroomname = "+meetingRoomName);
                                calendarEventList = readCalendar(MainActivity.this,meetingRoomNames.get(meetingRoomName));
                                //  Toast.makeText(getBaseContext(),"****"+meetingRoomName,Toast.LENGTH_SHORT).show();
                                System.out.println("calendarEventList = "+calendarEventList);
                                data = new ArrayList<>();
                                for (i = 0; i < calendarData.size(); i++){


                                    beginTime = new SimpleDateFormat(DATE_TIME_FORMAT).format(calendarData.get(i).start);
                                    System.out.println("begintime = "+beginTime);
                                    endTime = new SimpleDateFormat(DATE_TIME_FORMAT).format(calendarData.get(i).end);
                                    System.out.println("endtime = "+endTime);
                                    data.add(calendarData.get(i).title);
                                    data.add(beginTime);
                                    data.add(endTime);

                                }

                                timeStart = new SimpleDateFormat(DATE_TIME_FORMAT).format(currentTime);
                                timeEnd = new SimpleDateFormat(DATE_TIME_FORMAT).format(endDate);
                                //    System.out.println("*******"+data);

                                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create(); //Read Update
                                alertDialog.setTitle("Confirm your booking\n");
                                alertDialog.setMessage("Book "+name+"from "+timeStart+" to "+timeEnd);
                                alertDialog.setButton(Dialog.BUTTON_POSITIVE,"Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Write your code here to execute after dialog closed
                                        // Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();

                                        AsyncTaskRunner task = new AsyncTaskRunner();
                                        task.execute();
                                        Toast.makeText(MainActivity.this, "Meeting is scheduled", Toast.LENGTH_SHORT).show();
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                final Intent mainIntent = getIntent();
                                                finish();
                                                startActivity(mainIntent);

                                            }
                                        }, 60000);
                                                                       // finish();
                                    }

                                });
                                alertDialog.setButton(Dialog.BUTTON_NEGATIVE,"Edit", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Write your code here to execute after dialog closed
                                        // Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
                                        schedule = new Intent(getBaseContext(), ScheduleActivity.class);
                                        schedule.putExtra("message",data);
                                        schedule.putExtra("json", res);
                                        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$"+res);
                                        schedule.putExtra("roomName",meetingRoomName);
                                        schedule.putExtra(Intent.EXTRA_EMAIL,meetingroomID);
                                        startActivity(schedule);

                                    }

                                });
                                alertDialog.show();

                            }
                        });

                    }

                    length--;
                    count++;
                }

            }
        }

// Approch 1: This Is how we can diactivate remaining buttons on screen (deactivate= do not show on screen.)
        int numberOfColumns = 4, numberOfRows =3;
        int r = count / numberOfColumns;
        int c = count % numberOfColumns;

        for (int h = r; h <= numberOfRows; h++) {
//        // This is How we remove the table row
            View rowFromWhichCellNeedsToBeDeleted = table.getChildAt(h);
//		table.removeView(row);

            TableRow tableRowFromWhichCellNeedsToBeDeleted = (TableRow) rowFromWhichCellNeedsToBeDeleted;
            for (int i = c; i < numberOfColumns; i++) {
                if(tableRowFromWhichCellNeedsToBeDeleted!=null) {
                    View view1 = tableRowFromWhichCellNeedsToBeDeleted.getChildAt(c);
                    tableRowFromWhichCellNeedsToBeDeleted.removeView(view1);
                }
            }
        }

    }

    public boolean isProjectorAvailable(String meetingRoomName) {
        if (meetingRoomName.toLowerCase().contains("projector")) {
            return true;
        } else {
            return false;
        }
    }
    public String giveMeetingRoomSize(String meetingRoomName) {

        int index = meetingRoomName.toLowerCase().indexOf("(");
        int indexE = meetingRoomName.toLowerCase().indexOf(" seats");
        if (indexE == -1 || index == -1) {
            return "";
        }
        String size = meetingRoomName.toLowerCase().substring(index + 1, indexE);
        return size;
    }

    // Newly Added Start: Newly added Function for highlighting meeting rooms Red/Green

    public boolean checkOngoingOrUpcomingMeetInHalfHour(List<CalendarList> calendarEventList) {


        if (calendarEventList.size() == 0)
        {
            return false;
        }
        CalendarList chapter = calendarEventList.get(0);


        Calendar calInstanceCurrent = Calendar.getInstance(); // creates calendar
        calInstanceCurrent.setTime(new Date(new Date().getTime())); // sets calendar time/date

        Calendar calInstanceMeetingEventStart = Calendar.getInstance();
        calInstanceMeetingEventStart.setTime(new Date(chapter.start.getTime()));

        Calendar calInstanceMeetingEventEnd = Calendar.getInstance();
        calInstanceMeetingEventEnd.setTime(new Date(chapter.end.getTime()));

        if (!DateUtils.isToday(calInstanceMeetingEventStart.getTimeInMillis())) {
            calInstanceMeetingEventStart.set(calInstanceCurrent.get(Calendar.YEAR), calInstanceCurrent.get(Calendar.MONTH), calInstanceCurrent.get(Calendar.DAY_OF_MONTH));
            calInstanceMeetingEventEnd.set(calInstanceCurrent.get(Calendar.YEAR), calInstanceCurrent.get(Calendar.MONTH), calInstanceCurrent.get(Calendar.DAY_OF_MONTH));
        }
        long meetingStartTimeConvertedToTodayInMillis = calInstanceMeetingEventStart.getTimeInMillis();
        long meetingEndTimeConvertedToTodayInMillis = calInstanceMeetingEventEnd.getTimeInMillis();

        // Set current meeting attributes (bg colour etc).
        // As It is a Current Ongoing meeting Enable the Complaint Button.
        if (meetingStartTimeConvertedToTodayInMillis < calInstanceCurrent.getTimeInMillis() && meetingEndTimeConvertedToTodayInMillis > calInstanceCurrent.getTimeInMillis()) {

            return true;

        }

        // Code modification start
        // Check if there is a meeting in block of 30 mins if yes then mark room as BUSY(RED)
        // so that user will not be able to book it.

        // code control here means meeting is not ongoing
        long comparet = Integer.valueOf(calInstanceMeetingEventStart.get(Calendar.HOUR_OF_DAY)).
                compareTo(calInstanceCurrent.get(Calendar.HOUR_OF_DAY));
        long x = calInstanceCurrent.get(Calendar.HOUR_OF_DAY);
        long y = calInstanceMeetingEventStart.get(Calendar.HOUR_OF_DAY);

        if (comparet == 0)//Means meeting is in the current HOUR only
        {
            long minCurrent = calInstanceCurrent.get(Calendar.MINUTE);
            long minMeetingEventStart = calInstanceMeetingEventStart.get(Calendar.MINUTE);

            if ((minCurrent < minMeetingEventStart) && minMeetingEventStart < 30  && minCurrent < 30)
            {
                return true;
            }

            if ((minCurrent < minMeetingEventStart) && minMeetingEventStart > 30  && minCurrent > 30)
            {
                return true;
            }

        }
        // Code modification end

        long compare = Integer.valueOf(calInstanceMeetingEventStart.get(Calendar.HOUR_OF_DAY)).compareTo(calInstanceCurrent.get(Calendar.HOUR_OF_DAY));

        if (compare == 0)//Means Both meeting time in hours is same hence compare minutes
        {
            compare = Integer.valueOf(calInstanceMeetingEventStart.get(Calendar.MINUTE)).compareTo(calInstanceCurrent.get(Calendar.MINUTE));
        }

        if ((compare == 1 && currentEventFoundFlag == false) || (currentEventFoundFlag == true && previousTitle == chapter.title))// no meeting event is highlighted
        {

        }

        return false;
    }

    // Newly Added end: Newly added Function for highlighting meeting rooms Red/Green

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list_view_with_simple_adapter, menu);
        return true;
    }
*/
    public List<CalendarList> getDataForListView(MainActivity context, String calendarName) {
        List calendarList = readCalendar(context, calendarName);
        return calendarList;
    }

    public List readCalendar(Context context, String calendarName) {
        organizer = "";
        ContentResolver contentResolver = context.getContentResolver();
        calendarData = new ArrayList<CalendarList>();
        Cursor eventCursor = null;
        Cursor cursorThirdFloor = null;
        // while loading every calender we need to set Complaint button as Disabled as initial state.
//        ImageButton sendBtn = (ImageButton) findViewById(R.id.sendEmail);
//        sendBtn.setVisibility(View.GONE);

        try {
            // Fetch all events of selected calendar
            Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
            long now = new Date().getTime();


            Calendar calInstanceE = Calendar.getInstance(); // creates calendar
            calInstanceE.setTime(new Date(now));
            long currentHourOfDay = calInstanceE.get(Calendar.HOUR_OF_DAY);

            ContentUris.appendId(builder, now);
            ContentUris.appendId(builder, (now + (24 - currentHourOfDay) * DateUtils.HOUR_IN_MILLIS));

            final String[] projection = new String[]
                    {CalendarContract.Events.TITLE, CalendarContract.Events.DTSTART,
                            CalendarContract.Events.DTEND, CalendarContract.Events.ACCOUNT_NAME,
                            CalendarContract.Events.DURATION, CalendarContract.Events.ACCESS_LEVEL, CalendarContract.Events.ORGANIZER,
                            CalendarContract.Events.SELF_ATTENDEE_STATUS};

            eventCursor = contentResolver.query(
                    builder.build(), projection, CalendarContract.Instances.CALENDAR_DISPLAY_NAME + " = ?",
                    new String[]{"" + calendarName}, "DTSTART ASC");

            eventCursor.moveToFirst();
            do {
                CalendarList record = new CalendarList();

                String title = eventCursor.getString(0);
                final Date begin = new Date(eventCursor.getLong(1));
                Date end = new Date(eventCursor.getLong(2));
                final String accountName = eventCursor.getString(3);
                final String duration = eventCursor.getString(4);
                final Long accessLevel = eventCursor.getLong(5);

                final String selfAttendeeStatus = eventCursor.getString(7);

                // Check to hide declined events by Meeting room
                if (selfAttendeeStatus.equals("2")) {
                    continue;
                }

//                if(organizer.isEmpty())
//                {
                organizer = eventCursor.getString(6);
//                }

                if (duration != null) {
                    // Calculation Logic for Meeting end time
                    int durationInSeconds = Integer.parseInt(duration.substring(1, duration.length() - 1));
                    Calendar calInstance = Calendar.getInstance(); // creates calendar
                    calInstance.setTime(new Date(eventCursor.getLong(1))); // sets calendar time/date
                    calInstance.add(Calendar.SECOND, durationInSeconds); // Add Seconds from Duration
                    end = calInstance.getTime();
                }

                if (accessLevel == 2) //Means private Meeting
                {
                    title = "Busy";
                }

                if (title.isEmpty()) // Untitled event
                {
                    title = "Untitled event";
                }

                System.out.println("Title: " + title + " Begin: " + begin + " End: " + end +
                        " accountName: " + accountName + " accessLevel: " + accessLevel);

                record.title = title;
                record.start = begin;
                record.end = end;
                record.calendarName = accountName;
                record.organizer = organizer;

                calendarData.add(record);
            } while (eventCursor.moveToNext());

            Collections.sort(calendarData, SENIORITY_ORDER);

        } catch (Exception ex) {

        } finally {
            try {
                if (eventCursor != null && !eventCursor.isClosed()) {
                    eventCursor.close();
                }

                if (cursorThirdFloor != null && !cursorThirdFloor.isClosed()) {
                    cursorThirdFloor.close();
                }

            } catch (Exception ex) {
            }
        }

        return calendarData;
    }

    static final Comparator<CalendarList> SENIORITY_ORDER = new Comparator<CalendarList>() {
        public int compare(CalendarList e1, CalendarList e2) {
            int compare = e1.start.compareTo(e2.start);
            if (compare == 0) {//Means both events start time is different
                return compare;
            } else {
                Calendar calInstanceE1 = Calendar.getInstance(); // creates calendar
                calInstanceE1.setTime(new Date(e1.start.getTime())); // sets calendar time/date

                Calendar calInstanceE2 = Calendar.getInstance(); // creates calendar
                calInstanceE2.setTime(new Date(e2.start.getTime())); // sets calendar time/date

                compare = Integer.valueOf(calInstanceE1.get(Calendar.HOUR_OF_DAY)).compareTo(calInstanceE2.get(Calendar.HOUR_OF_DAY));
                if (compare == 0)//Means Both meeting time in hours is same hence compare minutes
                {
                    compare = Integer.valueOf(calInstanceE1.get(Calendar.MINUTE)).compareTo(calInstanceE2.get(Calendar.MINUTE));
                }
            }

            return compare;
        }
    };

    // sort calendar names alphabetically
    static final Comparator<String> CALENDAR_NAME_ORDER = new Comparator<String>() {
        public int compare(String e1, String e2) {
            return e1.compareToIgnoreCase(e2);
        }
    };
    private class AsyncTaskRunner extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            createEvent(mCredential);

            return null;
        }
    }

    public void createEvent(GoogleAccountCredential mCredential) {

        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        com.google.api.services.calendar.Calendar service = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, mCredential)
                .setApplicationName("R_D_Location Calendar")
                .build();

        event = new Event()
                .setSummary("Meeting Room Guide created meeting")
                .setLocation(meetingRoomName)
                .setDescription("New test event 1");

        DateTime startDateTime = new DateTime(currentTime);
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("Asia/Kolkata");
        event.setStart(start);

        DateTime endDateTime = new DateTime(endDate);
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("Asia/Kolkata");
        event.setEnd(end);

        String[] recurrence = new String[]{"RRULE:FREQ=DAILY;COUNT=1"};
        event.setRecurrence(Arrays.asList(recurrence));

        attendees = new EventAttendee[]{
                //  new EventAttendee().setEmail("kiran.bodakhe@synerzip.com"),
                new EventAttendee().setEmail(meetingroomID),

        };

        event.setAttendees(Arrays.asList(attendees));


        EventReminder[] reminderOverrides = new EventReminder[]{
                new EventReminder().setMethod("email").setMinutes(24 * 60),
                new EventReminder().setMethod("popup").setMinutes(10),
        };
        Event.Reminders reminders = new Event.Reminders()
                .setUseDefault(false)
                .setOverrides(Arrays.asList(reminderOverrides));
        event.setReminders(reminders);

        try {
            event = service.events().insert(calendarId, event).setSendNotifications(true).execute();
        } catch (UserRecoverableAuthIOException e) {
            startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("Event created: %s\n", event.getHtmlLink());

        //   Toast.makeText(getBaseContext(),"Meeting is scheduled",Toast.LENGTH_SHORT).show();
    }

    class AsyncTaskRunner1 extends AsyncTask<String, String, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(String... strings) {
            StringRequest sr = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG, response);

                    try {

                        JSONArray arr = new JSONArray(response);
                        // System.out.println("fvkfbkdnlc"+arr);
                        System.out.println("dgihcdbcjb" + arr.length());
                        for (p = 0; p < arr.length(); p++) {

                            emp_obj = arr.getJSONObject(p);
                            first_name = emp_obj.getString("emp_firstname");
                            last_name = emp_obj.getString("emp_lastname");
                            emp_names.add(first_name + " " + last_name);
                        }
                        res = emp_names;
                        System.out.println("emp_names" + emp_names);
                        Toast.makeText(MainActivity.this, "Done", Toast.LENGTH_SHORT).show();
                       // i = new Intent(getBaseContext(), ScheduleActivity.class);


                      //  startActivity(i);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(TAG, "Error: " + error.getMessage());
                    Log.d(TAG, "" + error.getMessage() + "," + error.toString());
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<String, String>();
                    headers.put("api-key", "0a9c00533933b1597c7dcd5169236f1d");
                    headers.put("Content-Type", "application/json");
                    return headers;
                }

            };

            AppController.getInstance(getBaseContext()).addToRequestQueue(sr);

            return res;
        }
    }
}


