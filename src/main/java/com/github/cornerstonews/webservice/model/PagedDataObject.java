/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.cornerstonews.webservice.model;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Min;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "pagedDataObject")
public abstract class PagedDataObject<T> {

    @XmlTransient
    private static final Integer DEFAULT_PAGE_SIZE = 20;

    @XmlTransient
    protected Integer pagesToDisplay = 5;

    /*
     * Start Index of the search query result based on pagination
     */
    @XmlElement(name = "startIndex")
    @Min(value = 0)
    private Long startIndex;

    /*
     * Last index of the search query result based on pagination
     */
    @XmlElement(name = "endIndex")
    @Min(value = 0)
    private Long endIndex;

    /*
     * Total results from the search query
     */
    @XmlElement(name = "totalItems")
    @Min(value = 0)
    private Long totalItems;

    /*
     * Number of rows/elements/objects in a page size
     */
    @XmlElement(name = "pageSize")
    @Min(value = 0)
    private Integer pageSize;

    /*
     * Returns the current (selected) page number.
     */
    @XmlElement(name = "currentPage")
    @Min(value = 0)
    private Integer currentPage;

    /*
     * First page based on pagination frame size
     */
    @XmlElement(name = "startPage")
    @Min(value = 0)
    private Integer startPage;

    /*
     * Last page based on pagination frame size
     */
    @XmlElement(name = "endPage")
    @Min(value = 0)
    private Integer endPage;

    /*
     * Total pages based on the search query result
     */
    @XmlElement(name = "totalPages")
    @Min(value = 0)
    private Integer totalPages;

    /*
     * List of pages for pagination based on frame size starting with startPage and endPage
     */
    @XmlElement(name = "pages")
    private List<Long> pages;

    /*
     * True/False based on if there is next page available to traverse
     */
    @XmlElement(name = "hasNextPage")
    private Boolean hasNextPage;

    /*
     * True/False based on if there is previous page available to traverse
     */
    @XmlElement(name = "hasPreviousPage")
    private Boolean hasPreviousPage;

    protected PagedDataObject(Long totalItems) {
        this(totalItems, null, null);
    }

    protected PagedDataObject(Long totalItems, Integer pageSize) {
        this(totalItems, pageSize, null);
    }

    protected PagedDataObject(Long totalItems, Integer pageSize, Integer currentPage) {
        this.totalItems = totalItems;
        this.pageSize = pageSize;
        this.currentPage = currentPage;

        this.generatePagedDataObject();
    }

    public Long getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(Long startIndex) {
        this.startIndex = startIndex;
    }

    public Long getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(Long endIndex) {
        this.endIndex = endIndex;
    }

    public Long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Long totalItems) {
        this.totalItems = totalItems;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getStartPage() {
        return startPage;
    }

    public void setStartPage(Integer startPage) {
        this.startPage = startPage;
    }

    public Integer getEndPage() {
        return endPage;
    }

    public void setEndPage(Integer endPage) {
        this.endPage = endPage;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public List<Long> getPages() {
        return pages;
    }

    public void setPages(List<Long> pages) {
        this.pages = pages;
    }

    public Boolean getHasNextPage() {
        return hasNextPage;
    }

    public void setHasNextPage(Boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }

    public Boolean getHasPreviousPage() {
        return hasPreviousPage;
    }

    public void setHasPreviousPage(Boolean hasPreviousPage) {
        this.hasPreviousPage = hasPreviousPage;
    }

    private PagedDataObject<T> generatePagedDataObject() {

        if (this.totalItems == null) {
            return this;
        }

        this.pageSize = (pageSize != null) ? pageSize : DEFAULT_PAGE_SIZE;
        this.currentPage = (currentPage != null) ? currentPage : 1;

        // calculate total pages
        this.totalPages = (int) Math.ceil(this.totalItems / (double) this.pageSize);
        if(this.totalPages == 0 && this.totalItems > 0) {
            this.totalPages = 1;
        }

        if (this.totalPages <= pagesToDisplay) {
            // less than DEFAULT_PAGES_TO_DISPLAY total pages so show all
            this.startPage = 1;
            this.endPage = this.totalPages;
        } else {
            // more than DEFAULT_PAGES_TO_DISPLAY total pages so calculate start and end pages
            if (this.currentPage <= (Math.floor(pagesToDisplay / 2.0) + 1)) {
                this.startPage = 1;
                this.endPage = pagesToDisplay;
            } else if (this.currentPage + (Math.ceil(pagesToDisplay / 2.0) - 1) >= this.totalPages) {
                this.startPage = this.totalPages - (pagesToDisplay - 1);
                this.endPage = this.totalPages;
            } else {
                this.startPage = this.currentPage - (pagesToDisplay / 2);
                this.endPage = this.startPage + (pagesToDisplay - 1);
            }
        }

        // calculate start and end item indexes
        this.startIndex = (long)(this.currentPage - 1) * this.pageSize;
        this.endIndex = Math.max(0, Math.min(this.startIndex + this.pageSize - 1, this.totalItems - 1));

        // figure out if next and previous pages are available
        this.hasNextPage = this.currentPage <= (this.totalPages - 1);
        this.hasPreviousPage = this.currentPage > 1;

        // create an array of pages for the pager control
        pages = new ArrayList<>(totalPages);
        for (long i = this.startPage; i < this.endPage + 1; i++) {
            pages.add(i);
        }

        return this;
    }

}
