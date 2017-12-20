package com.secuchat.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.secuchat.DBObjects.ChatRoomRecord;
import com.secuchat.R;
import java.util.ArrayList;

public class ChatRoomsViewAdapter extends ArrayAdapter<ChatRoomRecord> {

    private final Context context;
    private final ArrayList<ChatRoomRecord> roomIDs;
 

    public ChatRoomsViewAdapter(Context c, ArrayList<ChatRoomRecord> array) {
        super(c, R.layout.chat_room_list_item, R.id.secondLine, array);
        this.context = c;
        this.roomIDs = array;

    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {

            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            // Create new cell
            v = vi.inflate(R.layout.chat_room_list_item, null);

            if (getItem(position) != null) { //Check if null

                TextView tt = (TextView) v.findViewById(R.id.user);
                TextView tt1 = (TextView) v.findViewById(R.id.secondLine);
                //ImageView imgView = (ImageView) v.findViewById(R.id.icon);

                if (tt != null) {
                    tt.setText("Arnold");
                }
                if (tt1 != null) {
                    // Set to correct label
                    tt1.setText(roomIDs.get(position).getLabel());
                }
            }
        }
        return v;
    }
}

