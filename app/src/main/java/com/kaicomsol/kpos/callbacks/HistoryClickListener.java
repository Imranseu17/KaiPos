package com.kaicomsol.kpos.callbacks;

import com.kaicomsol.kpos.models.Content;

public interface HistoryClickListener {
    public void onHistoryClick(Content content);
    public void retryPageLoad();
}
