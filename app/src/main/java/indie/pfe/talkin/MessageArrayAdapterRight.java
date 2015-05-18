package indie.pfe.talkin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mustapha on 18/05/2015.
 */
public class MessageArrayAdapterRight extends ArrayAdapter<MessageItem> {
    private TextView CountryName;
    private List<MessageItem> countries = new ArrayList<>();
    private LinearLayout wrapper;

    public MessageArrayAdapterRight(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    @Override
    public int getCount() {
        return this.countries.size();
    }

    @Override
    public void add(MessageItem object) {
        countries.add(object);
        super.add(object);
    }

    public MessageItem getItem(int index) {
        return this.countries.get(index);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.activity_main_item_right_message, parent, false);
        }
        wrapper = (LinearLayout) row.findViewById(R.id.wrapperR);
        MessageItem message = getItem(position);
        CountryName = (TextView) row.findViewById(R.id.messageItemR);
        CountryName.setText(message.message);
        wrapper.setRight(10);

        return row;
    }

    public Bitmap decodeToBitmap(byte[] decodedByte) {
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }
}
