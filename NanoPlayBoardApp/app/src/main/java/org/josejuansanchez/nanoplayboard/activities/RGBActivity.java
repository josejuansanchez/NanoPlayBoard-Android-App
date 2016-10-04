package org.josejuansanchez.nanoplayboard.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.christophesmet.android.views.colorpicker.ColorPickerView;

import org.josejuansanchez.nanoplayboard.R;
import org.josejuansanchez.nanoplayboard.constants.ProtocolConstants;
import org.josejuansanchez.nanoplayboard.models.NanoPlayBoardMessage;

public class RGBActivity extends NanoPlayBoardActivity {

    public static final String TAG = RGBActivity.class.getSimpleName();
    private ColorPickerView mColorPickerView;
    private View mColorSectedView;
    private TextView mColorSelectedRed;
    private TextView mColorSelectedGreen;
    private TextView mColorSelectedBlue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rgb);
        setTitle("RGB LED");
        mColorPickerView = (ColorPickerView) findViewById(R.id.colorpicker);
        mColorSectedView = (View) findViewById(R.id.colorselected_view);
        mColorSelectedRed = (TextView) findViewById(R.id.colorselected_red);
        mColorSelectedGreen = (TextView) findViewById(R.id.colorselected_green);
        mColorSelectedBlue = (TextView) findViewById(R.id.colorselected_blue);
        mColorPickerView.setDrawDebug(false);
        setListeners();
    }

    public void setListeners() {
        mColorPickerView.setColorListener(new ColorPickerView.ColorListener() {
            @Override
            public void onColorSelected(int color) {
                updateSelectedColor(color);
                NanoPlayBoardMessage message = new NanoPlayBoardMessage(ProtocolConstants.ID_RGB_SET_COLOR);
                message.setColor(color);
                sendJsonMessage(message);
            }

            @Override
            public void onStopTrackingTouch(int color) {
                updateSelectedColor(color);
                NanoPlayBoardMessage message = new NanoPlayBoardMessage(ProtocolConstants.ID_RGB_SET_COLOR);
                message.setColor(color);
                sendJsonMessage(message);
            }
        });
    }

    private void updateSelectedColor(int color) {
        mColorSectedView.setBackgroundColor(color);
        mColorSelectedRed.setText(Html.fromHtml("R: <b>" + Color.red(color) + "</b>"));
        mColorSelectedGreen.setText(Html.fromHtml("G: <b>" + Color.green(color) + "</b>"));
        mColorSelectedBlue.setText(Html.fromHtml("B: <b>" + Color.blue(color) + "</b>"));
    }
}
