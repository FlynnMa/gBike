/*
 * Copyright (C) 2015 Daniel.Liu Tel:13818674825
 */

package com.vehicle.uart;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.vehicle.uart.BackScrollManager;
import com.vehicle.uart.CarouselContainer;
import java.lang.ref.WeakReference;
import java.util.Arrays;

import com.vehicle.uart.BindBT;

/**
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public class DummyListFragment extends ListFragment implements OnItemClickListener
{
	private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;

	/**
     * The header to bind the {@link BackScrollManager} to
     */
    private CarouselContainer mCarousel;

    /**
     * Empty constructor as per the {@link Fragment} docs
     */
    public DummyListFragment()
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        mCarousel = (CarouselContainer) activity.findViewById(R.id.carousel_header);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Simple ArrayAdapter
        final CarouselListAdapter adapter = new CarouselListAdapter(getActivity());

		if (!MainActivity.IsBluetoothConnected())
		{
       		adapter.add(this.getString(R.string.device_unbinding));
		}
		adapter.add("历史记录01");
		adapter.add("历史记录02");
		adapter.add("历史记录03");

        // Bind the data
        setListAdapter(adapter);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        final ListView listView = getListView();
        // Attach the BackScrollManager
        listView.setOnScrollListener(new BackScrollManager(mCarousel, null,
                CarouselContainer.TAB_INDEX_FIRST));
        // Register the onItemClickListener
        listView.setOnItemClickListener(this);
        // We disable the scroll bar because it would otherwise be incorrect
        // because of the hidden
        // header
        listView.setVerticalScrollBarEnabled(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        // This is the header
        if (position == 0)
		{
            return;
        }

        // Remember to substract one from the touched position
        final String str = (String) parent.getItemAtPosition(position - 1);
		EVLog.e("onItemClick " + str);

		// TODO:
		if(str.equals(this.getString(R.string.device_unbinding)))
		{
			if (!Feature.blSimulatorMode)
            {
            	if (!MainActivity.mBtAdapter.isEnabled())
				{
					EVLog.e("onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
                else
				{
            		// Open DeviceListActivity class, with popup windows that scan for devices
        			Intent newIntent = new Intent(view.getContext(), DeviceListActivity.class);
//                    newIntent.putExtra("intValue", 123);
        			startActivity(newIntent);
                }
			}
		}
    }

    /**
     * This is a special adapater used in conjunction with
     * {@link CarouselHeader} and {@link BackScrollManager}. In order to
     * smoothly animate the {@link CarouselHeader}, a faux header in placed at
     * position == 0 in the adapter. This isn't necessary to use the widget, but
     * it is if you want the animation to appear correct.
     */
    private static final class CarouselListAdapter extends ArrayAdapter<String>
    {
        /**
         * The header view
         */
        private static final int ITEM_VIEW_TYPE_HEADER = 0;

        /**
         * * The data in the list.
         */
        private static final int ITEM_VIEW_TYPE_DATA = 1;

        /**
         * Number of views (TextView, CarouselHeader)
         */
        private static final int VIEW_TYPE_COUNT = 2;

        /**
         * Fake header
         */
        private final View mHeader;

        /**
         * Constructor of <code>CarouselListAdapter</code>
         *
         * @param context The {@link Context} to use
         */
        public CarouselListAdapter(Context context)
        {
            super(context, 0);
            // Inflate the fake header
            mHeader = LayoutInflater.from(context).inflate(R.layout.faux_carousel, null);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            // Return a faux header at position 0
            if (position == 0)
			{
                return mHeader;
            }

            // Recycle ViewHolder's items
            ViewHolder holder;
            if (convertView == null)
			{
                convertView = LayoutInflater.from(getContext()).inflate(
                        android.R.layout.simple_list_item_1, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            }
			else
			{
                holder = (ViewHolder) convertView.getTag();
            }

            // Retieve the data, but make sure to call one less than the current
            // position to avoid counting the faux header.
            final String movies = getItem(position - 1);
            holder.mLineOne.get().setText(movies);
            return convertView;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasStableIds()
        {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long getItemId(int position)
        {
            if (position == 0)
			{
                return -1;
            }
            return position - 1;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getViewTypeCount()
        {
            return VIEW_TYPE_COUNT;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getItemViewType(int position)
        {
            if (position == 0)
			{
                return ITEM_VIEW_TYPE_HEADER;
            }
            return ITEM_VIEW_TYPE_DATA;
        }
    }

    private static final class ViewHolder
	{
        public WeakReference<TextView> mLineOne;

        /* Constructor of <code>ViewHolder</code> */
        public ViewHolder(View view)
        {
            // Initialize mLineOne
            mLineOne = new WeakReference<TextView>((TextView) view.findViewById(android.R.id.text1));
        }
    }
}
