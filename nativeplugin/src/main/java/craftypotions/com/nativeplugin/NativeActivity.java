package craftypotions.com.nativeplugin;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import com.google.firebase.MessagingUnityPlayerActivity;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;
import android.os.Bundle;
import android.util.Log;

public class NativeActivity
    extends MessagingUnityPlayerActivity
{
    // -- PUBLIC

    // .. OPERATIONS

    @Override
    protected void onCreate(
        Bundle savedInstanceState
        )
    {
        super.onCreate( savedInstanceState );
        currentActivity = this;
    }

    // ~~

    public void RequestPermission(
        int type
        )
    {
        ActivityCompat.requestPermissions(
            this,
            new String[]{ ConvertTypeToString( type ) },
            type
        );
    }

    // ~~

    public boolean HasPermission(
        int type
        )
    {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
            this,
            ConvertTypeToString( type )
        );
    }

    // ~~

    @Override
    public void onRequestPermissionsResult(
        int request_code,
        String permission_table[],
        int[] grant_result_table
        )
    {
        super.onRequestPermissionsResult( request_code, permission_table, grant_result_table );

        switch ( request_code )
        {
            case PERMISSION_REQUEST_READ_CONTACTS:
            case PERMISSION_REQUEST_CAPTURE_CAMERA:
            {
                if ( grant_result_table.length > 0 && grant_result_table[ 0 ] == PackageManager.PERMISSION_GRANTED )
                {
                    UnityPlayer.UnitySendMessage( "PermissionMessageReceiver",
                                                  "OnPermissionGranted",
                                                  Integer.toString( request_code )
                    );
                }
                else
                {
                    UnityPlayer.UnitySendMessage( "PermissionMessageReceiver",
                                                  "OnPermissionDenied",
                                                  Integer.toString( request_code )
                    );

                }
                break;
            }
        }
    }

    // .. VARIABLES

    static public NativeActivity
        currentActivity;

    // -- PRIVATE

    // .. OPERATIONS

    String ConvertTypeToString(
        int type
        )
    {
        switch( type )
        {
            case PERMISSION_REQUEST_READ_CONTACTS:
            {
                return Manifest.permission.READ_CONTACTS;
            }

            case PERMISSION_REQUEST_CAPTURE_CAMERA:
            {
                return Manifest.permission.CAPTURE_VIDEO_OUTPUT;
            }
        }

        return "";
    }

    // .. VARIABLES

    static final int
        PERMISSION_REQUEST_READ_CONTACTS = 0,
        PERMISSION_REQUEST_CAPTURE_CAMERA = 1;
}
