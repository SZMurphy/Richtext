package com.szmurphy.app;

import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.szmurphy.richtext.ExtendTagHandler;
import com.szmurphy.richtext.HtmlTagHandler;
import com.szmurphy.richtext.R;
import com.szmurphy.richtext.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String testText = "<p><strong>Sl<em><span style=\"font-size: 14px;\">ide </span>L</em>ef<span style=\"text-decoration: underline;\">tsd</span><span style=\"font-size: 48px;\"><span style=\"color: #f1c40f;\"><span style=\"text-decoration: underline;\">f</span>a</span><span style=\"text-decoration: underline;\"><span style=\"color: #f1c40f; text-decoration: underline;\">s</span>d</span></span><span style=\"text-decoration: underline;\"><span style=\"font-size: 20px;\">f</span></span><span style=\"font-size: 48px;\"><span style=\"font-size: 20px;\"><span style=\"text-decoration: underline;\">adfs</span><span style=\"font-size: 30px;\"><span style=\"text-decoration: underline;\">s</span>s</span><span style=\"text-decoration: underline;\">s</span><em><span style=\"text-decoration: underline;\">s</span><span style=\"font-size: 48px;\"><span style=\"text-decoration: underline;\">s</span>s</span></em>sssssss</span>ss<span style=\"color: #e03e2d;\">d</span><span style=\"color: #95a5a6;\"><span style=\"color: #e03e2d;\">s<span style=\"font-size: 20px;\">fa</span></span>aa</span></span></strong></p>";
        Spanned s = HtmlTagHandler.fromHtml(testText,null,new ExtendTagHandler(view.getContext(), binding.textviewFirst.getTextColors()));
        if(!TextUtils.isEmpty(s)) {
            binding.textviewFirst.setMovementMethod(LinkMovementMethod.getInstance());
            binding.textviewFirst.setText(s);
        } else {
            binding.textviewFirst.setText(Html.fromHtml(testText));//Html.FROM_HTML_MODE_COMPACT
        }

        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}