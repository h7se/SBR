package com.qou.h7se.sbr;

import java.util.List;

public interface DataFilterCompleteCallback<E> {
    void run(List<Integer> positions, List<E> result);
}
