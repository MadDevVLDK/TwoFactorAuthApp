package ru.superplushkin.twofactorauthapp.activity;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import ru.superplushkin.twofactorauthapp.subclasses.LocaleHelper;
import ru.superplushkin.twofactorauthapp.subclasses.ThemeHelper;

public abstract class MyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        LocaleHelper.updateLocale(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.updateLocale(base));
    }
}