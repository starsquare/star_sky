package com.hikvision.www.framwork;

/**
 * Created by huangxing7 on 2017/11/21.
 */

public interface Unbinder
{
    void unbind();

    Unbinder EMPTY = new Unbinder() {
        @Override public void unbind() { }
    };
}
