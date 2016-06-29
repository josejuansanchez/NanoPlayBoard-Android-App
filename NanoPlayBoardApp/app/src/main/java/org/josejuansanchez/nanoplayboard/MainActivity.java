package org.josejuansanchez.nanoplayboard;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView mListview;
    private String[] mValues = new String[] { "Potentiometer", "LDR", "RGB LED" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListview = (ListView) findViewById(R.id.listview);

        final ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < mValues.length; ++i) {
            list.add(mValues[i]);
        }
        final BasicArrayAdapter adapter = new BasicArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        mListview.setAdapter(adapter);

        loadListeners();
    }

    private void loadListeners() {
        mListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                Intent intent = null;
                switch (position) {
                    case 0:
                        intent = new Intent(MainActivity.this, PotentiometerActivity.class);
                        break;
                    case 1:
                        intent = new Intent(MainActivity.this, LDRActivity.class);
                        break;
                    case 2:
                        intent = new Intent(MainActivity.this, RGBActivity.class);
                        break;
                }
                startActivity(intent);
            }

        });
    }

    private class BasicArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public BasicArrayAdapter(Context context, int textViewResourceId,
                                 List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }
}
