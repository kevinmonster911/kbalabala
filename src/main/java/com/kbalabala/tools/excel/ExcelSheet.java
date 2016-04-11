package com.kbalabala.tools.excel;

import java.util.Collection;

/**
 * sheet的Vo The <code>ExcelSheet</code>
 */
public class ExcelSheet<T> {
    private String sheetName;
    private String[] headers;
    private Collection<T> dataset;

    /**
     * @return the sheetName
     */
    public String getSheetName() {
        return sheetName;
    }

    /**
     * Excel页签名称
     *
     * @param sheetName the sheetName to set
     */
    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    /**
     * Excel表头
     *
     * @return the headers
     */
    public String[] getHeaders() {
        return headers;
    }

    /**
     * @param headers the headers to set
     */
    public void setHeaders(String[] headers) {
        this.headers = headers;
    }

    /**
     * Excel数据集合
     *
     * @return the dataset
     */
    public Collection<T> getDataset() {
        return dataset;
    }

    /**
     * @param dataset the dataset to set
     */
    public void setDataset(Collection<T> dataset) {
        this.dataset = dataset;
    }

}
