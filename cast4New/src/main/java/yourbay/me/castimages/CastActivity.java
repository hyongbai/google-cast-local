package yourbay.me.castimages;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import yourbay.me.castimages.cast.CastHelper;
import yourbay.me.castimages.cast.Consts;
import yourbay.me.castimages.cast.httpd.CastFileServer;
import yourbay.me.castimages.cast.sender.ImageCaster;
import yourbay.me.castimages.cast.sender.MediaCaster;

public class CastActivity extends ActionBarActivity implements View.OnClickListener {

    private final static String TAG = "CastHelper";
    private final int REQUEST_CODE_CHOOSE_IMAGE = 1234;
    MediaCaster mediaSender;
    CastFileServer mFileServer;
    private CastHelper mCastHelper;
    private EditText editText;
    private String BASE_URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCastHelper = new CastHelper(this);
        setContentView(R.layout.activity_cast);
        startFileServer();
        findViewById(R.id.btn_cast).setOnClickListener(this);
        initSpinner();
    }

    private void initSpinner() {
        Object[] NAMES = Consts.URLS.keySet().toArray();
        editText = (EditText) findViewById(R.id.et_text);
        editText.setVisibility(View.GONE);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        final ArrayAdapter<String> adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, NAMES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String name = adapter.getItem(position);
                String path = Consts.URLS.get(name);
                cast(path, name);
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
        MenuItem routerMenu = menu.findItem(R.id.media_route_menu_item);
        mCastHelper.onCreateOptionsMenu(routerMenu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCastHelper.start();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "stop");
        if (isFinishing()) {
            mCastHelper.stop();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mFileServer != null) {
            mFileServer.closeAllConnections();
            mFileServer = null;
        }
    }

    @Override
    public void onClick(View v) {
        IntentUtils.pickupImages(this, REQUEST_CODE_CHOOSE_IMAGE);
    }

    private void cast(String uri, String title) {
        if (mediaSender == null) {
            mediaSender = new ImageCaster();
        }
        mediaSender.setApiClient(mCastHelper.getApiClient());
        mediaSender.cast(uri, title);
        Log.d(TAG, "cast:title=" + title + "    uri=" + uri);
    }

    private void startFileServer() {
        if (mFileServer != null) {
            return;
        }
        TextView tv = (TextView) findViewById(R.id.hello_world);
        mFileServer = new CastFileServer();
        BASE_URL = mFileServer.getLocalIpAddress() + ":" + mFileServer.getServerPort();
        try {
            mFileServer.start();
        } catch (IOException e) {
            e.printStackTrace();
            tv.append(e.getMessage());
            tv.append("\n");
        }
        tv.append(BASE_URL);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode != REQUEST_CODE_CHOOSE_IMAGE) {
            return;
        }
        if (mFileServer == null) {
            return;
        }
        try {
            Uri selectedImage = data.getData();
            String path = UriUtil.getImagePath(this, selectedImage);
            String uri = mFileServer.generateUri(path);
            cast(uri, new File(path).getName());
            Log.d(TAG, "onActivityResult    " + path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}