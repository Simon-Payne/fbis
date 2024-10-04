package com.flatshire.fbis.components;

import com.flatshire.fbis.domain.BusInfo;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface BodsServiceHelper {

    /**
     * Fetch bus position data for a given line reference.
     * @param lineRef String
     * @return Pair&lt;String, String&gt; a pair of latitude/longitude coordinates representing the
     *      current position of the bus matching the line reference.
     */
    Pair<String, String> fetchData(String lineRef);

    /**
     * Fetch buses operated by the referenced operatopr.
     * @param operatorRef String the operator reference
     * @return List&lt;{@link BusInfo}&gt; the buses operated by the operator
     */
    List<BusInfo> fetchBusInfo(String operatorRef);
}
