// Generated by view binder compiler. Do not edit!
package com.example.schedulerapp.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.schedulerapp.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class FragmentDayBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final RecyclerView recyclerViewLessons;

  @NonNull
  public final TextView textViewDayName;

  @NonNull
  public final TextView textViewEmptyState;

  private FragmentDayBinding(@NonNull LinearLayout rootView,
      @NonNull RecyclerView recyclerViewLessons, @NonNull TextView textViewDayName,
      @NonNull TextView textViewEmptyState) {
    this.rootView = rootView;
    this.recyclerViewLessons = recyclerViewLessons;
    this.textViewDayName = textViewDayName;
    this.textViewEmptyState = textViewEmptyState;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static FragmentDayBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static FragmentDayBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.fragment_day, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static FragmentDayBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.recyclerViewLessons;
      RecyclerView recyclerViewLessons = ViewBindings.findChildViewById(rootView, id);
      if (recyclerViewLessons == null) {
        break missingId;
      }

      id = R.id.textViewDayName;
      TextView textViewDayName = ViewBindings.findChildViewById(rootView, id);
      if (textViewDayName == null) {
        break missingId;
      }

      id = R.id.textViewEmptyState;
      TextView textViewEmptyState = ViewBindings.findChildViewById(rootView, id);
      if (textViewEmptyState == null) {
        break missingId;
      }

      return new FragmentDayBinding((LinearLayout) rootView, recyclerViewLessons, textViewDayName,
          textViewEmptyState);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
