package craftypotions.com.nativeplugin;

import android.util.Log;
import com.unity3d.player.UnityPlayer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;

public class Contact
{
    // -- PUBLIC

    // .. OPERATIONS

    public byte[] toBytes()
    {
        try
        {
            putString( Id );
            putString( Name );

            if ( Photo == null )
            {
                log( "write empty photo" );
                Buffer.putShort( ( short ) 0 );
            }
            else
            {
                log( "write photo length == " + Photo.length );
                Buffer.putShort( ( short ) Photo.length );
                Buffer.put( Photo );
            }

            if ( ( PhoneTable == null ) || ( PhoneTable.size() == 0 ) )
            {
                log( "write empty Phones" );
                Buffer.putShort( ( short ) 0 );
            }
            else
            {
                log( "write Phones size == " + PhoneTable.size() );
                Buffer.putShort( ( short ) PhoneTable.size() );
                for ( int i = 0; i < PhoneTable.size(); i++ )
                {
                    putString( PhoneTable.get( i ).Number );
                    putString( PhoneTable.get( i ).Type );
                }
            }

            if ( ( this.EmailTable == null ) || ( this.EmailTable.size() == 0 ) )
            {
                log( "write empty Emails" );
                Buffer.putShort( ( short ) 0 );
            }
            else
            {
                log( "write Emails size == " + EmailTable.size() );
                Buffer.putShort( ( short ) EmailTable.size() );

                for ( int i = 0; i < EmailTable.size(); i++ )
                {
                    putString( EmailTable.get( i ).Address );
                    putString( EmailTable.get( i ).Type );
                }
            }
            if ( ( ConnectionTable == null ) || ( ConnectionTable.size() == 0 ) )
            {
                log( "write empty Connections" );
                Buffer.putShort( ( short ) 0 );
            }
            else
            {
                log( "write Connections size == " + ConnectionTable.size() );
                Buffer.putShort( ( short ) ConnectionTable.size() );
                for ( int i = 0; i < ConnectionTable.size(); i++ )
                {
                    putString( ( String ) ConnectionTable.get( i ) );
                }
            }
            return Buffer.array();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            logException( e );
        }
        return null;
    }

    // -- PRIVATE

    // .. OPERATIONS

    void putString(
        String value
        )
    {
        if ( ( value == null ) || ( value == "" ) )
        {
            log( "write empty string " );
            Buffer.putShort( ( short ) 0 );
        }
        else
        {
            byte[]
                data;

            data = value.getBytes( Utf8 );
            Buffer.putShort( ( short ) data.length );
            Buffer.put( data );
            log( "write string " + value + " length is " + value.length() );
        }
    }

    // ~~

    void log(
        String message
        )
    {
    }

    // ~~

    void logException(
        Exception e
        )
    {
        UnityPlayer.UnitySendMessage( "ContactsListMessageReceiver", "Error", Log.getStackTraceString( e ) );
        e.printStackTrace();
    }

    // .. ATTRIBUTES

    Charset
        Utf8 = Charset.forName("UTF-8");
    String
        Id,
        Name;
    byte[]
        Photo;
    List<ContactPhone>
        PhoneTable;
    List<ContactEmail>
        EmailTable;
    List<String>
        ConnectionTable;
    public static ByteBuffer
        Buffer = null;
    volatile boolean
        unityGotThis;
}
