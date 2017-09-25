package craftypotions.com.nativeplugin;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import com.google.firebase.MessagingUnityPlayerActivity;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

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
            case PERMISSION_REQUEST_INTERNET:
            case PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE:
            case PERMISSION_REQUEST_CAMERA:
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

    // ~~

    public void ShareMedia(
        String text,
        String subject,
        String imagePath
        )
    {
        Intent
            share_intent;
        boolean
            image_added;
        File
            file;

        share_intent = new Intent( Intent.ACTION_SEND );

        if ( text != null && !text.isEmpty() )
        {
            share_intent.putExtra( Intent.EXTRA_TEXT, text );
        }

        if ( subject != null && !subject.isEmpty() )
        {
            share_intent.putExtra( Intent.EXTRA_SUBJECT, subject );
        }

        image_added = false;
        file = PrepareFileForShare( imagePath );

        if ( file != null )
        {
            try
            {
                share_intent.putExtra(
                    Intent.EXTRA_STREAM,
                    FileProvider.getUriForFile(
                        this,
                        getPackageName() + ".fileprovider",
                        file
                    )
                );

                share_intent.addFlags( Intent.FLAG_GRANT_READ_URI_PERMISSION );
                image_added = true;
            }
            catch ( IllegalArgumentException var8 )
            {
                var8.printStackTrace();
            }
        }

        if ( image_added )
        {
            share_intent.setType( "image/*" );
        }
        else
        {
            share_intent.setType( "text/plain" );
        }

        startActivity( Intent.createChooser( share_intent, "Share via..." ) );
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

            case PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE:
            {
                return Manifest.permission.WRITE_EXTERNAL_STORAGE;
            }

            case PERMISSION_REQUEST_INTERNET:
            {
                return Manifest.permission.INTERNET;
            }

            case PERMISSION_REQUEST_CAMERA:
            {
                return Manifest.permission.CAMERA;
            }
        }

        return "";
    }

    // ~~

    File PrepareFileForShare(
        String filePath
        )
    {
        if ( filePath != null && !filePath.isEmpty() )
        {
            File
                old_file = new File( filePath );

            if ( !old_file.exists() )
            {
                return null;
            }
            else
            {
                File
                    storage_dir,
                    result;
                FileChannel
                    outputChannel,
                    inputChannel;

                storage_dir = new File( getFilesDir(), "shared_images" );

                if ( !storage_dir.exists() )
                {
                    storage_dir.mkdirs();
                }

                outputChannel = null;
                inputChannel = null;

                try
                {
                    result = new File( storage_dir, "image.png" );

                    outputChannel = ( new FileOutputStream( result ) ).getChannel();
                    inputChannel = ( new FileInputStream( old_file ) ).getChannel();
                    inputChannel.transferTo( 0L, inputChannel.size(), outputChannel );
                    inputChannel.close();
                }
                catch ( IOException var22 )
                {
                    result = null;
                }
                finally
                {
                    if ( inputChannel != null )
                    {
                        try
                        {
                            inputChannel.close();
                        }
                        catch ( IOException var21 )
                        {
                            var21.printStackTrace();
                        }
                    }

                    if ( outputChannel != null )
                    {
                        try
                        {
                            outputChannel.close();
                        }
                        catch ( IOException var20 )
                        {
                            var20.printStackTrace();
                        }
                    }
                }

                return result;
            }
        }
        else
        {
            return null;
        }
    }

    // .. VARIABLES

    static final int
        PERMISSION_REQUEST_READ_CONTACTS = 0,
        PERMISSION_REQUEST_CAPTURE_CAMERA = 1,
        PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 2,
        PERMISSION_REQUEST_INTERNET = 3,
        PERMISSION_REQUEST_CAMERA = 4;
}
