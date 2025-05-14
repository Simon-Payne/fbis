package com.flatshire.fbis.components;

import com.flatshire.fbis.csv.BusRouteBean;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class BusRouteReader {

    private static final Pattern REGEX = Pattern.compile("^route([0-9]+).csv$");

    public List<BusRouteBean> readBusRouteFromCsvFile(Path path) throws IOException, URISyntaxException {
        Path normalisedPath = normaliseFilePath(path);
        try (Reader reader = Files.newBufferedReader(normalisedPath)) {
            CsvToBean<BusRouteBean> cb = new CsvToBeanBuilder<BusRouteBean>(reader)
                    .withType(BusRouteBean.class)
                    .build();
            return cb.parse();
        }
    }

    private static Path normaliseFilePath(Path path) throws FileNotFoundException, URISyntaxException {
        Objects.requireNonNull(path, "Path was null");
        String p = path.toString();
        File file;
        if(p.startsWith("classpath:")) {
            String f = p.substring(p.indexOf(":") + 1);
            URL resource = BusRouteReader.class.getClassLoader().getResource(f);
            Objects.requireNonNull(resource, "Routes file not found at path " + resource);
            file = new File(resource.getPath());
        } else {
            file = new File(p);
        }
        if(!file.exists() || !file.canRead()) {
            throw new FileNotFoundException("Non-existent file " + file);
        }
        String fileName = p.substring(p.lastIndexOf(File.separator) + 1);
        Matcher matcher = REGEX.matcher(fileName);
        if(!matcher.matches() || matcher.groupCount() != 1) {
            throw new IllegalArgumentException("""
                Invalid path '%s': did not start with 'route', \
                continue with an integer and end with '.csv'"""
                    .formatted(fileName));
        }
        return Path.of(file.getPath());
    }

}
