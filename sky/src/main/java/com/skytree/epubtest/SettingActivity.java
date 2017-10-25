package com.skytree.epubtest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class SettingActivity extends Activity {
    public CheckBox doublePagedCheckBox;
    public CheckBox lockRotationCheckBox;
    public CheckBox globalPaginationCheckBox;

    public Button themeWhiteButton;
    public Button themeBrownButton;
    public Button themeBlackButton;

    public ImageView themeWhiteImageView;
    public ImageView themeBrownImageView;
    public ImageView themeBlackImageView;

    public RadioGroup pageTransitionGroup;

    public CheckBox mediaOverlayCheckBox;
    public CheckBox ttsCheckBox;
    public CheckBox autoStartPlayingCheckBox;
    public CheckBox autoLoadNewChapterCheckBox;
    public CheckBox highlightTextToVoiceCheckBox;



    public TextView SkyEpub;
    private OnClickListener onClickListener = new OnClickListener() {
        public void onClick(View arg) {
            if (arg == SkyEpub) {
                String url = "http://skyepub.net/";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                return;
            }

            int themeIndex = 0;
            if (arg == themeWhiteButton) {
                themeIndex = 0;
            } else if (arg == themeBrownButton) {
                themeIndex = 1;
            } else {
                themeIndex = 2;
            }
             SkyApplicationHolder.setting.theme = themeIndex;
            markTheme( SkyApplicationHolder.setting.theme);
        }
    };

    public void showMessageBox(String title, String messgage) {
        AlertDialog ad = new AlertDialog.Builder(this).create();
        ad.setCancelable(false); // This blocks the 'BACK' button
        ad.setTitle(title);
        ad.setMessage(messgage);
        ad.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        ad.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

         SkyApplicationHolder.loadSetting();

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_setting);

        doublePagedCheckBox = (CheckBox) this.findViewById(R.id.doublePagedCheckBox);
        lockRotationCheckBox = (CheckBox) this.findViewById(R.id.lockRotationCheckBox);
        globalPaginationCheckBox = (CheckBox) this.findViewById(R.id.globalPaginationCheckBox);

        mediaOverlayCheckBox = (CheckBox) this.findViewById(R.id.mediaOverlayCheckBox);
        ttsCheckBox = (CheckBox) this.findViewById(R.id.ttsCheckBox);
        autoStartPlayingCheckBox = (CheckBox) this.findViewById(R.id.autoStartPlayingCheckBox);
        autoLoadNewChapterCheckBox = (CheckBox) this.findViewById(R.id.autoLoadNewChapterCheckBox);
        highlightTextToVoiceCheckBox = (CheckBox) this.findViewById(R.id.highlightTextToVoiceCheckBox);


        globalPaginationCheckBox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                CheckBox cb = (CheckBox) v;
                if (cb.isChecked()) {
                    showMessageBox(getString(R.string.warning), getString(R.string.globalpaginationwarning));
                }
            }
        });

        themeWhiteButton = (Button) this.findViewById(R.id.themeWhiteButton);
        themeWhiteButton.setOnClickListener(onClickListener);
        themeBrownButton = (Button) this.findViewById(R.id.themeBrownButton);
        themeBrownButton.setOnClickListener(onClickListener);
        themeBlackButton = (Button) this.findViewById(R.id.themeBlackButton);
        themeBlackButton.setOnClickListener(onClickListener);

        themeWhiteImageView = (ImageView) this.findViewById(R.id.themeWhiteImageView);
        themeWhiteImageView.setScaleType(ScaleType.FIT_CENTER);
        themeWhiteImageView.setAdjustViewBounds(true);

        themeBrownImageView = (ImageView) this.findViewById(R.id.themeBrownImageView);
        themeBrownImageView.setScaleType(ScaleType.FIT_CENTER);
        themeBrownImageView.setAdjustViewBounds(true);

        themeBlackImageView = (ImageView) this.findViewById(R.id.themeBlackImageView);
        themeBlackImageView.setScaleType(ScaleType.FIT_CENTER);
        themeBlackImageView.setAdjustViewBounds(true);

        SkyEpub = (TextView) this.findViewById(R.id.skyepubTextView);
        SkyEpub.setOnClickListener(onClickListener);


        pageTransitionGroup = (RadioGroup) this.findViewById(R.id.pageTransitionGroup);
    }

    private void showToast(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void markTheme(int themeIndex) {
        int markColor = 0xAABFFF00;
        themeWhiteImageView.setBackgroundColor(Color.TRANSPARENT);
        themeBrownImageView.setBackgroundColor(Color.TRANSPARENT);
        themeBlackImageView.setBackgroundColor(Color.TRANSPARENT);

        if (themeIndex == 0) {
            themeWhiteImageView.setBackgroundColor(markColor);
        } else if (themeIndex == 1) {
            themeBrownImageView.setBackgroundColor(markColor);
        } else {
            themeBlackImageView.setBackgroundColor(markColor);
        }
    }

    public void loadValues() {
        doublePagedCheckBox.setChecked( SkyApplicationHolder.setting.doublePaged);
        lockRotationCheckBox.setChecked( SkyApplicationHolder.setting.lockRotation);
        globalPaginationCheckBox.setChecked( SkyApplicationHolder.setting.globalPagination);

        mediaOverlayCheckBox.setChecked( SkyApplicationHolder.setting.mediaOverlay);
        ttsCheckBox.setChecked( SkyApplicationHolder.setting.tts);
        autoStartPlayingCheckBox.setChecked( SkyApplicationHolder.setting.autoStartPlaying);
        autoLoadNewChapterCheckBox.setChecked( SkyApplicationHolder.setting.autoLoadNewChapter);
        highlightTextToVoiceCheckBox.setChecked( SkyApplicationHolder.setting.highlightTextToVoice);

        int index =  SkyApplicationHolder.setting.transitionType;
        if (index == 0) pageTransitionGroup.check(R.id.noneRadio);
        else if (index == 1) pageTransitionGroup.check(R.id.slideRadio);
        else pageTransitionGroup.check(R.id.curlRadio);

        markTheme( SkyApplicationHolder.setting.theme);
    }

    public void saveValues() {
         SkyApplicationHolder.setting.doublePaged = doublePagedCheckBox.isChecked();
         SkyApplicationHolder.setting.lockRotation = lockRotationCheckBox.isChecked();
         SkyApplicationHolder.setting.globalPagination = globalPaginationCheckBox.isChecked();

         SkyApplicationHolder.setting.mediaOverlay = mediaOverlayCheckBox.isChecked();
         SkyApplicationHolder.setting.tts = ttsCheckBox.isChecked();
         SkyApplicationHolder.setting.autoStartPlaying = autoStartPlayingCheckBox.isChecked();
         SkyApplicationHolder.setting.autoLoadNewChapter = autoLoadNewChapterCheckBox.isChecked();
         SkyApplicationHolder.setting.highlightTextToVoice = highlightTextToVoiceCheckBox.isChecked();

        int index = pageTransitionGroup.indexOfChild(findViewById(pageTransitionGroup.getCheckedRadioButtonId()));
         SkyApplicationHolder.setting.transitionType = index;
    }

    @Override
    public void onResume() {
        super.onResume();
         SkyApplicationHolder.loadSetting();
        loadValues();
    }

    @Override
    public void onPause() {
        super.onPause();
        saveValues();
         SkyApplicationHolder.saveSetting();
    }

}
