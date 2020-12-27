package ddwu.moblie.finalproject.ma01_20180999;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class PerformanceAdapter extends BaseAdapter {
    private Context context;                //inflater 객체 생성 시 필요
    private int layout;                     //AdapterView 항목에 대한 layout
    private ArrayList<Performance> myDataList;   //원본 데이터 리스트
    private LayoutInflater inflater;  //inflater 객체

    public PerformanceAdapter(Context context, int layout, ArrayList<Performance> myDataList) {
        this.context = context;
        this.layout = layout;
        this.myDataList = myDataList;

        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return myDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return myDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return myDataList.get(position).get_id();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final int pos = position;
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(layout, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.tvPerformanceTitle = convertView.findViewById(R.id.tvPerformanceTitle);
            viewHolder.tvPerformanceVenue = convertView.findViewById(R.id.tvPerformanceVenue);
            viewHolder.tvPerformancePeriod = convertView.findViewById(R.id.tvPerformancePeriod);

            convertView.setTag(viewHolder); //임의의 객체를 convertView에 저장? 아무튼 해놓으면 getTag 할 수 있음
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

//        button.setFocusable(false);

        viewHolder.tvPerformanceTitle.setText(myDataList.get(position).getTitle());
        viewHolder.tvPerformanceVenue.setText(myDataList.get(position).getVenue());
        viewHolder.tvPerformancePeriod.setText(myDataList.get(position).getPeriod());

        return convertView;
    }

    public void setList(ArrayList<Performance> list) {
        this.myDataList = list;
        notifyDataSetChanged();
    }

    static class ViewHolder {
        //명시적으로
        public ViewHolder() {
            tvPerformanceTitle = null;
            tvPerformanceVenue = null;
            tvPerformancePeriod = null;
        }

        TextView tvPerformanceTitle;
        TextView tvPerformanceVenue;
        TextView tvPerformancePeriod;
    }
}
