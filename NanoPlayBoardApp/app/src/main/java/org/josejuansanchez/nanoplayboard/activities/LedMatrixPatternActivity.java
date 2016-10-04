package org.josejuansanchez.nanoplayboard.activities;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import org.josejuansanchez.nanoplayboard.R;
import org.josejuansanchez.nanoplayboard.constants.ProtocolConstants;
import org.josejuansanchez.nanoplayboard.models.NanoPlayBoardMessage;

public class LedMatrixPatternActivity extends NanoPlayBoardActivity {

    public static final String TAG = LedMatrixPatternActivity.class.getSimpleName();
    private TextView mPatternSelectedDec;
    private TextView mPatternSelectedHex;
    private GridView mGridView;
    private final int MATRIX_ROWS = 7;
    private final int MATRIX_COLS = 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led_matrix_pattern);
        setTitle("Led Matrix");
        mPatternSelectedDec = (TextView) findViewById(R.id.pattern_selected_decimal);
        mPatternSelectedHex = (TextView) findViewById(R.id.pattern_selected_hexadecimal);
        mGridView = (GridView) findViewById(R.id.gridview);
        mGridView.setAdapter(new LedMatrixPatternActivity.ImageAdapter(this));
        setListeners();
    }

    private void setListeners() {
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                // Update the thumbnail of the item (is on or off?)
                int thumbId = ((LedMatrixPatternActivity.ImageAdapter)mGridView.getAdapter()).getItemThumbId(position);
                if (thumbId == R.drawable.led_off) {
                    ((LedMatrixPatternActivity.ImageAdapter)mGridView.getAdapter()).setItemThumbId(position, R.drawable.led_on);
                } else {
                    ((LedMatrixPatternActivity.ImageAdapter)mGridView.getAdapter()).setItemThumbId(position, R.drawable.led_off);
                }

                // Calculate the values of the matrix and send the JSON message
                boolean matrix[][] = createMatrixFromAdapter();
                int columns[] = convertColumnsMatrixToInt(matrix);

                NanoPlayBoardMessage message = new NanoPlayBoardMessage(ProtocolConstants.ID_LEDMATRIX_PRINT_PATTERN);
                message.setPattern(columns);
                sendJsonMessage(message);
                updateTextViewPattern(columns);
            }
        });
    }

    // Create a matrix of booleans from the adapter used for the GridView
    private boolean [][] createMatrixFromAdapter() {
        LedMatrixPatternActivity.ImageAdapter adapter = ((LedMatrixPatternActivity.ImageAdapter) mGridView.getAdapter());
        boolean matrix[][] = new boolean[MATRIX_ROWS][MATRIX_COLS];
        for(int i = 0; i < MATRIX_ROWS; i++) {
            for(int j = 0; j < MATRIX_COLS; j++ ) {
                if (adapter.getItemThumbId((i * MATRIX_COLS) + j) == R.drawable.led_on) {
                    matrix[i][j] = true;
                } else {
                    matrix[i][j] = false;
                }
            }
        }
        return matrix;
    }

    // Convert each column of the matrix into a int value
    private int [] convertColumnsMatrixToInt(boolean matrix[][]) {
        int columnInt[] = new int[MATRIX_COLS];
        for(int j = 0; j < MATRIX_COLS; j++) {
            boolean columnBool[] = copyColumnMatrix(matrix, j);
            columnInt[j] = convertArrayOfBooleansToInt(columnBool);
        }
        return columnInt;
    }

    // Note that is necessary to add manually the last element of the array
    // because the led matrix has 7 rows but we need an array of 8 bits
    private boolean [] copyColumnMatrix(boolean matrix[][], int colIndex) {
        boolean column[] = new boolean[MATRIX_ROWS + 1];
        for(int i = 0; i < MATRIX_ROWS; i++) {
            column[i] = matrix[i][colIndex];
        }
        column[MATRIX_ROWS] = false;
        return column;
    }

    private int convertArrayOfBooleansToInt(boolean a[]) {
        int n = 0;
        int length = a.length;
        for (int i = 0; i < length; i++) {
            n = (n << 1) + (a[i] ? 1 : 0);
        }
        return n;
    }

    private void updateTextViewPattern(int[] pattern) {
        String DecText = patternToString(pattern, 10);
        String HexText = patternToString(pattern, 16);
        mPatternSelectedDec.setText(Html.fromHtml("Dec: <b>" + DecText + "</b>"));
        mPatternSelectedHex.setText(Html.fromHtml("Hex: <b>" + HexText + "</b>"));
    }

    private String patternToString(int pattern[], int base) {
        String text = "";
        int length = pattern.length;
        for(int i = 0; i < length; i++) {
            text += Integer.toString(pattern[i], base);
            if (i != length - 1) {
                text += ", ";
            }
        }
        return text;
    }

    private class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return mThumbIds.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        public void setItemThumbId(int position, int id) {
            mThumbIds[position] = id;
            notifyDataSetChanged();
        }

        public int getItemThumbId(int position) {
            return mThumbIds[position];
        }

        // create a new ImageView for each item referenced by the Adapter
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageResource(mThumbIds[position]);
            return imageView;
        }

        private Integer[] mThumbIds = {
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off,
                R.drawable.led_off
        };
    }
}