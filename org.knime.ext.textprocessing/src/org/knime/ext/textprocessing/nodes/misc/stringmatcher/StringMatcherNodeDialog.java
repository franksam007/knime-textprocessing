/*
 * ------------------------------------------------------------------------
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
 * ------------------------------------------------------------------------
 */
package org.knime.ext.textprocessing.nodes.misc.stringmatcher;

import org.knime.core.data.StringValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;

/**The configuration dialog for the string matcher.
 *
 * @author Iris Adae, University of Konstanz
 *
 */
public class StringMatcherNodeDialog extends DefaultNodeSettingsPane {

    /** String for first column. */
     private final SettingsModelString m_col1;
     /** String for second column.*/
    private final SettingsModelString m_col2;
    /** boolean for to be sorted in memory or not. */
    private final SettingsModelBoolean m_sortInMemory;
    /** boolean for showing the found minimal distance. */
    private final SettingsModelBoolean m_showdist;
    /** the Panel for the cost. */
    private CostPanel m_costPanel;
    /** Integer for the maximal number of related words. */
    private final SettingsModelInteger m_numberofrelatedwords;

    /**
     * Constructor for StringMatcherNodeDialog.
     */
    @SuppressWarnings("unchecked")
    public StringMatcherNodeDialog() {
          super();
        m_col1 = new SettingsModelString(StringMatcherNodeModel.CFG_COL1, null);
        m_col2 = new SettingsModelString(StringMatcherNodeModel.CFG_COL2, null);
        m_numberofrelatedwords = new SettingsModelIntegerBounded(
                        StringMatcherNodeModel.CFG_NUMBER, 3, 1, Integer.MAX_VALUE);
        m_sortInMemory = new SettingsModelBoolean(StringMatcherNodeModel.CFG_SORT_IN_MEMORY, false);
        m_showdist = new SettingsModelBoolean(StringMatcherNodeModel.CFG_SHOW_DISTANCE, true);

        // adding the tab to configure the costs.
        m_costPanel = new CostPanel();
        addTab("Cost Panel", m_costPanel);

        // adding the column selection
        addDialogComponent(new DialogComponentColumnNameSelection(
                            m_col1, "Search string column:", 0, StringValue.class));
        addDialogComponent(new DialogComponentColumnNameSelection(m_col2, "Dictionary column:", 1, StringValue.class));
        addDialogComponent(new DialogComponentNumber(m_numberofrelatedwords, StringMatcherNodeModel.CFG_NUMBER, 1, 5));

        // adding the buttons.
        setHorizontalPlacement(true);

        addDialogComponent(new DialogComponentBoolean(m_sortInMemory, "Process in memory"));
        addDialogComponent(new DialogComponentBoolean(m_showdist, "Display the found distance"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveAdditionalSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        assert settings != null;
        m_costPanel.saveSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadAdditionalSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs)
        throws NotConfigurableException {
        assert settings != null;
        assert specs != null;
        try {
            m_costPanel.loadSettings(settings);
        } catch (InvalidSettingsException e) {
            e.printStackTrace();
        }
    }
}
