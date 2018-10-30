/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   Oct 25, 2018 (dewi): created
 */
package org.knime.ext.textprocessing.nodes.view.bratdocumentviewer;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.js.core.JSONViewContent;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Representation class for Brat Document Viewer node. It contains the values needed for the visualization. The values
 * stay unchanged throughout the view.
 *
 * @author Andisa Dewi, KNIME AG, Berlin, Germany
 */
@JsonAutoDetect
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class BratDocumentViewerRepresentation extends JSONViewContent {

    private String m_docText;

    private String m_docTitle;

    private List<String> m_termIds;

    private List<String> m_tags;

    private List<String> m_terms;

    private List<String> m_startIndexes;

    private List<String> m_stopIndexes;

    /**
     * @param text the documentText to set
     */
    public void setDocumentText(final String text) {
        m_docText = text;
    }

    /**
     * @return the documentText
     */
    public String getDocumentText() {
        return m_docText;
    }

    /**
     * @return the documentTitle
     */
    public String getDocumentTitle() {
        return m_docTitle;
    }

    /**
     * @param documentTitle the documentTitle to set
     */
    public void setDocumentTitle(final String documentTitle) {
        this.m_docTitle = documentTitle;
    }

    /**
     * @return the termIds
     */
    public List<String> getTermIds() {
        return m_termIds;
    }

    /**
     * @param termIds the termIds to set
     */
    public void setTermIds(final List<String> termIds) {
        this.m_termIds = termIds;
    }

    /**
     * @return the tags
     */
    public List<String> getTags() {
        return m_tags;
    }

    /**
     * @param tags the tags to set
     */
    public void setTags(final List<String> tags) {
        this.m_tags = tags;
    }

    /**
     * @return the terms
     */
    public List<String> getTerms() {
        return m_terms;
    }

    /**
     * @param terms the terms to set
     */
    public void setTerms(final List<String> terms) {
        this.m_terms = terms;
    }

    /**
     * @return the startIndexes
     */
    public List<String> getStartIndexes() {
        return m_startIndexes;
    }

    /**
     * @param startIndexes the startIndexes to set
     */
    public void setStartIndexes(final List<String> startIndexes) {
        this.m_startIndexes = startIndexes;
    }

    /**
     * @return the stopIndexes
     */
    public List<String> getStopIndexes() {
        return m_stopIndexes;
    }

    /**
     * @param stopIndexes the stopIndexes to set
     */
    public void setStopIndexes(final List<String> stopIndexes) {
        this.m_stopIndexes = stopIndexes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveToNodeSettings(final NodeSettingsWO settings) {
        if (m_docText != null && m_terms != null) {
            settings.addString(BratDocumentViewerConfigKeys.DOC_TEXT, m_docText);
            settings.addString(BratDocumentViewerConfigKeys.DOC_TITLE, m_docTitle);
            settings.addStringArray(BratDocumentViewerConfigKeys.DOC_TERM_IDS, m_termIds.toArray(new String[]{}));
            settings.addStringArray(BratDocumentViewerConfigKeys.DOC_TERMS, m_terms.toArray(new String[]{}));
            settings.addStringArray(BratDocumentViewerConfigKeys.DOC_TAGS, m_tags.toArray(new String[]{}));
            settings.addStringArray(BratDocumentViewerConfigKeys.DOC_START_IDX, m_startIndexes.toArray(new String[]{}));
            settings.addStringArray(BratDocumentViewerConfigKeys.DOC_STOP_IDX, m_stopIndexes.toArray(new String[]{}));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadFromNodeSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        setDocumentText(settings.getString(BratDocumentViewerConfigKeys.DOC_TEXT));
        setDocumentTitle(settings.getString(BratDocumentViewerConfigKeys.DOC_TITLE));
        setTermIds(Arrays.asList(settings.getStringArray(BratDocumentViewerConfigKeys.DOC_TERM_IDS)));
        setTerms(Arrays.asList(settings.getStringArray(BratDocumentViewerConfigKeys.DOC_TERMS)));
        setTags(Arrays.asList(settings.getStringArray(BratDocumentViewerConfigKeys.DOC_TAGS)));
        setStartIndexes(Arrays.asList(settings.getStringArray(BratDocumentViewerConfigKeys.DOC_START_IDX)));
        setStopIndexes(Arrays.asList(settings.getStringArray(BratDocumentViewerConfigKeys.DOC_STOP_IDX)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }

        BratDocumentViewerRepresentation other = (BratDocumentViewerRepresentation)obj;
        return new EqualsBuilder().append(m_docText, other.m_docText).append(m_docTitle, other.m_docTitle)
            .append(m_termIds, other.m_termIds).append(m_terms, other.m_terms).append(m_tags, other.m_tags)
            .append(m_startIndexes, other.m_startIndexes).append(m_stopIndexes, other.m_stopIndexes).isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(m_docText).append(m_docTitle).append(m_termIds).append(m_terms)
            .append(m_tags).append(m_startIndexes).append(m_stopIndexes).toHashCode();
    }

}
