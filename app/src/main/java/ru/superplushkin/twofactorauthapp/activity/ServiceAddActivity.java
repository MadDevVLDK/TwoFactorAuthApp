package ru.superplushkin.twofactorauthapp.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.IntentCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

import ru.superplushkin.twofactorauthapp.db.DatabaseHelper;
import ru.superplushkin.twofactorauthapp.R;
import ru.superplushkin.twofactorauthapp.model.QRService;
import ru.superplushkin.twofactorauthapp.model.Service;
import ru.superplushkin.twofactorauthapp.subclasses.TOTPGenerator;
import ru.superplushkin.twofactorauthapp.subclasses.TransitionHelper;


public class ServiceAddActivity extends MyActivity {

    private DatabaseHelper dbHelper;

    private TextInputEditText etServiceName, etSecretKey, etIssuer, etAccount, etAlgorithm, etDigits, etPeriod;
    private MaterialButton btnValidate, btnScanQR, btnAdd;
    private LinearLayout layoutAdvancedContent;
    private TextView expandAdvancedOptions;

    private ActivityResultLauncher<Intent> scanQrLauncher;
    private boolean isExpanded = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setAttributes(getWindow().getAttributes());

        setContentView(R.layout.activity_add_service);
        TransitionHelper.setOnStart(this, R.anim.slide_in_right);

        dbHelper = new DatabaseHelper(this);

        setupViews();
        setupQrScanner();
        setupClickListeners();
        setupBackPressedHandler();
    }

    private void setupViews() {
        Toolbar toolBar = findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);

        layoutAdvancedContent = findViewById(R.id.layoutAdvancedContent);
        expandAdvancedOptions = findViewById(R.id.expandAdvancedOptions);

        btnValidate = findViewById(R.id.btnValidate);
        btnAdd = findViewById(R.id.btnAdd);
        btnScanQR = findViewById(R.id.btnScanQR);

        etSecretKey = findViewById(R.id.etSecretKey);
        etServiceName = findViewById(R.id.etServiceName);
        etIssuer = findViewById(R.id.etIssuer);
        etAccount = findViewById(R.id.etAccount);
        etAlgorithm = findViewById(R.id.etAlgorithm);
        etDigits = findViewById(R.id.etDigits);
        etPeriod = findViewById(R.id.etPeriod);
    }
    private void setupClickListeners() {
        expandAdvancedOptions.setOnClickListener(v -> toggleAdvancedSettings());

        btnValidate.setOnClickListener(v -> validateSecretKey());
        btnScanQR.setOnClickListener(v -> launchQrScanner());
        btnAdd.setOnClickListener(v -> addService());
    }
    private void setupBackPressedHandler() {
        var callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (hasUnsavedChanges()) {
                    showUnsavedChangesDialog();
                } else {
                    finishActivityWithAnim(RESULT_CANCELED);
                }
            }
        };

        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void setupQrScanner() {
        scanQrLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Intent data = result.getData();
                if (result.getResultCode() == RESULT_OK && data != null) {
                    QRService service = IntentCompat.getParcelableExtra(data, "SCAN_RESULT_SERVICE", QRService.class);

                    if (service != null) {
                        if (hasUnsavedChanges()) {
                            new AlertDialog.Builder(this)
                                .setTitle(R.string.replace_data)
                                .setMessage(R.string.replace_existing_data_with_qr)
                                .setPositiveButton(R.string.replace_button, (dialog, which) -> fillFieldsFromQr(service))
                                .setNegativeButton(R.string.cancel_button, null)
                                .setIcon(R.drawable.ic_warning)
                                .show();
                        } else {
                            fillFieldsFromQr(service);
                        }
                    } else {
                        Toast.makeText(this, R.string.failed_to_parse_qr, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
    }

    private void toggleAdvancedSettings() {
        if (isExpanded) {
            layoutAdvancedContent.setVisibility(View.GONE);
            expandAdvancedOptions.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_expand_more, 0, 0, 0);
        } else {
            layoutAdvancedContent.setVisibility(View.VISIBLE);
            expandAdvancedOptions.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_expand_less, 0, 0, 0);

            Animation slideDown = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
            slideDown.setDuration(150);
            layoutAdvancedContent.startAnimation(slideDown);
        }

        isExpanded = !isExpanded;
    }

    private void launchQrScanner() {
        Intent intent = new Intent(this, QrScannerActivity.class);
        scanQrLauncher.launch(intent);
    }

    private void fillFieldsFromQr(QRService service) {
        String secret = service.getSecretKey();
        if (secret != null) {
            etSecretKey.setText(secret);
            etSecretKey.setSelection(secret.length());
        }

        String serviceName = service.getServiceName();
        if (serviceName != null && !serviceName.isEmpty()) {
            etServiceName.setText(serviceName);
            etServiceName.setSelection(serviceName.length());
        }

        String issuer = service.getIssuer();
        if (issuer != null && !issuer.isEmpty()) {
            etIssuer.setText(issuer);
        }

        String account = service.getAccount();
        if (account != null && !account.isEmpty()) {
            etAccount.setText(account);
        }

        String algorithm = service.getAlgorithm();
        if (algorithm != null && !algorithm.isEmpty()) {
            etAlgorithm.setText(algorithm);
        } else {
            etAlgorithm.setText(Service.DEFAULT_ALGORITHM);
        }

        short digits = service.getDigits();
        if (digits > 0) {
            etDigits.setText(String.valueOf(digits));
        } else {
            etDigits.setText(Service.DEFAULT_ALGORITHM_NUM_LENGHT);
        }

        short period = service.getPeriod();
        if (period > 0) {
            etPeriod.setText(String.valueOf(period));
        } else {
            etPeriod.setText(Service.DEFAULT_ALGORITHM_PERIOD);
        }

        if (!isExpanded) {
            toggleAdvancedSettings();
        }

        Toast.makeText(this, R.string.qr_data_loaded, Toast.LENGTH_SHORT).show();
    }

    private void addService() {
        String serviceName = Objects.requireNonNull(etServiceName.getText()).toString().trim();
        String secretKey = Objects.requireNonNull(etSecretKey.getText()).toString().trim();
        String issuer = Objects.requireNonNull(etIssuer.getText()).toString().trim();
        String account = Objects.requireNonNull(etAccount.getText()).toString().trim();
        String algorithm = Objects.requireNonNull(etAlgorithm.getText()).toString().trim();
        String digitsStr = Objects.requireNonNull(etDigits.getText()).toString().trim();
        String periodStr = Objects.requireNonNull(etPeriod.getText()).toString().trim();

        if (serviceName.isEmpty()) {
            showError(etServiceName, getString(R.string.enter_service_name));
            return;
        }

        if (secretKey.isEmpty()) {
            showError(etSecretKey, getString(R.string.enter_secret_key));
            return;
        }

        if (!TOTPGenerator.isValidSecretKey(secretKey)) {
            showError(etSecretKey, getString(R.string.key_must_be_in_base32_format));
            return;
        }

        if (issuer.isEmpty()) {
            showError(etIssuer, getString(R.string.issuer_required));
            return;
        }

        if (algorithm.isEmpty()) {
            algorithm = Service.DEFAULT_ALGORITHM;
        } else if (!TOTPGenerator.isValidAlgorithm(algorithm)) {
            showError(etAlgorithm, getString(R.string.invalid_algorithm));
            return;
        }

        short digits = Service.DEFAULT_ALGORITHM_NUM_LENGHT;
        try {
            if (!digitsStr.isEmpty()) {
                digits = Short.parseShort(digitsStr);
            }
            if (!TOTPGenerator.isValidDigits(digits)) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            showError(etDigits, getString(R.string.digits_must_be_between_4_and_10));
            return;
        }

        short period = Service.DEFAULT_ALGORITHM_PERIOD;
        try {
            if (!periodStr.isEmpty()) {
                period = Short.parseShort(periodStr);
            }
            if (!TOTPGenerator.isValidPeriod(period)) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ignored) {
            showError(etPeriod, getString(R.string.period_must_be_between_10_and_60));
            return;
        }

        long id = dbHelper.addService(serviceName, secretKey, account, issuer, algorithm, digits, period);
        if (id != -1) {
            Toast.makeText(this, R.string.service_successfully_added, Toast.LENGTH_SHORT).show();
            finishActivityWithAnim(RESULT_OK);
        } else {
            Toast.makeText(this, R.string.service_adding_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void validateSecretKey() {
        String secretKey = Objects.requireNonNull(etSecretKey.getText()).toString().trim();
        String algorithm = Objects.requireNonNull(etAlgorithm.getText()).toString().trim();
        String digitsStr = Objects.requireNonNull(etDigits.getText()).toString().trim();
        String periodStr = Objects.requireNonNull(etPeriod.getText()).toString().trim();

        if (secretKey.isEmpty()) {
            Toast.makeText(this, R.string.enter_secret_key_for_validation, Toast.LENGTH_SHORT).show();
            etSecretKey.requestFocus();
            etSecretKey.setError(getString(R.string.enter_secret_key_for_validation));
            return;
        }

        if (algorithm.isEmpty()) {
            algorithm = Service.DEFAULT_ALGORITHM;
        } else if (!TOTPGenerator.isValidAlgorithm(algorithm)) {
            showError(etAlgorithm, getString(R.string.invalid_algorithm));
            return;
        }

        short digits = Service.DEFAULT_ALGORITHM_NUM_LENGHT;
        try {
            if (!digitsStr.isEmpty()) {
                digits = Short.parseShort(digitsStr);
            }
            if (!TOTPGenerator.isValidDigits(digits)) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ignored) {
            showError(etDigits, getString(R.string.digits_must_be_between_4_and_10));
            return;
        }

        short period = Service.DEFAULT_ALGORITHM_PERIOD;
        try {
            if (!periodStr.isEmpty()) {
                period = Short.parseShort(periodStr);
            }
            if (!TOTPGenerator.isValidPeriod(period)) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ignored) {
            showError(etPeriod, getString(R.string.period_must_be_between_10_and_60));
            return;
        }

        if (TOTPGenerator.isValidSecretKey(secretKey)) {
            String testCode = TOTPGenerator.generateCode(secretKey, algorithm, digits, period);

            new android.app.AlertDialog.Builder(this)
                .setTitle(R.string.key_validation)
                .setMessage(getString(R.string.key_is_correct_test_code) + TOTPGenerator.formatCode(testCode))
                .setPositiveButton(R.string.ok_button, null)
                .show();
        } else {
            Toast.makeText(this, R.string.wrong_key_format, Toast.LENGTH_LONG).show();
            etSecretKey.requestFocus();
            etSecretKey.setError(getString(R.string.wrong_key_format));
        }
    }
    private boolean hasUnsavedChanges() {
        return !Objects.requireNonNull(etServiceName.getText()).toString().trim().isEmpty() ||
                !Objects.requireNonNull(etSecretKey.getText()).toString().trim().isEmpty() ||
                !Objects.requireNonNull(etAccount.getText()).toString().trim().isEmpty() ||
                !Objects.requireNonNull(etIssuer.getText()).toString().trim().isEmpty() ||
                !Objects.requireNonNull(etAlgorithm.getText()).toString().trim().isEmpty() ||
                !Objects.requireNonNull(etDigits.getText()).toString().trim().isEmpty() ||
                !Objects.requireNonNull(etPeriod.getText()).toString().trim().isEmpty();
    }

    private void showUnsavedChangesDialog() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.unsaved_changes)
            .setMessage(R.string.u_ve_got_unsaved_changes_are_u_sure_u_want_to_exit)
            .setPositiveButton(R.string.exit_button, (dialog, which) -> finishActivityWithAnim(RESULT_CANCELED))
            .setNegativeButton(R.string.cancel_button, (dialog, which) -> {})
            .setNeutralButton(R.string.save_button, (dialog, which) -> addService())
            .setIcon(R.drawable.ic_warning)
            .show();
    }
    private void showError(TextInputEditText editText, String message) {
        editText.requestFocus();
        editText.setError(message);
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (hasUnsavedChanges()) {
            showUnsavedChangesDialog();
        } else {
            finishActivityWithAnim(RESULT_CANCELED);
        }
        return true;
    }

    private void finishActivityWithAnim(int resultKey) {
        setResult(resultKey);
        finish();
        TransitionHelper.setOnClose(this, R.anim.slide_out_right);
    }
}