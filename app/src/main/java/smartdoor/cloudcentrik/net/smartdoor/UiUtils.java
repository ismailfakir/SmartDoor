package smartdoor.cloudcentrik.net.smartdoor;

import android.content.Context;
import android.widget.Toast;

public class UiUtils {

    public static void showInfo(String info,Context context){

        Toast.makeText(context,info,Toast.LENGTH_LONG).show();

    }
}
