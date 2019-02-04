package com.kaicomsol.kpos.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Paging {

    @SerializedName("pageNumber")
    @Expose
    private int pageNumber;
    @SerializedName("pageSize")
    @Expose
    private int pageSize;
    @SerializedName("totalNumberOfRecords")
    @Expose
    private int totalNumberOfRecords;
    @SerializedName("totalNumberOfPages")
    @Expose
    private int totalNumberOfPages;
    @SerializedName("hasNextPage")
    @Expose
    private boolean hasNextPage;


    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalNumberOfRecords() {
        return totalNumberOfRecords;
    }

    public int getTotalNumberOfPages() {
        return totalNumberOfPages;
    }

    public boolean isHasNextPage() {
        return hasNextPage;
    }
}
