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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mustapha Essouri on 01/05/2015.
 */
public class MessageArrayAdapter extends ArrayAdapter<MessageItem> {
    private TextView CountryName;
    private TextView CountrySurname;
    private List<MessageItem> countries = new ArrayList<MessageItem>();
    private LinearLayout wrapper;
    private LinearLayout wrapperRtl;

    public MessageArrayAdapter(Context context, int textViewResourceId) {
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
            row = inflater.inflate(R.layout.activity_main_item_message, parent, false);
        }

        TextView dayz = (TextView) row.findViewById(R.id.dayZ);

        long date = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm:ss a");
        //SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E LLLL");
        //String dayZ = simpleDateFormat.format(date);
        String dateString = sdf.format(date);
        //dayz.setText(dayZ);
        TextView textView = (TextView) row.findViewById(R.id.TimeDate);
        TextView textView1 = (TextView) row.findViewById(R.id.TimeDateRtl);

        wrapper = (LinearLayout) row.findViewById(R.id.wrapper);
        //wrapperRtl = (LinearLayout) row.findViewById(R.id.wrapperRtl);
        MessageItem message = getItem(position);
        CountrySurname = (TextView) row.findViewById(R.id.messageItemRtl);
        CountryName = (TextView) row.findViewById(R.id.messageItem);
        if (!message.left) {

            wrapperRtl.setVisibility(View.VISIBLE);
            CountrySurname.setText(message.message);
            wrapperRtl.setRight(10);
            textView1.setText(dateString);

        } else {

            wrapper.setVisibility(View.VISIBLE);
            CountryName.setText(message.message);
            //CountryName.setBackgroundResource(message.left ? R.drawable.msg_in : R.drawable.msg_out);
            //CountryName.setTextColor(message.left ? getContext().getResources().getColor(R.color.black) : getContext().getResources().getColor(R.color.black));
            //wrapper.setGravity(message.left ? Gravity.LEFT : Gravity.RIGHT);
            wrapper.setLeft(10);
            textView.setText(dateString);
        }


        return row;

    }

    public Bitmap decodeToBitmap(byte[] decodedByte) {
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

}
