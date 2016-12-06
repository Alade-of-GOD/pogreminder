package com.studio.pogworld.pogreminder;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;

public class RemindersActivity extends AppCompatActivity {

    private ListView mListView;
    private RemindersDbAdapter mDbAdapter;
    private RemindersSimpleCursorAdapter mCursorAdapter;
    //MediaPlayer alarmSong;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_launcher);
        mListView = (ListView) findViewById(R.id.reminders_list_view);
        mListView.setDivider(null);
        //alarmSong = MediaPlayer.create(RemindersActivity.this, R.raw.blow);
        mDbAdapter = new RemindersDbAdapter(this);
        mDbAdapter.open();
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int masterListPosition, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(RemindersActivity.this);
                ListView modeListView = new ListView(RemindersActivity.this);
                String[] modes = new String[] { "Edit Reminder","Scedule Reminder", "Delete Reminder" };
                ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(RemindersActivity.this, android.R.layout.simple_list_item_2, android.R.id.text1, modes);
                modeListView.setAdapter(modeAdapter);
                builder.setView(modeListView);
                final Dialog dialog = builder.create();
                dialog.show();
                modeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//edit reminder
                        int nId = getIdFromPosition(masterListPosition);
                        final Reminder reminder = mDbAdapter.fetchReminderById(nId);
                        if (position == 0) {
                            fireCustomDialog(reminder);
//delete reminder
                        } else if(position == 2){
                            mDbAdapter.deleteReminderById(getIdFromPosition(masterListPosition));
                            mCursorAdapter.changeCursor(mDbAdapter.fetchAllReminders());
                        }
                        else {
                            final Date today = new Date();
                            DatePickerDialog.OnDateSetListener listener_1 = new DatePickerDialog.OnDateSetListener(){

                                @Override
                                public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                    final Calendar alarmDay = Calendar.getInstance();
                                    alarmDay.set(year, Integer.parseInt(null));
                                    alarmDay.set(Calendar.MONTH, month);
                                    alarmDay.set(Calendar.DAY_OF_MONTH, day);
                                    scheduleReminder(alarmDay.getTimeInMillis(), reminder.getContent());
                                    Date alarm = new Date(today.getYear(), today.getMonth(), today.getDay(), year, month, day);
                                    scheduleReminder(alarm.getTime(), reminder.getContent());
                                }
                            };
                            TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                                    final Calendar alarmTime = Calendar.getInstance();
                                    alarmTime.set(Calendar.HOUR, hour);
                                    alarmTime.set(Calendar.MINUTE, minute);
                                    scheduleReminder(alarmTime.getTimeInMillis(), reminder.getContent());
                                    Date alarm = new Date(today.getYear(), today.getMonth(), today.getDate(), hour, minute);
                                    scheduleReminder(alarm.getTime(), reminder.getContent());
                                }
                            };
                            //final Calendar today = Calendar.getInstance();
                            //new TimePickerDialog(RemindersActivity.this, null, today.get(Calendar.HOUR), today.get(Calendar.MINUTE), false).show();
                            new DatePickerDialog(RemindersActivity.this, listener_1, today.getYear(),today.getMonth(),today.getDay()).show();
                            new TimePickerDialog(RemindersActivity.this,listener,today.getHours(),today.getMinutes(),false).show();

                        }
                        dialog.dismiss();
                    }
                });
            }
        });
        if (savedInstanceState == null) {
        //Clear all data
            mDbAdapter.deleteAllReminders();
            //Add some data
            addSomeReminders();
        }
        Cursor cursor = mDbAdapter.fetchAllReminders();
        //from columns defined in the db
        String[] from = new String[]{
                RemindersDbAdapter.COL_CONTENT
        };
        //to the ids of views in the layout
        int[] to = new int[]{
                R.id.row_text
        };
        mCursorAdapter = new RemindersSimpleCursorAdapter(RemindersActivity.this, R.layout.reminders_row, cursor, from, to, 0);
        //context, layout, view, data
        //the cursorAdapter (controller) is now updating the listView (view)
        //with data from the db (model)
        mListView.setAdapter(mCursorAdapter);
    }

    private void scheduleReminder(long time, String content) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(this, ReminderAlarmReceiver.class);
        alarmIntent.putExtra(ReminderAlarmReceiver.REMINDER_TEXT, content);
        //alarmIntent.putExtra(ReminderAlarmReceiver.REMINDER_SONG, content);
        PendingIntent broadcast = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, broadcast);
    }

    private int getIdFromPosition(int nC) {
        return (int)mCursorAdapter.getItemId(nC);
    }

    private void addSomeReminders() {
        mDbAdapter.createReminder("Pray for Royal Theatre Family Members", true);
        mDbAdapter.createReminder("Send Dad birthday gift", true);
        mDbAdapter.createReminder("Dinner at the Gage on Friday", false);
        mDbAdapter.createReminder("Invisible friend's gift", true);
        mDbAdapter.createReminder("Call PoG", false);
        mDbAdapter.createReminder("Prepare Advanced Android syllabus", true);
        mDbAdapter.createReminder("Read up Javafx", true);
        mDbAdapter.createReminder("Go shopping", false);
        mDbAdapter.createReminder("Buy 300,000 shares of Google", false);
    }

    private void fireCustomDialog(final Reminder reminder){
        // custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_custom);
        TextView titleView = (TextView) dialog.findViewById(R.id.custom_title);
        final EditText editCustom = (EditText) dialog.findViewById(R.id.custom_edit_reminder);
        Button commitButton = (Button) dialog.findViewById(R.id.custom_button_commit);
        final CheckBox checkBox = (CheckBox) dialog.findViewById(R.id.custom_check_box);
        checkBox.setChecked(true);  //make all reminders important
        LinearLayout rootLayout = (LinearLayout) dialog.findViewById(R.id.custom_root_layout);
        final boolean isEditOperation = (reminder != null);
//this is for an edit
        if (isEditOperation){
            titleView.setText("Edit Reminder");
            checkBox.setChecked(reminder.getImportant() == 1);
            editCustom.setText(reminder.getContent());
            rootLayout.setBackgroundColor(getResources().getColor(R.color.orange));
        }
        commitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reminderText = editCustom.getText().toString();
                if (isEditOperation) {
                    Reminder reminderEdited = new Reminder(reminder.getId(), reminderText, checkBox.isChecked() ? 1 : 0);
                    mDbAdapter.updateReminder(reminderEdited);
//this is for new reminder
                } else {
                    mDbAdapter.createReminder(reminderText, checkBox.isChecked());
                }
                mCursorAdapter.changeCursor(mDbAdapter.fetchAllReminders());
                dialog.dismiss();
            }
        });
        Button buttonCancel = (Button) dialog.findViewById(R.id.custom_button_cancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_reminders, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new:
        //create new Reminder
                fireCustomDialog(null);
                return true;
            case R.id.action_exit:
                exit();
                return true;
            default:
                return false;
        }
    }

    private void exit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.exit)
                .setCancelable(false)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle("PoG Reminders")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();

    }

    @Override
    public void onBackPressed(){
        exit();
    }


}
