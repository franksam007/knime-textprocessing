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
 *   Oct 19, 2018 (dewi): created
 */
package org.knime.ext.textprocessing.nodes.view.bratdocumentviewer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowIterator;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortType;
import org.knime.core.node.web.ValidationError;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.util.ColumnSelectionVerifier;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;
import org.knime.js.core.node.AbstractWizardNodeModel;

/**
 * The {@link NodeModel} for the Brat Document Viewer. This node visualizes only the first document.
 *
 * @author Andisa Dewi, KNIME AG, Berlin, Germany
 */
public class BratDocumentViewerNodeModel
    extends AbstractWizardNodeModel<BratDocumentViewerRepresentation, BratDocumentViewerValue> {

    /**
     * the key name for the terms IDs
     */
    public final static String ID_KEY = "id";

    /**
     * the key name for the term tags
     */
    public final static String TAG_KEY = "tag";

    /**
     * the key name for the terms
     */
    public final static String TERM_KEY = "term";

    /**
     * the key name for the list of start indexes
     */
    public final static String FIRSTPOS_KEY = "firstPos";

    /**
     * the key name for the list of stop indexes
     */
    public final static String LASTPOS_KEY = "lastPos";

    private SettingsModelString m_docColModel = BratDocumentViewerNodeDialog.getDocColModel();

    private int m_docCellIndex = -1;

    private List<String> m_ids;

    private List<String> m_tags;

    private List<String> m_startIdx;

    private List<String> m_stopIdx;

    private List<String> m_terms;

    /**
     * The constructor of the Brat Document Viewer node. The node has one input port and no output port.
     *
     * @param viewName the name for the interactive view
     *
     */
    public BratDocumentViewerNodeModel(final String viewName) {
        super(new PortType[]{BufferedDataTable.TYPE}, new PortType[]{}, viewName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        checkDataTableSpec(inSpecs[0]);

        return new DataTableSpec[]{};
    }

    /**
     * Check the data table spec
     *
     * @param spec The data table spec to check
     * @throws InvalidSettingsException
     */
    private final void checkDataTableSpec(final DataTableSpec spec) throws InvalidSettingsException {
        // check input spec
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        verifier.verifyMinimumDocumentCells(1, true);

        ColumnSelectionVerifier.verifyColumn(m_docColModel, spec, DocumentValue.class, null)
            .ifPresent(msg -> setWarningMessage(msg));

        m_docCellIndex = spec.findColumnIndex(m_docColModel.getStringValue());
    }

    /**
     * Get the start and stop index of each term and wrap them in a JSON object
     *
     * @param doc the document to process
     * @return a list of JSON objects containing the terms and tags
     */
    public static List<JSONObject> processTagsAndTerms(final Document doc) {
        // TODO what happens if there are no tags?
        List<JSONObject> result = new ArrayList<JSONObject>();
        List<Term> terms = getTerms(doc);
        String text = doc.getDocumentBodyText();
        // mark the start position to search a term in the text
        int i = 0;
        // we need count for the entity naming, e.g T1, T2, etc
        int count = 1;
        int startIndex = -1;
        int stopIndex = -1;

        for (Term t : terms) {
            List<Tag> tags = t.getTags();
            // if tag is empty then ignore
            if (!tags.isEmpty()) {
                String term = t.getText();
                // get the start and stop index of the term in the text
                startIndex = text.indexOf(term, i);
                stopIndex = startIndex + term.length();
                i = stopIndex;
                // iterate through the tags, and create an entity for each one
                for (int j = 0; j < tags.size(); j++) {
                    JSONObject docDataJson = new JSONObject();
                    docDataJson.put(ID_KEY, "T" + count++);
                    String tag = tags.get(j).getTagValue();
                    // add the tag to the entity as well
                    docDataJson.put(TAG_KEY, StringUtils.capitalize(tag.toLowerCase()));
                    docDataJson.put(FIRSTPOS_KEY, Integer.toString(startIndex));
                    docDataJson.put(LASTPOS_KEY, Integer.toString(stopIndex));
                    docDataJson.put(TERM_KEY, term);
                    result.add(docDataJson);
                }
            }
        }
        return result;
    }

    /**
     * Get a list of terms from a document
     *
     * @param doc the document
     * @return the list of terms
     */
    private static List<Term> getTerms(final Document doc) {
        List<Term> termList = null;
        if (doc != null) {
            termList = new ArrayList<Term>();

            Iterator<Sentence> it = doc.sentenceIterator();
            while (it.hasNext()) {
                Sentence sen = it.next();
                termList.addAll(sen.getTerms());
            }
        }
        return termList;
    }

    private void setDocumentData(final Document doc) {
        List<JSONObject> terms = processTagsAndTerms(doc);
        performReset();

        for (JSONObject obj : terms) {
            m_ids.add(obj.getString(ID_KEY));
            String tag = obj.getString(TAG_KEY);
            m_tags.add(tag);
            m_terms.add(obj.getString(TERM_KEY));
            m_startIdx.add(obj.getString(FIRSTPOS_KEY));
            m_stopIdx.add(obj.getString(LASTPOS_KEY));
        }
    }

    private void setCollectionData(final Document doc) {
        // for now not needed..
    }

    /*public JSONObject getDocumentData(final Document doc) {
        List<JSONObject> terms = processTagsAndTerms(doc);
        m_tagSet = new LinkedHashSet<String>();

        // JSON root for the document data
        JSONObject docDataJson = new JSONObject();
        // to store the list of entities
        List<List<Object>> entities = new ArrayList<List<Object>>();
        docDataJson.put("text", doc.getText());

        for (JSONObject obj : terms) {
            // an entity is a list of objects
            List<Object> entity = new ArrayList<Object>();
            entity.add(obj.getString(ID_KEY));
            String tag = obj.getString(TAG_KEY);
            m_tagSet.add(tag);
            entity.add(tag);
            // pack the start and stop index in a list of a list
            List<List<String>> outerList = new ArrayList<List<String>>();
            List<String> innerList = new ArrayList<String>();
            innerList.add(obj.getString(FIRSTPOS_KEY));
            innerList.add(obj.getString(LASTPOS_KEY));

            outerList.add(innerList);
            entity.add(outerList);
            entities.add(entity);
        }

        docDataJson.put("entities", entities);
        return docDataJson;
    }

    public JSONObject getCollectionData(final Document doc) {
        // JSON root for the collection data
        JSONObject collDataJson = new JSONObject();
        List<JSONObject> objects = new ArrayList<JSONObject>();

        for (String tag : m_tagSet) {
            JSONObject obj = new JSONObject();
            obj.put("type", tag);
            obj.put("labels", new String[]{tag.toLowerCase()});

            // set random color
            Color randColor = new Color((int)(Math.random() * 0x1000000)).brighter();
            obj.put("bgColor",
                String.format("#%02x%02x%02x", randColor.getRed(), randColor.getGreen(), randColor.getBlue()));
            objects.add(obj);
        }
        collDataJson.put("entity_types", objects);

        return collDataJson;
    }*/

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_docColModel.saveSettingsTo(settings);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_docColModel.validateSettings(settings);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_docColModel.loadSettingsFrom(settings);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BratDocumentViewerRepresentation createEmptyViewRepresentation() {
        return new BratDocumentViewerRepresentation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BratDocumentViewerValue createEmptyViewValue() {
        return new BratDocumentViewerValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getJavascriptObjectID() {
        return "org.knime.ext.textprocessing.nodes.view.bratdocumentviewer";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHideInWizard() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHideInWizard(final boolean hide) {
        // nothing to do

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValidationError validateViewValue(final BratDocumentViewerValue viewContent) {
        synchronized (getLock()) {
            // validate value, nothing to do
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveCurrentValue(final NodeSettingsWO content) {
        // nothing to do

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] performExecute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
        synchronized (getLock()) {
            BufferedDataTable in = (BufferedDataTable)inObjects[0];
            checkDataTableSpec(in.getDataTableSpec());

            BratDocumentViewerRepresentation rep = getViewRepresentation();
            final RowIterator it = in.iterator();
            if (it.hasNext()) {
                // take only the first row
                DataCell docCell = it.next().getCell(m_docCellIndex);
                if (!docCell.isMissing()) {
                    Document doc = ((DocumentValue)docCell).getDocument();

                    setDocumentData(doc);
                    setCollectionData(doc);

                    rep.setDocumentText(doc.getDocumentBodyText());
                    rep.setDocumentTitle(doc.getTitle());
                    rep.setTermIds(m_ids);
                    rep.setTerms(m_terms);
                    rep.setTags(m_tags);
                    rep.setStartIndexes(m_startIdx);
                    rep.setStopIndexes(m_stopIdx);
                } else {
                    // If there is no document in the first row
                    // set warning message
                    setWarningMessage("There is no document in the first row.");
                }
            } else {
                setWarningMessage("Input table is empty.");
            }

        }
        return new BufferedDataTable[]{};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performReset() {
        m_ids = new ArrayList<String>();
        m_tags = new ArrayList<String>();
        m_terms = new ArrayList<String>();
        m_startIdx = new ArrayList<String>();
        m_stopIdx = new ArrayList<String>();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void useCurrentValueAsDefault() {
        // nothing to do

    }

}
