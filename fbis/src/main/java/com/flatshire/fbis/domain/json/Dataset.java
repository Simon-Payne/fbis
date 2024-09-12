package com.flatshire.fbis.domain.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Dataset(Long id,
                      String created,
                      String modified,
                      String operatorName,
                      List<String> nocs,
                      String name,
                      String description,
                      String comment,
                      String status,
                      String url) {
}
