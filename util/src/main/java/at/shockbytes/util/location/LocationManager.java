package at.shockbytes.util.location;

import android.location.Location;
import android.support.annotation.NonNull;

/**
 * @author Martin Macheiner
 *         Date: 05.09.2017.
 */

public interface LocationManager {

    interface OnLocationUpdateListener {

        void onConnected();

        void onDisconnected();

        void onError(Exception e);

        void onLocationUpdate(Location location);

    }

    void start(@NonNull OnLocationUpdateListener listener);

    void stop();

    void setUpdateInterval(long updateMillis);

    boolean isLocationUpdateRequested();

}