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

    public Dataset(Long id,
                   String created,
                   String modified,
                   String operatorName,
                   List<String> nocs,
                   String name,
                   String description,
                   String comment,
                   String status,
                   String url) {
        this.id = id;
        this.created = created;
        this.modified = modified;
        this.operatorName = operatorName;
        this.nocs = List.copyOf(nocs);
        this.name = name;
        this.description = description;
        this.comment = comment;
        this.status = status;
        this.url = url;
    }

}
