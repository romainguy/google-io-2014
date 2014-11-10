/**
 * Copyright 2014 Google
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.io2014;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends Activity {
    public static SparseArray<Bitmap> sPhotoCache = new SparseArray<Bitmap>(4);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void showPhoto(View view) {
        Intent intent = new Intent();
        intent.setClass(this, DetailActivity.class);

        switch (view.getId()) {
            case R.id.show_photo_1:
                intent.putExtra("lat", 37.6329946);
                intent.putExtra("lng", -122.4938344);
                intent.putExtra("zoom", 14.0f);
                intent.putExtra("title", "Pacifica Pier");
                intent.putExtra("description", getResources().getText(R.string.lorem));
                intent.putExtra("photo", R.drawable.photo1);
                break;
            case R.id.show_photo_2:
                intent.putExtra("lat", 37.73284);
                intent.putExtra("lng", -122.503065);
                intent.putExtra("zoom", 15.0f);
                intent.putExtra("title", "Pink Flamingo");
                intent.putExtra("description", getResources().getText(R.string.lorem));
                intent.putExtra("photo", R.drawable.photo2);
                break;
            case R.id.show_photo_3:
                intent.putExtra("lat", 36.861897);
                intent.putExtra("lng", -111.374438);
                intent.putExtra("zoom", 11.0f);
                intent.putExtra("title", "Antelope Canyon");
                intent.putExtra("description", getResources().getText(R.string.lorem));
                intent.putExtra("photo", R.drawable.photo3);
                break;
            case R.id.show_photo_4:
                intent.putExtra("lat", 36.596125);
                intent.putExtra("lng", -118.1604282);
                intent.putExtra("zoom", 9.0f);
                intent.putExtra("title", "Lone Pine");
                intent.putExtra("description", getResources().getText(R.string.lorem));
                intent.putExtra("photo", R.drawable.photo4);
                break;
        }

        ImageView hero = (ImageView) ((View) view.getParent()).findViewById(R.id.photo);

        sPhotoCache.put(intent.getIntExtra("photo", -1),
                ((BitmapDrawable) hero.getDrawable()).getBitmap());

        ActivityOptions options =
                ActivityOptions.makeSceneTransitionAnimation(this, hero, "photo_hero");
        startActivity(intent, options.toBundle());
    }
}
