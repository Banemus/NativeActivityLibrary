package craftypotions.com.nativeplugin;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import com.unity3d.player.UnityPlayer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ContactDataRetriever
{
    // -- PUBLIC

    // .. OPERATIONS

    // .. FUNCTIONS

    public void LoadInformation(
        Activity activity,
        boolean load_photos,
        boolean load_numbers,
        boolean load_emails,
        boolean load_connections
        )
    {
        ContentResolver = activity.getContentResolver();

        ContactTable = new ArrayList();
        ContactsMap = new HashMap();

        try
        {
            LoadNames();

            if ( load_numbers )
            {
                LoadNumbers( activity );
            }

            if ( load_emails )
            {
                LoadEmails( activity );
            }

            if ( load_connections )
            {
                LoadConnections();
            }

            Contact.Buffer = ByteBuffer.allocate( 1048576 );
            Contact.Buffer.order( ByteOrder.LITTLE_ENDIAN );

            for ( int i = 0; i < ContactTable.size(); i++ )
            {
                Contact
                    contact;

                Contact.Buffer.clear();

                contact = ContactTable.get( i );

                if ( load_photos )
                {
                    contact.Photo = LoadPhoto( contact );
                }

                UnityPlayer.UnitySendMessage(
                    "ContactsListMessageReceiver",
                    "OnContactReady",
                    Integer.toString( i )
                );

                while ( !contact.unityGotThis )
                {
                    Thread.sleep( 1L );
                }
            }

            UnityPlayer.UnitySendMessage( "ContactsListMessageReceiver", "OnInitializeDone", "" );
            ContactTable = null;
            ContactsMap = null;
            Contact.Buffer = null;
        }
        catch ( Exception e )
        {
            String error = Log.getStackTraceString( e );
            UnityPlayer.UnitySendMessage( "ContactsListMessageReceiver", "OnInitializeFail", error );
        }
    }

    // ~~

    public byte[] GetContact(
        int id
        )
    {
        Contact
            contact;
        byte[]
            data;

        contact = ContactTable.get( id );
        data = contact.toBytes();
        contact.unityGotThis = true;

        return data;
    }

    // -- PRIVATE

    // .. OPERATIONS

    void LoadNames()
    {
        Cursor
            cursor;

        cursor = null;

        cursor = ContentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            new String[]{ "_id", "display_name" },
            null,
            null,
            "display_name COLLATE LOCALIZED ASC"
        );

        if ( cursor.getCount() > 0 )
        {
            while ( cursor.moveToNext() )
            {
                Contact item = new Contact();
                item.Id = cursor.getString( cursor.getColumnIndex( "_id" ) );
                item.Name = cursor.getString( cursor.getColumnIndex( "display_name" ) );
                item.PhoneTable = new ArrayList();
                item.EmailTable = new ArrayList();
                item.ConnectionTable = new ArrayList();

                ContactsMap.put( item.Id, item );
                ContactTable.add( item );
            }
        }
    }

    // ~~

    void LoadNumbers(
        Activity activity
        )
    {
        Cursor
            phone;

        phone = ContentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            new String[]{ "data1", "data2", "contact_id" },
            null,
            null,
            null
        );

        if ( phone.moveToFirst() )
        {
            int
                contactNumberColumnIndex,
                contactTypeColumnIndex,
                contactIdColumnIndex;

            contactNumberColumnIndex = phone.getColumnIndex( "data1" );
            contactTypeColumnIndex = phone.getColumnIndex( "data2" );
            contactIdColumnIndex = phone.getColumnIndex( "contact_id" );

            while ( !phone.isAfterLast() )
            {
                String
                    number,
                    contactId;
                Contact
                    contact;

                number = phone.getString( contactNumberColumnIndex );
                contactId = phone.getString( contactIdColumnIndex );
                contact = ContactsMap.get( contactId );

                if ( contact == null )
                {
                    phone.moveToNext();
                }
                else
                {
                    int
                        type;
                    String
                        phone_type;
                    ContactPhone
                        contact_phone;

                    type = phone.getInt( contactTypeColumnIndex );

                    phone_type = ( String ) ContactsContract.CommonDataKinds.Phone.getTypeLabel(
                        activity.getResources(),
                        type,
                        "Mobile"
                    );

                    if ( phone_type.equals( "Custom" ) )
                    {
                        phone_type = phone.getString( phone.getColumnIndex( "data3" ) );
                    }

                    contact_phone = new ContactPhone();
                    contact_phone.Number = number;
                    contact_phone.Type = phone_type;
                    contact.PhoneTable.add( contact_phone );
                    phone.moveToNext();
                }
            }
        }
        phone.close();
    }

    // ~~

    void LoadEmails(
        Activity activity
        )
    {
        Cursor
            email;

        email = ContentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            new String[]{ "data1", "data2", "contact_id" },
            null,
            null,
            null
        );

        if ( email.moveToFirst() )
        {
            int
                contact_email_column_index ,
                contact_type_column_index,
                contact_id_columns_index;

            contact_email_column_index = email.getColumnIndex( "data1" );
            contact_type_column_index = email.getColumnIndex( "data2" );
            contact_id_columns_index = email.getColumnIndex( "contact_id" );

            while ( !email.isAfterLast() )
            {
                String
                    address,
                    contact_id,
                    customLabel;
                int
                    type;
                Contact
                    contact;

                address = email.getString( contact_email_column_index );
                contact_id = email.getString( contact_id_columns_index );
                type = email.getInt( contact_type_column_index );
                customLabel = "Custom";

                contact = ContactsMap.get( contact_id );

                if ( contact == null )
                {
                    email.moveToNext();
                }
                else
                {
                    CharSequence
                        email_type;
                    ContactEmail
                        contact_email;

                    email_type = ContactsContract.CommonDataKinds.Email.getTypeLabel(
                        activity.getResources(),
                        type,
                        customLabel
                    );

                    contact_email = new ContactEmail();
                    contact_email.Address = address;
                    contact_email.Type = email_type.toString();
                    contact.EmailTable.add( contact_email );
                    email.moveToNext();
                }
            }
        }
        email.close();
    }

    // ~~

    void LoadConnections()
    {
        Cursor
            connection;

        connection = ContentResolver.query(
            ContactsContract.RawContacts.CONTENT_URI,
            new String[]{ "contact_id", "account_name" },
            null,
            null,
            null
        );

        if ( connection.moveToFirst() )
        {
            int
                id_column_index,
                connection_column_index;

            id_column_index = connection.getColumnIndex( "contact_id" );
            connection_column_index = connection.getColumnIndex( "account_name" );

            while ( !connection.isAfterLast() )
            {
                String
                    contact_id,
                    connection_name;
                Contact
                    contact;

                contact_id = connection.getString( id_column_index );
                connection_name = connection.getString( connection_column_index );

                contact = ContactsMap.get( contact_id );

                if ( contact == null )
                {
                    connection.moveToNext();
                }
                else
                {
                    contact.ConnectionTable.add( connection_name );
                    connection.moveToNext();
                }
            }
        }
        connection.close();
    }

    // ~~

    byte[] LoadPhoto(
        Contact item
    )
    {
        try
        {
            long
                contact_id;
            Uri
                photo_uri;
            Cursor
                cursor;

            contact_id = Long.parseLong( item.Id );

            Uri contactUri = ContentUris.withAppendedId(
                ContactsContract.Contacts.CONTENT_URI,
                contact_id
            );

            photo_uri = Uri.withAppendedPath( contactUri, "photo" );

            cursor = ContentResolver.query( photo_uri, new String[]{ "data15" }, null, null, null );

            if ( cursor == null )
            {
                return null;
            }
            try
            {
                if ( cursor.moveToFirst() )
                {
                    byte[]
                        data;

                    data = cursor.getBlob( 0 );
                    if ( data != null )
                    {
                        return data;
                    }
                }
            }
            finally
            {
                cursor.close();
            }

            return null;
        }
        catch ( Exception e )
        {
            logException( e );
        }
        return null;
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

    Map< String, Contact >
        ContactsMap = null;
    ArrayList< Contact >
        ContactTable = null;
    ContentResolver
        ContentResolver = null;
}