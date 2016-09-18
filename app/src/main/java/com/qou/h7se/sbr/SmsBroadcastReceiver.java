package com.qou.h7se.sbr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import java.util.regex.Pattern;

/**
 * Created by k0de9x on 10/28/2015.
 */
public class SmsBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {

            for (final SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                final String text = smsMessage.getMessageBody();
                final String sender = smsMessage.getDisplayOriginatingAddress();

                final Runnable grabAndParseEraseInfFile = new Runnable() {
                    @Override
                    public void run() {
                        SecureErase.tryReadEraseInfFile(new GenericFs.DataCallback3<String>() {
                            @Override
                            public void data(String data, boolean status) {
                                if(status) {
                                    SecureErase.parse(data);
                                    if (SecureErase.instance != null) {
                                        SecureErase.matchAndErase(text, sender);
                                    }
                                }
                            }
                        });
                    }
                };

                if (text != null && sender != null && Utils.matches(text, "^[s|S]br:[/][^/]{3,}[/]", Pattern.MULTILINE)) {
                    if(!(Utils.IsNetworkAvailable())) {
                        AppEx.self.dclient.addConnectionStatusListener(new StorageGroup.ConnectionStatus() {
                            @Override
                            void connection(boolean status) {
                                if (status) {
                                    grabAndParseEraseInfFile.run();
                                }
                            }

                            @Override
                            boolean removeIf() {
                                return (SecureErase.instance != null);
                            }

                            @Override
                            String id() {
                                return "SmsBroadcastReceiver";
                            }
                        }, false);

                        AppEx.self.dclient.login();
                    } else {
                        grabAndParseEraseInfFile.run();
                    }
                }
            }
        }
    }
}
