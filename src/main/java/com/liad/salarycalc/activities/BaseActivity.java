package com.liad.salarycalc.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.liad.salarycalc.R;
import com.liad.salarycalc.fragments.DataFragment;
import com.liad.salarycalc.managers.AnalyticsManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import static androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE;

public abstract class BaseActivity extends AppCompatActivity {

    private AlertDialog dialog;
    private DataFragment dataFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setFullScreen();
    }

    private void setFullScreen() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public void changeActivity(Activity activity, Class destActivity, boolean closeCurrent) {
        Intent intent = new Intent(activity, destActivity);
        startActivity(intent);
        if (closeCurrent) activity.finish();
    }

    public void changeFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_activity_frame_layout, fragment, null);
        fragmentTransaction.setTransition(TRANSIT_FRAGMENT_FADE);
        if (addToBackStack) fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void showDialog(final Context context, String title, String message , boolean showCancelBtn) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setCancelable(false);
        View customLayout = LayoutInflater.from(context).inflate(R.layout.custom_dialog_layout, null, false);

        TextView titleTV = customLayout.findViewById(R.id.custom_dialog_title);
        TextView messageTV = customLayout.findViewById(R.id.custom_dialog_message_text);
        Button okBtn = customLayout.findViewById(R.id.custom_dialog_ok_button);
        Button cancelBtn = customLayout.findViewById(R.id.custom_dialog_cancel_button);
        cancelBtn.setVisibility(showCancelBtn ? View.VISIBLE : View.GONE);

        titleTV.setText(title);
        if (message != null) messageTV.setText(message);
        else messageTV.setVisibility(View.GONE);

        builder.setView(customLayout);
        dialog = builder.create();
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dataFragment != null) {
                    dataFragment.deleteShift();
                }
                dialog.dismiss();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 80);
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(inset);

        dialog.show();
    }

    public void closeKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) inputManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        AnalyticsManager.getInstance(this).getMixPanel().flush();
        super.onDestroy();
    }

    public void setDataFragmentRef(DataFragment dataFragment) {
        this.dataFragment = dataFragment;
    }
}
