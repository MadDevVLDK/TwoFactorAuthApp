package ru.superplushkin.twofactorauthapp.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.os.BundleCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import ru.superplushkin.twofactorauthapp.R;
import ru.superplushkin.twofactorauthapp.model.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ServiceDetailsBottomSheet extends BottomSheetDialogFragment {

    private Service service;
    private OnServiceBottomSheetClickListener clickListener;


    public static ServiceDetailsBottomSheet newInstance(Service service) {
        var fragment = new ServiceDetailsBottomSheet();
        var args = new Bundle();
        args.putParcelable("service", service);
        fragment.setArguments(args);

        return fragment;
    }
    public void setClickListener(OnServiceBottomSheetClickListener listener) {
        this.clickListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            service = BundleCompat.getParcelable(getArguments(), "service", Service.class);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_service_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvServiceName = view.findViewById(R.id.tvServiceName);
        TextView tvAccount = view.findViewById(R.id.tvAccount);
        TextView tvIssuer = view.findViewById(R.id.tvIssuer);
        TextView tvCreatedAt = view.findViewById(R.id.tvCreatedAt);
        TextView tvUsageCount = view.findViewById(R.id.tvUsageCount);
        TextView tvAlgorithm = view.findViewById(R.id.tvAlgorithm);
        TextView tvDigits = view.findViewById(R.id.tvDigits);
        TextView tvPeriod = view.findViewById(R.id.tvPeriod);

        MaterialButton btnQR = view.findViewById(R.id.btnQR);
        MaterialButton btnEdit = view.findViewById(R.id.btnEdit);
        MaterialButton btnDelete = view.findViewById(R.id.btnDelete);

        if (service != null) {
            String serviceName = service.getServiceName();
            tvServiceName.setText(serviceName);
            tvServiceName.setOnClickListener(v -> copyCodeToClipboard(serviceName));

            String account = service.getAccount();
            tvAccount.setText(account != null && !account.isEmpty() ? account : getString(R.string.not_specified));
            tvAccount.setOnClickListener(v -> copyCodeToClipboard(account));
            
            String issuer = service.getIssuer();
            tvIssuer.setText(issuer != null && !issuer.isEmpty() ? issuer : getString(R.string.not_specified));
            tvIssuer.setOnClickListener(v -> copyCodeToClipboard(issuer));

            Date date = new Date(service.getCreatedAt());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            tvCreatedAt.setText(outputFormat.format(date));

            tvUsageCount.setText(String.format(Locale.getDefault(), getString(R.string.service_used_times), service.getUsageCount()));
            tvAlgorithm.setText(service.getAlgorithm());
            tvDigits.setText(String.valueOf(service.getDigits()));
            tvPeriod.setText(String.valueOf(service.getPeriod()));
        }

        btnQR.setOnClickListener(v -> {
            if (clickListener != null && service != null) {
                clickListener.onQRClick(service);
            }
            dismiss();
        });

        btnEdit.setOnClickListener(v -> {
            if (clickListener != null && service != null) {
                clickListener.onEditClick(service);
            }
            dismiss();
        });

        btnDelete.setOnClickListener(v -> {
            if (clickListener != null && service != null) {
                clickListener.onDeleteClick(service);
            }
            dismiss();
        });
    }

    private void copyCodeToClipboard(String text) {
        try {
            ClipData clip = ClipData.newPlainText("2FA Data", text);
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(clip);
        } catch (Exception e) {
            Toast.makeText(getContext(), "✗ Copy failed", Toast.LENGTH_SHORT).show();
        }
    }

    public interface OnServiceBottomSheetClickListener {
        void onQRClick(Service service);
        void onEditClick(Service service);
        void onDeleteClick(Service service);
    }
}