/*
 * Copyright (c) 2020. All rights reserved.
 *
 * Author: Osman Karagöz
 * Licensed under the GNU General Public License, version 3.
 * See the file http://www.gnu.org/copyleft/gpl.txt
 */

package net.osmank3.labebe.db;

import androidx.annotation.Nullable;

import java.util.Date;

public class AppLog {
    private String appName;
    private Date start, end;

    public AppLog() {
    }

    public AppLog(String appName, Date start, @Nullable Date end) {
        this.appName = appName;
        this.start = start;
        this.end = end;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }
}
