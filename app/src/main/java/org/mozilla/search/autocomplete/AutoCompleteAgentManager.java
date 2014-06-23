package org.mozilla.search.autocomplete;

import android.app.Activity;
import android.database.Cursor;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;

class AutoCompleteAgentManager {

    private final Handler mMainUiHandler;
    private final Handler mHandler;
    private final AutoCompleteWordListAgent mAutoCompleteWordListAgent;

    public AutoCompleteAgentManager(Activity activity, Handler mainUiHandler) {
        HandlerThread thread = new HandlerThread("org.mozilla.search.autocomplete.SuggestionAgent");
        // TODO: Where to kill this thread?
        thread.start();
        Log.i("AUTOCOMPLETE", "Starting thread");
        mMainUiHandler = mainUiHandler;
        mHandler = new SuggestionMessageHandler(thread.getLooper());
        mAutoCompleteWordListAgent = new AutoCompleteWordListAgent(activity);
    }

    /**
     * Process the next incoming query.
     */
    public void search(String queryString) {

        // TODO check if there's a pending search.. not sure how to handle that.
        mHandler.sendMessage(mHandler.obtainMessage(0, queryString));
    }

    /**
     * This background thread runs the queries; the results get sent back through mMainUiHandler
     * <p/>
     * TODO: Refactor this wordlist search and add other search providers (eg: Yahoo)
     */
    private class SuggestionMessageHandler extends Handler {

        private SuggestionMessageHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (null == msg.obj)
                return;

            Cursor cursor = mAutoCompleteWordListAgent.getWordMatches(((String) msg.obj).toLowerCase()
            );
            ArrayList<AutoCompleteModel> res = new ArrayList<AutoCompleteModel>();

            if (null == cursor)
                return;

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                res.add(new AutoCompleteModel(
                        cursor.getString(cursor.getColumnIndex(AutoCompleteWordListAgent.COL_WORD))));
            }


            mMainUiHandler.sendMessage(Message.obtain(mMainUiHandler, 0, res));
        }

    }

}