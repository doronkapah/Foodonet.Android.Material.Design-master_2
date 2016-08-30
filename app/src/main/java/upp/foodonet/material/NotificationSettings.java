package upp.foodonet.material;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import CommonUtilPackage.CommonUtil;

public class NotificationSettings extends AppCompatActivity {

    View v_ring_radius;
    SeekBar sb_radius_setter;
    Switch sw_is_on;
    TextView tv_radius;


    ViewGroup.LayoutParams ring_params;
    int maxRadius;
    int progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb_notifications);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        tv_radius = (TextView)findViewById(R.id.tv_notifications_settings_radius);

        v_ring_radius = findViewById(R.id.v_ring_radius_view);
        ring_params = v_ring_radius.getLayoutParams();
        maxRadius = ring_params.height;
        sb_radius_setter = (SeekBar) findViewById(R.id.sb_radius_changer);
        sb_radius_setter.setMax(getResources().getInteger(R.integer.notifications_settings_default_radius));
        sb_radius_setter.setProgress(CommonUtil.GetNotificationsSettingsRadius(this));

        SetRadiusProgress(CommonUtil.GetNotificationsSettingsRadius(this));

        sb_radius_setter.setProgress(progress);
        sb_radius_setter.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                SetRadiusProgress(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sw_is_on = (Switch)findViewById(R.id.sw_notifications_on_off);
        sw_is_on.setChecked(CommonUtil.GetNotificationsSettingsIsOn(this));
        sw_is_on.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SetIsOn(b);
            }
        });
    }

    public void SetRadiusProgress(int radius) {
        int defaultRadius = getResources().getInteger(R.integer.notifications_settings_default_radius);
        RelativeLayout.LayoutParams params
                = new RelativeLayout.LayoutParams(maxRadius * radius / defaultRadius, maxRadius * radius / defaultRadius);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        v_ring_radius.setLayoutParams(params);
        tv_radius.setText(radius > 0 ? String.valueOf(radius) : "1");
        CommonUtil.SetNotificationsSettingsRadius(this, radius > 0 ? radius : 1);
        progress = radius;
    }

    public void SetIsOn(boolean isOn){
        CommonUtil.SetNotificationsSettingsIsOn(this, isOn);
    }

}
