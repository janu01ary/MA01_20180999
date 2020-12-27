package ddwu.moblie.finalproject.ma01_20180999;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class PerformanceCursorAdapter extends CursorAdapter {

    LayoutInflater inflater;
    int layout;

    public PerformanceCursorAdapter(Context context, int layout, Cursor c) {
        super(context, c, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.layout = layout;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view = inflater.inflate(layout, viewGroup, false);
        ViewHolder holder = new ViewHolder();
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        if (holder.tvPerformanceTitle == null) {
            holder.tvPerformanceTitle = view.findViewById(R.id.tvPerformanceTitle);
            holder.tvPerformanceVenue = view.findViewById(R.id.tvPerformanceVenue);
            holder.tvPerformancePeriod = view.findViewById(R.id.tvPerformancePeriod);
        }

        holder.tvPerformanceTitle.setText(cursor.getString(cursor.getColumnIndex(PerformanceDBHelper.COL_TITLE)));
        holder.tvPerformanceVenue.setText(cursor.getString(cursor.getColumnIndex(PerformanceDBHelper.COL_VENUE)));
        holder.tvPerformancePeriod.setText(cursor.getString(cursor.getColumnIndex(PerformanceDBHelper.COL_PERIOD)));

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
