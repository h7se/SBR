package com.qou.h7se.sbr;

/**
 * Created by k0de9x on 9/26/2015.
 */

public interface DataFilterCallback<E> {
    boolean include(E item);
}

