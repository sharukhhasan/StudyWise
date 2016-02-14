package com.sharukhhasan.studywise.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Spinner;

import com.sharukhhasan.studywise.R;

public class RegisterCoursesActivity extends AppCompatActivity {
    private String[] spinnerItems;
    private static final String TAG = RegisterCoursesActivity.class.getSimpleName();

    Spinner coursesOne, coursesTwo, coursesThree, coursesFour, coursesFive;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_courses);

        createSpinners();
    }

    public void createSpinners()
    {
        final Spinner coursesOne = (Spinner) findViewById(R.id.courseOneSpinner);
        final Spinner coursesTwo = (Spinner) findViewById(R.id.courseTwoSpinner);
        final Spinner coursesThree = (Spinner) findViewById(R.id.courseThreeSpinner);
        final Spinner coursesFour = (Spinner) findViewById(R.id.courseFourSpinner);
        final Spinner coursesFive = (Spinner) findViewById(R.id.courseFiveSpinner);

        /*ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("EngineeringCourses");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e)
            {
                if(e == null)
                {
                    ArrayList<String> courseNameList = new ArrayList<>();
                    ArrayList<String> courseNumberList = new ArrayList<>();
                    for(ParseObject object : list)
                    {
                        courseNameList.add(object.getString("coursename"));
                    }
                    ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_item, courseNameList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    coursesOne.setAdapter(adapter);
                    coursesTwo.setAdapter(adapter);
                    coursesThree.setAdapter(adapter);
                    coursesFour.setAdapter(adapter);
                    coursesFive.setAdapter(adapter);
                }
                else
                {
                    Log.d(TAG, "e is not null");
                }
            }
        });*/
    }

}
