package com.qou.h7se.sbr.backup;

import android.content.Context;
import android.provider.ContactsContract;

import com.qou.h7se.sbr.ContentProviderHandlerEx;

/**
 * Created by k0de9x on 10/17/2015.
 */
public class Contacts {
        public static void run(Context context, final ContentProviderHandlerEx.XDExport.OnXDDataCallback dataCallback, final ContentProviderHandlerEx.XDExport.OnXDProgressCallback progressCallback, final ContentProviderHandlerEx.XDExport.OnXDDataReadyCallback dataReadyCallback) {
            final ContentProviderHandlerEx.XDExport.Provider contacts = new ContentProviderHandlerEx.XDExport.Provider(ContactsContract.Contacts.CONTENT_URI, null, null, null);

//                final Provider numbers = new Provider(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, ContactsContract.Contacts.HAS_PHONE_NUMBER, "1" , ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
//                final Provider emails = new Provider(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID);
//                final Provider sp = new Provider(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI, null , null, ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID);

            final ContentProviderHandlerEx.XDExport.Provider data = new ContentProviderHandlerEx.XDExport.Provider(ContactsContract.Data.CONTENT_URI
                    , null
                    , null
                    , ContactsContract.Data.CONTACT_ID);

            contacts.getLinked().add(data);

            new ContentProviderHandlerEx.XDExport(context).query(contacts, dataCallback, progressCallback, dataReadyCallback);
        }
}
