package com.zxbear.ibvapi;

import androidx.annotation.UiThread;

public interface UnIBind {
    @UiThread
    void unbind();

    UnIBind EMPTY = () -> { };
}
