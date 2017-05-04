package com.task.endlesstilemap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //2D array of bitmaps
        Bitmap[][] arrayTiles = new Bitmap[10][10];

        //position of  the map camera in pixels
        int startX = 0;
        int startY = 0;

        BitmapDrawable drawableSingleTile = (BitmapDrawable) getResources().getDrawable(R.drawable.tile_00);
        Bitmap singleTile = drawableSingleTile.getBitmap();

        //default size of all the tiles in pixels
        int tileWidth = singleTile.getWidth();
        int tileHeight = singleTile.getHeight();

        //Created 2D array of bitmaps
        for (int i = 0; i < arrayTiles.length; i++) {
            for (int j = 0; j < arrayTiles.length; j++) {
                Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier("tile_" + j + i, "drawable", getPackageName()));
                arrayTiles[i][j] = imageBitmap;
            }
        }

        setContentView(new Background(this, arrayTiles, tileWidth, tileHeight, startX, startY));
    }
}
