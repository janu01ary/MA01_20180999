package ddwu.moblie.finalproject.ma01_20180999;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Performance implements Serializable {

    private long _id;
    private String title;
    private String venue;
    private String period;
    private String memo;
    private String imgPath;

    public Performance() {}

    public Performance(long _id, String title, String venue, String period) {
        this._id = _id;
        this.title = title;
        this.venue = venue;
        this.period = period;
    }

    public Performance(long _id, String title, String venue, String period, String memo, String imgPath) {
        this._id = _id;
        this.title = title;
        this.venue = venue;
        this.period = period;
        this.memo = memo;
        this.imgPath = imgPath;
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) { this.imgPath = imgPath; }

    @NonNull
    @Override
    public String toString() {
        return "제목: " + getTitle() + "\n장소: " + getVenue() + "\n기간: " + getPeriod();
    }
}
