/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2008
 * University of Konstanz, Germany
 * Chair for Bioinformatics and Information Mining (Prof. M. Berthold)
 * and KNIME GmbH, Konstanz, Germany
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
 * ---------------------------------------------------------------------
 * 
 * History
 *   22.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.frequencies.filter;


/**
 * A simple factory which creates concrete implementations of 
 * <code>FrequencyFilter</code>s conveniently.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class FilterFactory {

    private FilterFactory() { }
    
    /**
     * Creates and returns an instance of a concrete 
     * <code>FrequencyFilter</code> implementation. The given 
     * <code>filterOption</code> specifies which kind of 
     * <code>FrequencyFilter</code> is created. The additional parameter
     * specify the index of the column containing the terms, the column to apply
     * the filtering to, the number of rows to keep and the min and max value.
     * 
     * @param filterOption Specifies which filter is created
     * @param termColIndex The index of the column containing terms.
     * @param filterColIndex The index of the column to apply the filter method 
     * to.
     * @param number The number of rows to keep (the rest of the rows is 
     * filtered).
     * @param minVal The min value of the filter column's number to be not 
     * filtered.
     * @param maxVal The max value of the filter column's number to be not 
     * filtered.
     * @return A new instance of <code>FrequencyFilter</code>.
     */
    public static final FrequencyFilter createFilter(final String filterOption, 
            final int termColIndex, final int filterColIndex, final int number, 
            final double minVal, final double maxVal) {
        if (filterOption.equals(FilterNodeModel.SELECTION_NUMBER)) {
            return new KTermsFilter(termColIndex, filterColIndex, number);
        } else if (filterOption.equals(FilterNodeModel.SELECTION_THRESHOLD)) {
            return new ThresholdFilter(termColIndex, filterColIndex, minVal, 
                    maxVal);
        }
        return null;
    }
    
}
