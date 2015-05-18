package indie.pfe.talkin;

import android.app.Application;

/**
 * Created by Mustapha on 04/05/2015.
 */

public class EXTRA_USERNAME extends Application {
    private String displayname;

    public String getDisplayname() {
        return displayname;
    }

    public void setDisplayname(String name) {
        this.displayname = name;
    }

    public void onCreate() {
        //hello world!
    }
}
