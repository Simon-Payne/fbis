package com.flatshire.fbis.components;

import org.apache.commons.lang3.tuple.Pair;

public interface BodsServiceHelper {

    Pair<String, String> fetchData(String lineRef);

}
