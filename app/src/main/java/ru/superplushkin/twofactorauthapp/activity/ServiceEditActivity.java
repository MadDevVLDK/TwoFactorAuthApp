package ru.superplushkin.twofactorauthapp.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

import ru.superplushkin.twofactorauthapp.R;
import ru.superplushkin.twofactorauthapp.db.DatabaseHelper;
import ru.superplushkin.twofactorauthapp.model.Service;
import ru.superplushkin.twofactorauthapp.subclasses.TOTPGenerator;
import ru.superplushkin.twofactorauthapp.subclasses.TransitionHelper;

public class ServiceEditActivity extends MyActivity {

    private DatabaseHelper dbHelper;
    private Service service;

    private TextInputEditText etServiceName, etIssuer, etAccount, etAlgorithm, etDigits, etPeriod;
    private LinearLayout layoutAdvancedContent;
    private TextView expandAdvancedOptions;
    private MaterialButton btnSave, btnCancel;

    private String originalServiceName, originalIssuer, originalAccount;
    private String originalAlgorithm;
    private short originalDigits, originalPeriod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.activity_edit_service);
        TransitionHelper.setOnStart(this, R.anim.slide_in_right);

        long serviceId = getIntent().getLongExtra("SERVICE_ID", -1);
        if (serviceId == -1) {
            finish();
            return;
        }

        dbHelper = new DatabaseHelper(this);
        service = dbHelper.getService(serviceId);
        if (service == null) {
            Toast.makeText(this, R.string.service_adding_error, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        fillFields();
        storeOriginalValues();
        setupClickListeners();
        setupBackPressedHandler();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        etServiceName = findViewById(R.id.etServiceName);
        etIssuer = findViewById(R.id.etIssuer);
        etAccount = findViewById(R.id.etAccount);
        etAlgorithm = findViewById(R.id.etAlgorithm);
        etDigits = findViewById(R.id.etDigits);
        etPeriod = findViewById(R.id.etPeriod);

        layoutAdvancedContent = findViewById(R.id.layoutAdvancedContent);
        expandAdvancedOptions = findViewById(R.id.expandAdvancedOptions);

        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        layoutAdvancedContent.setVisibility(View.VISIBLE);
        expandAdvancedOptions.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_expand_less, 0, 0, 0);
    }

    private void fillFields() {
        etServiceName.setText(service.getServiceName());
        etIssuer.setText(service.getIssuer());
        etAccount.setText(service.getAccount());
        etAlgorithm.setText(service.getAlgorithm());
        etDigits.setText(String.valueOf(service.getDigits()));
        etPeriod.setText(String.valueOf(service.getPeriod()));

        // курсор в конец
        etServiceName.setSelection(etServiceName.getText().length());
        etIssuer.setSelection(etIssuer.getText().length());
        etAccount.setSelection(etAccount.getText().length());
        etAlgorithm.setSelection(etAlgorithm.getText().length());
        etDigits.setSelection(etDigits.getText().length());
        etPeriod.setSelection(etPeriod.getText().length());
    }

    private void storeOriginalValues() {
        originalServiceName = service.getServiceName();
        originalIssuer = service.getIssuer();
        originalAccount = service.getAccount();
        originalAlgorithm = service.getAlgorithm();
        originalDigits = service.getDigits();
        originalPeriod = service.getPeriod();
    }

    private void setupClickListeners() {
        btnSave.setOnClickListener(v -> saveService());
        btnCancel.setOnClickListener(v -> attemptExit());

        expandAdvancedOptions.setOnClickListener(v -> {
            boolean isVisible = layoutAdvancedContent.getVisibility() == View.VISIBLE;
            layoutAdvancedContent.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            expandAdvancedOptions.setCompoundDrawablesRelativeWithIntrinsicBounds(
                isVisible ? R.drawable.ic_expand_more : R.drawable.ic_expand_less, 0, 0, 0
            );
        });
    }

    private void setupBackPressedHandler() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                attemptExit();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public boolean onSupportNavigateUp() {
        attemptExit();
        return true;
    }

    private void attemptExit() {
        if (hasUnsavedChanges()) {
            showUnsavedChangesDialog();
        } else {
            finishWithResult(false);
        }
    }

    private boolean hasUnsavedChanges() {
        String currentName = Objects.requireNonNull(etServiceName.getText()).toString().trim();
        String currentIssuer = Objects.requireNonNull(etIssuer.getText()).toString().trim();
        String currentAccount = Objects.requireNonNull(etAccount.getText()).toString().trim();
        String currentAlgorithm = Objects.requireNonNull(etAlgorithm.getText()).toString().trim();
        String currentDigitsStr = Objects.requireNonNull(etDigits.getText()).toString().trim();
        String currentPeriodStr = Objects.requireNonNull(etPeriod.getText()).toString().trim();

        boolean nameChanged = !currentName.equals(originalServiceName);
        boolean issuerChanged = !currentIssuer.equals(originalIssuer);
        boolean accountChanged = !currentAccount.equals(originalAccount);
        boolean algorithmChanged = !currentAlgorithm.equals(originalAlgorithm);

        short currentDigits;
        try {
            currentDigits = currentDigitsStr.isEmpty() ? Service.DEFAULT_ALGORITHM_NUM_LENGHT : Short.parseShort(currentDigitsStr);
        } catch (NumberFormatException e) {
            currentDigits = originalDigits;
        }
        boolean digitsChanged = currentDigits != originalDigits;

        short currentPeriod;
        try {
            currentPeriod = currentPeriodStr.isEmpty() ? Service.DEFAULT_ALGORITHM_PERIOD : Short.parseShort(currentPeriodStr);
        } catch (NumberFormatException e) {
            currentPeriod = originalPeriod;
        }
        boolean periodChanged = currentPeriod != originalPeriod;

        return nameChanged || issuerChanged || accountChanged || algorithmChanged || digitsChanged || periodChanged;
    }

    private void showUnsavedChangesDialog() {
        new AlertDialog.Builder(this, R.style.MyDialogTheme)
            .setTitle(R.string.unsaved_changes)
            .setMessage(R.string.u_ve_got_unsaved_changes_are_u_sure_u_want_to_exit)
            .setPositiveButton(R.string.exit_button, (dialog, which) -> finishWithResult(false))
            .setNegativeButton(R.string.cancel_button, null)
            .show();
    }

    private void saveService() {
        String serviceName = Objects.requireNonNull(etServiceName.getText()).toString().trim();
        String issuer = Objects.requireNonNull(etIssuer.getText()).toString().trim();
        String account = Objects.requireNonNull(etAccount.getText()).toString().trim();
        String algorithm = Objects.requireNonNull(etAlgorithm.getText()).toString().trim();
        String digitsStr = Objects.requireNonNull(etDigits.getText()).toString().trim();
        String periodStr = Objects.requireNonNull(etPeriod.getText()).toString().trim();

        if (serviceName.isEmpty()) {
            etServiceName.setError(getString(R.string.enter_service_name));
            etServiceName.requestFocus();
            return;
        }
        if (issuer.isEmpty()) {
            etIssuer.setError(getString(R.string.issuer_required));
            etIssuer.requestFocus();
            return;
        }

        if (algorithm.isEmpty()) {
            algorithm = Service.DEFAULT_ALGORITHM;
        } else if (!TOTPGenerator.isValidAlgorithm(algorithm)) {
            etAlgorithm.setError(getString(R.string.invalid_algorithm));
            etAlgorithm.requestFocus();
            return;
        }

        short digits;
        try {
            digits = digitsStr.isEmpty() ? Service.DEFAULT_ALGORITHM_NUM_LENGHT : Short.parseShort(digitsStr);
            if (!TOTPGenerator.isValidDigits(digits)) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            etDigits.setError(getString(R.string.digits_must_be_between_4_and_10));
            etDigits.requestFocus();
            return;
        }

        short period;
        try {
            period = periodStr.isEmpty() ? Service.DEFAULT_ALGORITHM_PERIOD : Short.parseShort(periodStr);
            if (!TOTPGenerator.isValidPeriod(period)) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            etPeriod.setError(getString(R.string.period_must_be_between_10_and_60));
            etPeriod.requestFocus();
            return;
        }

        service.setServiceName(serviceName);
        service.setAccount(account);
        service.setIssuer(issuer);
        service.setAlgorithm(algorithm);
        service.setDigits(digits);
        service.setPeriod(period);

        boolean updated = dbHelper.updateService(service);
        if (updated) {
            Toast.makeText(this, R.string.service_updated, Toast.LENGTH_SHORT).show();
            finishWithResult(true);
        } else {
            Toast.makeText(this, R.string.service_update_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void finishWithResult(boolean saved) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("SERVICE_UPDATED", saved);
        if (saved && service != null) {
            resultIntent.putExtra("SERVICE_ID", service.getId());
        }
        setResult(RESULT_OK, resultIntent);
        finish();
        TransitionHelper.setOnClose(this, R.anim.slide_out_right);
    }
}