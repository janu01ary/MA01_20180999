package ddwu.moblie.finalproject.ma01_20180999;

import android.content.Context;
import android.widget.Toast;

public class AddressNotFoundException extends Exception {

    public AddressNotFoundException() {
        super();
    }

    public AddressNotFoundException(String arg0) {
        super(arg0);
    }

    public AddressNotFoundException(String arg0, Context context) {
        super(arg0);
        Toast.makeText(context, arg0, Toast.LENGTH_SHORT).show();
    }
}