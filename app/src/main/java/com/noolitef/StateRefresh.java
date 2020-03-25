package com.noolitef;

import java.util.Calendar;
import java.util.TimerTask;

class StateRefresh extends TimerTask {

    NooLiteF nooLiteF;
    long id;

    public StateRefresh(NooLiteF nooLiteF) {
        this.nooLiteF = nooLiteF;
        id = Calendar.getInstance().getTimeInMillis() % 10000;
    }

    @Override
    public void run() {
        //nooLiteF.refreshState();
        //Log.i("nooLiteF", "StateRefreshTimer id" + id);
    }
}
