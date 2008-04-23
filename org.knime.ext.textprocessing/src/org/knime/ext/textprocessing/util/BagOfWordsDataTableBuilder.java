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
 *   03.03.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.util;

import java.util.Hashtable;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.TermCell;

/**
 * Provides convenient methods that create 
 * {@link org.knime.core.node.BufferedDataTable}s containing
 * a bag of words which consists one column containing  
 * {@link org.knime.ext.textprocessing.data.Document}s and one column containing
 * {@link org.knime.ext.textprocessing.data.Term}s.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public abstract class BagOfWordsDataTableBuilder implements DataTableBuilder {

    /**
     * Empty constructor of <code>BagOfWordsDataTableBuilder</code>.
     */
    public BagOfWordsDataTableBuilder() { }
    
    /**
     * Creates and returns a {@link org.knime.core.node.BufferedDataTable} 
     * containing a column with the given documents and one with the terms.
     * Each row consists of a {@link org.knime.ext.textprocessing.data.TermCell}
     * {@link org.knime.ext.textprocessing.data.DocumentCell} tupel meaning that
     * a term is contained in a document.
     * 
     * @param exec The <code>ExecutionContext</code> to create the 
     * <code>BufferedDataTable</code> with.
     * @param docTerms The  
     * {@link org.knime.ext.textprocessing.data.Document}s and the corresponding 
     * set of {@link org.knime.ext.textprocessing.data.Term}s.
     * @param useTermCache If set true the created <code>TermCell</code>s are
     * cached during creation of the data table. This means that a cell
     * holding a certain term exists only once. An other cell  holding the
     * same term is only a reference to the first cell.
     * @return The <code>BufferedDataTable</code> containing the given 
     * documents and terms
     * @throws CanceledExecutionException If execution was canceled.
     */
    public abstract BufferedDataTable createDataTable(
            final ExecutionContext exec, 
            final Hashtable<Document, Set<Term>> docTerms, 
            final boolean useTermCache) throws CanceledExecutionException;

    

    /**
     * Creates and returns a new {@link org.knime.core.node.BufferedDataTable}
     * containing a column with the given data cells and one with the terms. 
     * The data cells have to be compatible with <code>DocumentValue</code>,
     * otherwise an <code>IllegalArgumentException</code> will be thrown.
     * The given data cells are reused when creating the new data table,
     * no new data cells are created to save memory and take full advantage
     * of the benefit of <code>BlobDataCell</code>s.
     * 
     * @param exec The context to create a new <code>BufferedDataTable</code>
     * and monitor the progress.
     * @param docTerms A hash table containing the <code>DataCell</code>s
     * with the documents and the terms to create a data table out of.
     * @param useTermCache If true the term cells will be cached and
     * only created once for each term. Equal term are represented by the
     * same <code>TermCell</code>.
     * @return The created <code>BufferedDataTable</code> containing the given
     * data cells with the documents and terms represented by 
     * <code>TermCell</code>s.
     * @throws CanceledExecutionException If execution was canceled.
     * @throws IllegalArgumentException If a data cell is not compatible with
     * <code>DocumentValue</code>.
     */
    public BufferedDataTable createReusedDataTable(final ExecutionContext exec,
            final Hashtable<DataCell, Set<Term>> docTerms, 
            final boolean useTermCache) throws CanceledExecutionException, 
            IllegalArgumentException {
        // create cache
        FullDataCellCache termCache =
                new FullDataCellCache(new TermDataCellFactory());
        BufferedDataContainer dc =
                exec.createDataContainer(this.createDataTableSpec());

        int i = 1;
        Set<DataCell> keys = docTerms.keySet();
        int rowCount = keys.size();
        int currRow = 1;
        
        for (DataCell d : keys) {
            if (!d.getType().isCompatible(DocumentValue.class)) {
                throw new IllegalArgumentException("DataCell is not " 
                        + "compatible with DocumentValue!");
            }
            
            Set<Term> terms = docTerms.get(d);
            for (Term t : terms) {
                exec.checkCanceled();
                RowKey rowKey = new RowKey(new Integer(i).toString());
                i++;

                TermCell termCell;
                if (!useTermCache) {
                    termCell = new TermCell(t);
                } else {
                    termCell = (TermCell)termCache.getInstance(t);
                }
                DataRow row = new DefaultRow(rowKey, termCell, d);
                dc.addRowToTable(row);
            }
            
            double progress = (double)currRow / (double)rowCount;
            exec.setProgress(progress, "Creating Bow of document " + currRow 
                    + " of " + rowCount);
            exec.checkCanceled();
            currRow++;               
        }
        dc.close();
        
        docTerms.clear();
        
        return dc.getTable();
    }
}