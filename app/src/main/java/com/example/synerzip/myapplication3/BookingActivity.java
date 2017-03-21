package com.example.synerzip.myapplication3;

/**
 * Created by synerzip on 26/8/16.
 */
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
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

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by synerzip on 11/8/16.
 */
public class BookingActivity extends Activity {

    GoogleAccountCredential mCredential;
    private static final String[] SCOPES = {CalendarScopes.CALENDAR};
    String accountName = "meeting.room.guide@synerzip.com";
    private int REQUEST_AUTHORIZATION = 11;
    SimpleDateFormat format = new SimpleDateFormat("hh:mm a");
    Button cancel,book;
    String timeSlot,URL,roomName;
    AutoCompleteTextView empNames;
    TextView QuickBook;
    private static String TAG = MainActivity.class.getSimpleName();
    RadioGroup durationGroup;
    Calendar calendar = Calendar.getInstance();
    RadioButton duration1,duration2,duration3;
    Date date  = new Date();
    Date date1,startTime,endTime,selectedTime;
    String meetingRoomID,temp;
    EventAttendee[] attendees;
    String calendarId = "primary";
    ArrayList<String> res = new ArrayList<>();
    Event event;
    Date d1,d2,d3;
    String d11,d12,d13;
    String first_name,last_name;
    ArrayList<String> eventList,UATimeslots,emp_names;
    private static final String DATE_TIME_FORMAT = "h:mm a";
    int i = 0,j = 0,p=0;

    ArrayList<String> timeSlots = new ArrayList<String>(Arrays.asList("10:00 AM","10:30 AM","11:00 AM","11:30 AM","12:00 PM","12:30 PM","1:00 PM","1:30 PM","2:00 PM","2:30 PM"
            ,"3:00 PM","3:30 PM","4:00 PM","4:30 PM","5:00 PM","5:30 PM","6:00 PM","6:30 PM","7:00 PM","7:30 PM","8:00 PM",
            "8:30 PM","9:00 PM","9:30 PM","10:00 PM"));

   /* String empNamesList[]= { "Salil Khedkar","Tushar Bende","Vishakha Korade","Sachin Ghare","Nikhil Waykole","Sushil Shinde","Sujith Sudhakaran","Sneha Jagdale ",
            "Himanshu Phirke","Zubair Pathan","Tanvi Shah","Medha Gokhale","Kiran Bodakhe","Nagmani Prasad ","Avnish Kumar","Sandip Nirmal ","Shaila Pawar ",
            "Abhishek Bhattacharyya","Atul Moglewar","Sidharam Teli","Fameeda Tamboli","Dheeraj Koshti","Amit Joshi","Prasanna Barate","Amol Wagh",
            "Yogesh Mandhare","Kunjan Thakkar","Umesh Kadam","Upasana kumari","Sachin Avhad","Yuvraj Patel","Hussain Pithawala","Vinayak Joglekar"
    };*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        mCredential = GoogleAccountCredential.usingOAuth2(getBaseContext(), Arrays.asList(SCOPES));
        mCredential.setSelectedAccountName(accountName);

        QuickBook = (TextView) findViewById(R.id.textView);
        durationGroup = (RadioGroup) findViewById(R.id.durationGroup);
        cancel = (Button) findViewById(R.id.cancelButton);
        book = (Button) findViewById(R.id.bookButton);
        duration1 = (RadioButton) findViewById(R.id.duration1);
        duration2 = (RadioButton) findViewById(R.id.duration2);
        duration3 = (RadioButton) findViewById(R.id.duration3);
        empNames = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        eventList = new ArrayList<>();
        UATimeslots = new ArrayList<>();
        emp_names = new ArrayList<>();

        Intent getData = getIntent();
        res = getData.getStringArrayListExtra("json");
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"+res);
        timeSlot = getData.getStringExtra("timeSlot");
        roomName = getData.getStringExtra("roomName");
        meetingRoomID = getData.getStringExtra(Intent.EXTRA_EMAIL);
        eventList = getData.getStringArrayListExtra("events");
        System.out.println("eventList = "+eventList);
        URL = "http://staging.hrms.synerzip.in/symfony/web/index.php/api/directory";
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getBaseContext(),android.R.layout.simple_list_item_1,res);
        empNames.setAdapter(adapter);
        empNames.setThreshold(1);

        try {

            selectedTime = format.parse(timeSlot);

        } catch (ParseException e) {

            e.printStackTrace();
        }
        date1 = new Date(date.getYear(),date.getMonth(),date.getDate(),selectedTime.getHours(),selectedTime.getMinutes());
       // calendar.set(Calendar.DATE,date.setTime(selectedTime));
        calendar.setTime(date1);

        System.out.println("addition = "+calendar.getTime());
        startTime = calendar.getTime();
        for(i = 0; i < eventList.size(); i++){

            if(!eventList.get(i).equals("Available - Click to book")) {
                try {
                    Date temp1 = format.parse(timeSlots.get(i+1));

                    temp = new SimpleDateFormat(DATE_TIME_FORMAT).format(temp1);
                    UATimeslots.add(temp);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("UATimesslotes "+UATimeslots);

        for(j = 0; j < UATimeslots.size(); j++){
            calendar.add(Calendar.MINUTE,30);
            d1 = calendar.getTime();
            d11 = new SimpleDateFormat(DATE_TIME_FORMAT).format(d1);
            calendar.add(Calendar.MINUTE,-30);
            calendar.add(Calendar.HOUR,1);
            d2 = calendar.getTime();
            d12 = new SimpleDateFormat(DATE_TIME_FORMAT).format(d2);
            calendar.add(Calendar.HOUR,-1);
            calendar.add(Calendar.MINUTE,30);
            calendar.add(Calendar.HOUR,1);
            d3 = calendar.getTime();
            d13 = new SimpleDateFormat(DATE_TIME_FORMAT).format(d3);
            calendar.add(Calendar.HOUR,-1);
            calendar.add(Calendar.MINUTE,-30);


          /*  if(d11.equals(UATimeslots.get(j))){

                 duration1.setEnabled(false);

            }*/
            if(d12.equals(UATimeslots.get(j))){

                duration2.setEnabled(false);
                duration3.setEnabled(false);

            }
            else if (d13.equals(UATimeslots.get(j))){

                duration3.setEnabled(false);
            }
        }




        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });
        book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int selectedID = durationGroup.getCheckedRadioButtonId();
                if(selectedID == duration1.getId()){

                    calendar.add(Calendar.MINUTE,30);
                    endTime = calendar.getTime();
                }
                else if (selectedID == duration2.getId()){

                    calendar.add(Calendar.HOUR,1);
                    endTime = calendar.getTime();

                }
                else {

                    calendar.add(Calendar.HOUR,1);
                    calendar.add(Calendar.MINUTE,30);
                    endTime = calendar.getTime();
                }
                AsyncTaskRunner task = new AsyncTaskRunner();
                task.execute();
                Toast.makeText(BookingActivity.this, "Meeting is scheduled", Toast.LENGTH_SHORT).show();
                Intent gotoMain = new Intent(getBaseContext(),MainActivity.class);

               // startActivityForResult(gotoMain,2);

                startActivity(gotoMain);


                /*@Override
                protected void onActivityResult(int requestCode, int resultCode, Intent data) {
                    super.onActivityResult(requestCode, resultCode, data);
                    if(resultCode==RESULT_OK){
                        Intent refresh = new Intent(this, inboxlist.class); //inboxlist is activity which list the read and unread messages
                        startActivity(refresh);
                        this.finish();
                    }
                }*/
               // handler.post(timedTask);

               /* AlertDialog alertDialog = new AlertDialog.Builder(BookingActivity.this).create();
                alertDialog.setTitle("Meeting Details :");
                LinearLayout layout = new LinearLayout(getBaseContext());
                layout.setOrientation(LinearLayout.VERTICAL);
                TextView meetingName = new TextView(getBaseContext());
                meetingName.setTextColor(Color.parseColor("#000000"));
                meetingName.setText("Meeting Name : "+empNames.getText().toString()+"'s Meeting");
                layout.addView(meetingName);
                TextView room = new TextView(getBaseContext());
                room.setText("Meeting Room : "+roomName);
                room.setTextColor(Color.parseColor("#000000"));
                layout.addView(room);
                TextView time = new TextView(getBaseContext());
                time.setText("Meeting Time : "+startTime+" - "+endTime);
                time.setTextColor(Color.parseColor("#000000"));
                layout.addView(time);

                alertDialog.setView(layout);
                alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to execute after dialog closed
                        Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
                    }
                });

                alertDialog.show();*/

            }
        });
       /* @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if(resultCode==RESULT_OK){
                Intent refresh = new Intent(this, MainActivity.class);
                startActivity(refresh);
                this.finish();
            }
        }*/
    }
    Handler handler = new Handler();
    Runnable timedTask = new Runnable(){

        @Override
        public void run() {
            handler.postDelayed(timedTask, 1000);
        }};


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
                .setSummary(empNames.getText().toString()+"'s Meeting")
                .setLocation(roomName)
                .setDescription("New test event 1");

        DateTime startDateTime = new DateTime(startTime);
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("Asia/Kolkata");
        event.setStart(start);

        DateTime endDateTime = new DateTime(endTime);
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("Asia/Kolkata");
        event.setEnd(end);

        String[] recurrence = new String[]{"RRULE:FREQ=DAILY;COUNT=1"};
        event.setRecurrence(Arrays.asList(recurrence));

        attendees = new EventAttendee[]{
               //  new EventAttendee().setEmail("kiran.bodakhe@synerzip.com"),
                  new EventAttendee().setEmail(meetingRoomID),

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

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }
}
