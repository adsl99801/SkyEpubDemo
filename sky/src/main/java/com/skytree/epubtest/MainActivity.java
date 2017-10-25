package com.skytree.epubtest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by keith on 2017/10/25.
 */

public class MainActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SkyApplicationHolder app = new SkyApplicationHolder();
        Context context = this;
        app.init(context);
        startActivity(new Intent(this, HomeActivity.class));
    }

}
