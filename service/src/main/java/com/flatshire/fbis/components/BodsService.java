package com.flatshire.fbis.components;

import org.apache.commons.lang3.tuple.Pair;

public interface BodsService {

    Pair<String, String> readPositionFromDataFeed(String lineRef);

}
