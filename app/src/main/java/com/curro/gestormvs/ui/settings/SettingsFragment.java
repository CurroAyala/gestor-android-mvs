package com.curro.gestormvs.ui.settings;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.curro.gestormvs.databinding.FragmentSettingsBinding;


public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;


    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateButtonStyles();

        binding.btnSpanish.setOnClickListener(v -> changeLanguage("es"));
        binding.btnEnglish.setOnClickListener(v -> changeLanguage("en"));
    }

    private void changeLanguage(String languageCode) {
        LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(languageCode);
        AppCompatDelegate.setApplicationLocales(appLocale);
    }

    private void updateButtonStyles() {
        String currentLang = "en";

        LocaleListCompat currentLocales = AppCompatDelegate.getApplicationLocales();
        if (!currentLocales.isEmpty()) {
            java.util.Locale locale = currentLocales.get(0);
            if (locale != null) {
                currentLang = locale.getLanguage();
            }
        }

        if (currentLang.equals("es")) {
            setActiveStyle(binding.btnSpanish);
            setInactiveStyle(binding.btnEnglish);
        } else {
            setActiveStyle(binding.btnEnglish);
            setInactiveStyle(binding.btnSpanish);
        }
    }

    private void setActiveStyle(android.widget.Button button) {
        button.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        androidx.core.content.ContextCompat.getColor(
                                requireContext(), com.curro.gestormvs.R.color.primary)));
        button.setTextColor(
                androidx.core.content.ContextCompat.getColor(
                        requireContext(), com.curro.gestormvs.R.color.background));
    }

    private void setInactiveStyle(android.widget.Button button) {
        button.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        androidx.core.content.ContextCompat.getColor(
                                requireContext(), com.curro.gestormvs.R.color.surface_elevated)));
        button.setTextColor(
                androidx.core.content.ContextCompat.getColor(
                        requireContext(), com.curro.gestormvs.R.color.text_primary));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}