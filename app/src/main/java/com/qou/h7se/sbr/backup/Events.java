package com.qou.h7se.sbr.backup;

import android.content.Context;
import android.provider.CalendarContract;

import com.qou.h7se.sbr.ContentProviderHandlerEx;
import com.qou.h7se.sbr.Uris;

/**
 * Created by k0de9x on 10/20/2015.
 */
public class Events {
    public static void run(Context context, final ContentProviderHandlerEx.XDExport.OnXDDataCallback dataCallback, final ContentProviderHandlerEx.XDExport.OnXDProgressCallback progressCallback, final ContentProviderHandlerEx.XDExport.OnXDDataReadyCallback dataReadyCallback) {
                final ContentProviderHandlerEx.XDExport.Provider calendars = new ContentProviderHandlerEx.XDExport.Provider(CalendarContract.Calendars.CONTENT_URI, null, null, null);

                final ContentProviderHandlerEx.XDExport.Provider events = new ContentProviderHandlerEx.XDExport.Provider(Uris.CALENDARS_EVENTS
                        , null
                        , null
                        , CalendarContract.Events.CALENDAR_ID);

                final ContentProviderHandlerEx.XDExport.Provider reminders = new ContentProviderHandlerEx.XDExport.Provider(Uris.CALENDARS_REMINDERS
                        , null
                        , null
                        , CalendarContract.Reminders.EVENT_ID);

                events.getLinked().add(reminders);
                calendars.getLinked().add(events);
                new ContentProviderHandlerEx.XDExport(context).query(calendars,dataCallback, progressCallback, dataReadyCallback);
    }
}
