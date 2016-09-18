package com.qou.h7se.sbr.backup;

import android.content.Context;

import com.qou.h7se.sbr.ContentProviderHandlerEx;
import com.qou.h7se.sbr.Uris;

/**
 * Created by k0de9x on 10/17/2015.
 */
public class Mms {
    public static void run(Context context, final ContentProviderHandlerEx.XDExport.OnXDDataCallback dataCallback, final ContentProviderHandlerEx.XDExport.OnXDProgressCallback progressCallback, final ContentProviderHandlerEx.XDExport.OnXDDataReadyCallback dataReadyCallback) {
        ContentProviderHandlerEx.XDExport.Helpers.ExportGeneric(context, Uris.MMS, dataCallback, progressCallback, dataReadyCallback);
    }
}
