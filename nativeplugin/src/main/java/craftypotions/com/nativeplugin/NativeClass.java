package craftypotions.com.nativeplugin;

import android.app.Activity;

public class NativeClass
{
    // -- PUBLIC

    // .. FUNCTIONS

    public static void LoadContacts(
        final Activity activity,
        final boolean load_photos,
        final boolean load_numbers,
        final boolean load_emails,
        final boolean load_connections
    )
    {
        Runnable
            runnable;

        if ( ContactDataRetriever == null )
        {
            ContactDataRetriever = new ContactDataRetriever();
        }

        runnable = new Runnable()
        {
            public void run()
            {
                ContactDataRetriever.LoadInformation( activity, load_photos, load_numbers, load_emails, load_connections );
            }
        };

        new Thread( runnable ).start();
    }

    // ~~

    public static byte[] GetContact(
        int id
    )
    {
        return ContactDataRetriever.GetContact( id );
    }

    // -- PROTECTED

    // .. OPERATIONS

    // .. VARIABLES

    public static ContactDataRetriever
        ContactDataRetriever;

    // -- PRIVATE

    // .. ATTRIBUTES
}
