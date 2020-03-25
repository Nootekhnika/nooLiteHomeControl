package com.noolitef.rx;

import java.util.HashMap;

class LogListViewItem extends HashMap<String, String> {

    protected static final String DATA1 = "data1";
    protected static final String DATA2 = "data2";
    protected static final String TIME = "time";

    public LogListViewItem(String data1, String data2, String time) {
        super();
        super.put(DATA1, data1);
        super.put(DATA2, data2);
        super.put(TIME, time);
    }
}
