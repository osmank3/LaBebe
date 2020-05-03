/*
 * Copyright (c) 2020. All rights reserved.
 *
 * Author: Osman Karagöz
 * Licensed under the GNU General Public License, version 3.
 * See the file http://www.gnu.org/copyleft/gpl.txt
 */

package net.osmank3.labebe.ui.statistics;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class StatisticsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public StatisticsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is statitstics fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}