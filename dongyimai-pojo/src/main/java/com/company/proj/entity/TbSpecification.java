package com.company.proj.entity;

import java.io.Serializable;
import java.util.List;

public class TbSpecification implements Serializable {
    private Long id;

    private String specName;

    private List<TbSpecificationOption> specOptionList;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSpecName() {
        return specName;
    }

    public void setSpecName(String specName) {
        this.specName = specName == null ? null : specName.trim();
    }

    public List<TbSpecificationOption> getSpecOptionList() {
        return specOptionList;
    }

    public void setSpecOptionList(List<TbSpecificationOption> specOptionList) {
        this.specOptionList = specOptionList;
    }
}