/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
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
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
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
 * Created on 04.05.2013 by Kilian Thiel
 */
package org.knime.ext.textprocessing.nodes.tagging.dict.wildcard;

import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.TagFactory;
import org.knime.ext.textprocessing.nodes.tagging.dict.AbstractDictionaryTaggerModel;
import org.knime.ext.textprocessing.nodes.tagging.dict.inport.DictionaryTaggerNodeDialog;

/**
 *
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 * @since 2.8
 */
public class WildcardTaggerNodeDialog extends DefaultNodeSettingsPane {

    /**
     * @return Creates and returns a
     * {@link org.knime.core.node.defaultnodesettings.SettingsModelString}
     * containing the matching strategy to use (single or multi term).
     */
    public static final SettingsModelString createMatchingModel() {
        return new SettingsModelString(WildcardTaggerConfigKeys.CFGKEY_SINGLETERM,
                                       WildcardTaggerNodeModel.DEF_MATCHING_MODEL);
    }

    /**
     * Creates a new instance of <code>WildcardTaggerNodeDialog</code>.
     */
    @SuppressWarnings("unchecked")
    public WildcardTaggerNodeDialog() {
        addDialogComponent(new DialogComponentColumnNameSelection(
                DictionaryTaggerNodeDialog.createColumnModel(), "Regular expression column",
                AbstractDictionaryTaggerModel.DICT_TABLE_INDEX,
                StringValue.class));

        setHorizontalPlacement(true);

        addDialogComponent(new DialogComponentBoolean(
                        DictionaryTaggerNodeDialog.createSetUnmodifiableModel(),
                        "Set named entities unmodifiable"));

        addDialogComponent(new DialogComponentBoolean(
                DictionaryTaggerNodeDialog.createCaseSensitiveModel(),
                "Case sensitive"));

        setHorizontalPlacement(false);

        addDialogComponent(new DialogComponentButtonGroup(createMatchingModel(), false, "Matching",
                new String[]{WildcardTaggerNodeModel.SINGLE_TERM_MATCHINGMODEL,
                             WildcardTaggerNodeModel.MULTI_TERM_MATCHINGMODEL}));

        // tag type model
        m_tagtypemodel = DictionaryTaggerNodeDialog.createTagTypeModel();
        m_tagtypemodel.addChangeListener(new InternalChangeListener());

        // tag list
        String selectedTagType = m_tagtypemodel.getStringValue();
        List<String> tags = TagFactory.getInstance()
                .getTagSetByType(selectedTagType).asStringList();
        m_tagSelection = new DialogComponentStringSelection(
                    DictionaryTaggerNodeDialog.createTagModel(), "Tag value", tags);

        this.setHorizontalPlacement(true);
        addDialogComponent(new DialogComponentStringSelection(
                m_tagtypemodel, "Tag type",
                TagFactory.getInstance().getTagTypes()));

        addDialogComponent(m_tagSelection);
    }

    private DialogComponentStringSelection m_tagSelection;

    private SettingsModelString m_tagtypemodel;

    /**
     *
     * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
     */
    class InternalChangeListener implements ChangeListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void stateChanged(final ChangeEvent e) {
            String selectedTagType = m_tagtypemodel.getStringValue();
            List<String> tags = TagFactory.getInstance()
                    .getTagSetByType(selectedTagType).asStringList();
            m_tagSelection.replaceListItems(tags, "");
        }
    }
}