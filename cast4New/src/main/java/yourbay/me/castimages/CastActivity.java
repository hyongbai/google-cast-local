package yourbay.me.castimages;

import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import yourbay.me.castimages.cast.CastHelper;
import yourbay.me.castimages.cast.sender.ImageSender;
import yourbay.me.castimages.cast.sender.MediaSender;

public class CastActivity extends ActionBarActivity implements View.OnClickListener {

    private final static String TAG = "CastHelper";
    MediaSender mediaSender;
    String sampleImage = "http://cdnzz.ifanr.com/wp-content/uploads/2015/01/ibm.jpg";
    private CastHelper mCastHelper = new CastHelper();
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCastHelper.onCreate(this);
        setContentView(R.layout.activity_cast);
        findViewById(R.id.btn_cast).setOnClickListener(this);
        mediaSender = new ImageSender();
        initSpinner();
//        sampleImage = "https://doc-14-2g-docs.googleusercontent.com/docs/securesc/6ptrqjnrb082u1mbous1sbt3gvrjpsvq/j0tqosf7hj2eo6qapdhpal1fdpsvt6t9/1421114400000/09116082813383085661/09116082813383085661/0BxTVIr4xUnqFVzBIYjBnc3I4ekU"//?h=09181378601586100340&e=download&gd=true
//        sampleImage="https://doc-0k-2g-docs.googleusercontent.com/docs/securesc/6ptrqjnrb082u1mbous1sbt3gvrjpsvq/uv5b2u9m4j8315tqjm0q131bpehia24h/1421121600000/09116082813383085661/09116082813383085661/0BxTVIr4xUnqFRGNObmFmX2EtSzQ?h=09181378601586100340&e=download&gd=true";
//        sampleImage = "file:///sdcard/0/DCIM/Camera/IMG_20141222_095634.jpg";
    }

    private void initSpinner() {
        String[] URLs = new String[]{"http://cdnzz.ifanr.com/wp-content/uploads/2015/01/ibm.jpg"//ifanr
                , "https://doc-0k-2g-docs.googleusercontent.com/docs/securesc/6ptrqjnrb082u1mbous1sbt3gvrjpsvq/uv5b2u9m4j8315tqjm0q131bpehia24h/1421121600000/09116082813383085661/09116082813383085661/0BxTVIr4xUnqFRGNObmFmX2EtSzQ?h=09181378601586100340&e=download&gd=true"//
                , "http://192.168.2.78:8000/nexus-n5_2x.png"//MAC
        };
        editText = (EditText) findViewById(R.id.et_text);
        editText.setVisibility(View.GONE);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        final ArrayAdapter<String> adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, URLs);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//              textview.setText("你的城市是：" + adapter.getItem(position));
                sampleImage = adapter.getItem(position);
//                editText.setText(sampleImage);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_cast, menu);
        MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
        MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteMenuItem);
        mediaRouteActionProvider.setRouteSelector(mCastHelper.getSelector());
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCastHelper.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        if (isFinishing()) {
            mCastHelper.onPause();
        }
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        if (!TextUtils.isEmpty(editText.getText().toString())) {
            sampleImage = editText.getText().toString();
        }
        mediaSender.setApiClient(mCastHelper.getApiClient());
        mediaSender.cast(sampleImage, "IBM LOGO");
        Log.d(TAG, "sampleImage=" + sampleImage);
    }
}