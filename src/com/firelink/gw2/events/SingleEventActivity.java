package com.firelink.gw2.events;

import com.firelink.gw2.objects.EventHolder;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

public class SingleEventActivity extends Activity
{
    private Activity activity;
    private Context context;

    private TextView resultTextView;

    private EventHolder eventHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_event_layout);

        activity = this;
        context  = this;

        eventHolder = new EventHolder();

        Bundle extras = getIntent().getExtras();

        //        eventHolder.eventID     = extras.getInt("eventID");
        //        eventHolder.name        = extras.getString("name");
        //        eventHolder.description = extras.getString("description");
        //        eventHolder.type        = extras.getString("type");
        //        eventHolder.typeID      = extras.getInt("typeID");
        //
        //        resultTextView = (TextView)findViewById(R.id.eventView_resultTextView);
        //        resultTextView.setText(eventHolder.name + "");
    }
}
