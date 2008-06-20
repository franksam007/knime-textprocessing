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

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.RowIterator;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentBuilder;
import org.knime.ext.textprocessing.data.DocumentCell;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.Paragraph;
import org.knime.ext.textprocessing.data.Section;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.TermValue;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

/**
 * Provides methods to purge terms out of documents based on the specified 
 * bag of words data table. The data table contains terms and documents. All 
 * terms of the documents which are not contained in the bag of words 
 * (the term column of the data table) are purged from the documents, except 
 * those which are unmodifiable. 
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class TermPurger {

    private Set<Term> m_terms;
    
    private DataTable m_inData;
    
    private int m_termColIndex;
    
    private int m_docColIndex;
    
    private ExecutionContext m_exec;
    
    /**
     * Creates a new instance of <code>TermPurger</code> with given bag of word 
     * input data contained in the data table and an 
     * <code>ExecutionContext</code> to create a <code>BufferedDataTable</code>
     * and monitor the progress.
     * 
     * @param inData The input data table containing a bag of words.
     * @param exec A execution context to monitor the progress.
     * @throws InvalidSettingsException If the given data table contains no
     * column with documents or terms.
     */
    public TermPurger(final DataTable inData, final ExecutionContext exec) 
    throws InvalidSettingsException {
        m_inData = inData;
        
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(
                inData.getDataTableSpec());
        verifier.verifyDocumentCell(true);
        verifier.verifyTermCell(true);
        m_termColIndex = verifier.getTermCellIndex();
        m_docColIndex = verifier.getDocumentCellIndex();
        m_exec = exec;
        
        cacheTerms();
    }
    
    private void cacheTerms() {
        m_terms = new HashSet<Term>();
        RowIterator it = m_inData.iterator();
        while (it.hasNext()) {
            DataRow row = it.next();
            Term t = ((TermValue)row.getCell(m_termColIndex)).getTermValue();
            if (!m_terms.contains(t)) {
                m_terms.add(t);
            }
        }
    }
    
    /**
     * Deletes all terms out of the documents which are not contained in the 
     * specified bag of words, creates a new data table with the modified 
     * documents and returns it.
     * 
     * @return A data table containing the modified documents.
     */
    public BufferedDataTable getPurgedDataTable() {
        Hashtable<Document, Document> preprocessedDoc = 
            new Hashtable<Document, Document>();
        
        RowIterator it = m_inData.iterator();
        while (it.hasNext()) {
            DataRow row = it.next();
            Document origDoc = ((DocumentValue)row.getCell(m_docColIndex))
                            .getDocument();
            
            // purge only if not purged yet
            if (!preprocessedDoc.containsKey(origDoc)) {
                Document purgedDocument = purgeDocument(origDoc);
                preprocessedDoc.put(origDoc, purgedDocument);
            }
        }
        
        return createNewDataTable(preprocessedDoc);
    }
    
    private BufferedDataTable createNewDataTable(
            final Hashtable<Document, Document> preprocessedDoc) {
        
        BufferedDataContainer dc = m_exec.createDataContainer(
                m_inData.getDataTableSpec());
        
        
        RowIterator it = m_inData.iterator();
        while (it.hasNext()) {
            DataRow row = it.next();
            Document origDoc = ((DocumentValue)row.getCell(m_docColIndex))
                            .getDocument();
            
            // add all cells of old data table except the document cell,
            // which has to be re-created with the purged document.
            DataCell[] cells = new DataCell[row.getNumCells()];
            for (int i = 0; i < row.getNumCells(); i++) {
                if (i == m_docColIndex) {
                    DocumentCell docCell = new DocumentCell(
                            preprocessedDoc.get(origDoc));
                    cells[i] = docCell;
                } else {
                    cells[i] = row.getCell(i);
                }
            }
            
            DataRow newRow = new DefaultRow(row.getKey(), cells);
            dc.addRowToTable(newRow);
        }
        
        dc.close();
        return dc.getTable();   
    }
    
    private Document purgeDocument(final Document doc) {
        Document newDoc;
        
        DocumentBuilder builder = new DocumentBuilder(doc);
        List<Section> sections = doc.getSections();
        for (Section s : sections) {
            List<Paragraph> paragraphs = s.getParagraphs();
            for (Paragraph p : paragraphs) {
                List<Sentence> sentences = p.getSentences();
                for (Sentence sen : sentences) {
                    List<Term> senTerms = sen.getTerms();
                    for (Term t : senTerms) {
                        // if not unmodifiable and not contains in terms set,
                        // set t null.
                        if (!t.isUnmodifiable()) {
                            if (!m_terms.contains(t)) {
                                t = null;
                            }
                        }
                        if (t != null && t.getText().length() > 0) {
                            builder.addTerm(t);
                        }
                    }
                    builder.createNewSentence();
                }
                builder.createNewParagraph();
            }
            builder.createNewSection(s.getAnnotation());
        }
        newDoc = builder.createDocument();
        
        return newDoc;
    }
}
