package com.ui;


import com.vehicle.uart.R;
import com.vehicle.uart.R.layout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class FragMainView extends Fragment{
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	private View rootView;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public FragMainView() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		if (getArguments().containsKey(ARG_ITEM_ID)) {
			// Load the dummy content specified by the fragment
			// arguments. In a real-world scenario, use a Loader
			// to load content from a content provider.
//			mItem = DummyContent.ITEM_MAP.get(getArguments().getString(
//					ARG_ITEM_ID));
//		}
	}

	private Button.OnClickListener button_listener = new Button.OnClickListener(){
		public void onClick(View v) {
//		    ((TextView) rootView.findViewById(R.id.frag_detail)).setText("I am clicked");
		}
		};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.frag_mainview,
				container, false);

		// Show the dummy content as text in a TextView.
//		if (mItem != null) {
//			((TextView) rootView.findViewById(R.id.frag_detail))
//					.setText(mItem.content);
//		}
		
//		Button myButton = (Button)rootView.findViewById(R.id.button1);
//		myButton.setOnClickListener(button_listener);

		return rootView;
	}
}
