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
 * ---------------------------------------------------------------------
 *
 * Created on 08.05.2013 by Kilian Thiel
 */
package org.knime.ext.textprocessing.nodes.view.documentviewer2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.knime.ext.textprocessing.data.TagFactory;
import org.knime.ext.textprocessing.nodes.view.documentviewer.SearchEngines;
import org.knime.ext.textprocessing.util.ImgLoaderUtil;

/**
 *
 * @author Hermann Azong, KNIME.com, Berlin, Germany
 */
class DocumentControlPanel2 extends JPanel {

    /**
     * Automatically generated serial version id.
     */
    private static final long serialVersionUID = -167060303181645711L;

    private final DocumentViewModel m_docViewModel;

    private final JToggleButton m_hiliteTagsButton;

    private final JToggleButton m_displayTagsButton;

    private final JToggleButton m_disableHtmlTags;

    private final JComboBox<String> m_tagTypes;

    private final JComboBox<String> m_linkSourcesBox;

    private final JButton m_colorChooserButton;

    private final JTextField m_searchField;

    private final JToggleButton m_searchButton;

    /**
     * Creates new instance of {@code DocumentControlPanel} with given document view model.
     *
     * @param docViewModel The document view model to use to set gui input to.
     */
    public DocumentControlPanel2(final DocumentViewModel docViewModel) {

        if (docViewModel == null) {
            throw new IllegalArgumentException("Document view model may not be null!");
        }
        m_docViewModel = docViewModel;

        setLayout(new BorderLayout());

        JPanel panelContainer = new JPanel();
        panelContainer.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel hiliteToolbar = new JPanel();
        hiliteToolbar.setLayout(new BorderLayout());

        JPanel searchToolbar = new JPanel();
        searchToolbar.setLayout(new BorderLayout());

        // Hilite box Component
        JPanel innerHiliteToolbar = new JPanel();
        innerHiliteToolbar.setLayout(new GridBagLayout());
        GridBagConstraints hGBC = new GridBagConstraints();
        hGBC.fill = GridBagConstraints.HORIZONTAL;
        hGBC.insets = new Insets(5, 3, 20, 3);

        // hilite button
        m_hiliteTagsButton = new JToggleButton();
        m_hiliteTagsButton.setSelected(DocumentViewPanel2.HILITE_TAGS);
        m_hiliteTagsButton.addActionListener(new HiliteActionListener());
        ImageIcon icon = ImgLoaderUtil.loadImageIcon("marker.png", "Hilite tags");
        m_hiliteTagsButton.setIcon(icon);
        m_hiliteTagsButton.setToolTipText("click to hilite tagged terms");
        hGBC.gridx = 0;
        hGBC.gridy = 0;
        innerHiliteToolbar.add(m_hiliteTagsButton, hGBC);

        // color chooser
        m_colorChooserButton = new JButton();
        icon = ImgLoaderUtil.loadImageIcon("color.png", "Color");
        m_colorChooserButton.setIcon(icon);
        m_colorChooserButton.setToolTipText("Choose Color");
        m_colorChooserButton.setBorder(BorderFactory.createLineBorder(DocumentViewPanel2.DEFAULT_ENTITY_COLOR));
        m_colorChooserButton.setBorderPainted(true);
        m_colorChooserButton.addActionListener(new ColorButtonListener());
        m_colorChooserButton.setOpaque(true);
        m_colorChooserButton.setPreferredSize(m_hiliteTagsButton.getPreferredSize());
        hGBC.gridx = 1;
        hGBC.gridy = 0;
        innerHiliteToolbar.add(m_colorChooserButton, hGBC);

        // tag combo box
        m_tagTypes = new JComboBox<String>();
        Set<String> tagTypes = TagFactory.getInstance().getTagTypes();
        for (String tagType : tagTypes) {
            m_tagTypes.addItem(tagType);
        }
        m_tagTypes.addActionListener(new HiliteActionListener());
        hGBC.gridx = 2;
        hGBC.gridy = 0;
        innerHiliteToolbar.add(m_tagTypes, hGBC);

        // links sources
        JLabel label1 = new JLabel("Link to:");
        hGBC.gridwidth = 1;
        hGBC.gridx = 0;
        hGBC.gridy = 1;
        innerHiliteToolbar.add(label1, hGBC);
        m_linkSourcesBox = new JComboBox<String>();
        hGBC.gridwidth = 2;
        hGBC.gridx = 1;
        hGBC.gridy = 1;
        for (String source : SearchEngines.getInstance().getSearchEngineNames()) {
            m_linkSourcesBox.addItem(source);
        }

        m_linkSourcesBox.setSelectedItem(SearchEngines.getInstance().getDefaultSource());
        m_linkSourcesBox.setEnabled(m_hiliteTagsButton.isSelected());
        m_linkSourcesBox.addActionListener(new LinkSourceListener());
        innerHiliteToolbar.add(m_linkSourcesBox, hGBC);

        // tags display button
        JLabel label2 = new JLabel("tags on/off: ");
        hGBC.gridx = 0;
        hGBC.gridy = 2;
        innerHiliteToolbar.add(label2, hGBC);
        m_displayTagsButton = new JToggleButton();
        m_displayTagsButton.setSelected(DocumentViewPanel2.DISPLAY_TAGS);
        m_displayTagsButton.addActionListener(new DisplayListener());
        ImageIcon icon_1 = ImgLoaderUtil.loadImageIcon("tag_blue.png", "OFF");
        m_displayTagsButton.setIcon(icon_1);
        m_displayTagsButton.setPreferredSize(new Dimension(53, 26));
        m_displayTagsButton.setToolTipText("click here to display tags");
        hGBC.gridx = 1;
        hGBC.gridy = 2;
        hGBC.fill = GridBagConstraints.NONE;
        hGBC.anchor = GridBagConstraints.EAST;
        innerHiliteToolbar.add(m_displayTagsButton, hGBC);
        hGBC.fill = GridBagConstraints.HORIZONTAL;

        // html display button
        JLabel label3 = new JLabel("html on/off: ");
        hGBC.gridx = 0;
        hGBC.gridy = 3;
        innerHiliteToolbar.add(label3, hGBC);
        m_disableHtmlTags = new JToggleButton();
        m_disableHtmlTags.setSelected(DocumentViewPanel2.DISABLE_HTML_TAGS);
        m_disableHtmlTags.addActionListener(new DisableListener());
        ImageIcon icon_2 = ImgLoaderUtil.loadImageIcon("html.png", "OFF");
        m_disableHtmlTags.setIcon(icon_2);
        m_disableHtmlTags.setPreferredSize(new Dimension(53, 26));
        m_disableHtmlTags.setToolTipText("display the original document with html interpretation");
        hGBC.gridx = 1;
        hGBC.gridy = 3;
        hGBC.fill = GridBagConstraints.NONE;
        hGBC.anchor = GridBagConstraints.EAST;
        innerHiliteToolbar.add(m_disableHtmlTags, hGBC);
        hGBC.fill = GridBagConstraints.HORIZONTAL;



        JPanel innerSearchToolbar = new JPanel();
        innerSearchToolbar.setLayout(new GridBagLayout());
        GridBagConstraints sGBC = new GridBagConstraints();
        sGBC.fill = GridBagConstraints.HORIZONTAL;
        sGBC.insets = new Insets(5, 3, 5, 3);

        // search button
        m_searchButton = new JToggleButton();
        m_searchButton.addActionListener(new SearchListener());
        icon = ImgLoaderUtil.loadImageIcon("search.png", "Search");
        m_searchButton.setIcon(icon);
        m_searchButton.setToolTipText("click to hilite search results");
        sGBC.gridx = 0;
        sGBC.gridy = 0;
        innerSearchToolbar.add(m_searchButton, sGBC);

        // search field
        m_searchField = new JTextField("[a-z]+");
        m_searchField.setColumns(10);
        m_searchField.getDocument().addDocumentListener(new SearchListener());
        m_searchField.setToolTipText("enter regular expression");
        m_searchField.setPreferredSize(m_searchButton.getPreferredSize());
        sGBC.gridx = 1;
        sGBC.gridy = 0;
        innerSearchToolbar.add(m_searchField, sGBC);

        // Border for the box
        Border border = new LineBorder(Color.gray, 1);
        Font font = new Font("sans-serif", Font.ITALIC, 12);
        Border se = new TitledBorder(border, "Search", TitledBorder.LEFT, TitledBorder.TOP, font, Color.gray);
        Border hi = new TitledBorder(border, "Highlighting", TitledBorder.LEFT, TitledBorder.TOP, font, Color.gray);

        searchToolbar.setBorder(se);
        hiliteToolbar.setBorder(hi);

        searchToolbar.add(innerSearchToolbar, BorderLayout.WEST);
        hiliteToolbar.add(innerHiliteToolbar, BorderLayout.WEST);

        // adding the Search box container
        gbc.gridx = 0;
        gbc.gridy = 0;
        panelContainer.add(searchToolbar, gbc);

        // adding the Hilite box container
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        panelContainer.add(hiliteToolbar, gbc);

        // Set all to the top
        add(panelContainer, BorderLayout.NORTH);

    }


    private void updateDocumentViewModel() {
        m_docViewModel.setDisableHtmlTags(m_disableHtmlTags.isSelected());
        m_docViewModel.setHiliteTags(m_hiliteTagsButton.isSelected());
        m_docViewModel.setDisplayTags(m_displayTagsButton.isSelected());
        m_docViewModel.setTagType(m_tagTypes.getSelectedItem().toString());
        m_docViewModel.setHiliteSearch(m_searchButton.isSelected());
        m_docViewModel.setSearchString(m_searchField.getText());
        if (m_linkSourcesBox.getSelectedItem() != null) {
            m_docViewModel.setLinkSourceName(m_linkSourcesBox.getSelectedItem().toString());
        }

        m_docViewModel.modelChanged();
    }

    /**
     * Sets (un-)hilited text when action was performed.
     *
     * @author Kilian Thiel, KNIME AG, Zurich, Switzerland
     */
    private class HiliteActionListener implements ActionListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void actionPerformed(final ActionEvent arg0) {
            updateDocumentViewModel();
            m_linkSourcesBox.setEnabled(m_hiliteTagsButton.isSelected());

        }
    }

    /**
     *
     *
     * @author Hermann Azong, KNIME.com, Berlin, Germany
     */
    private class DisplayListener implements ActionListener{

        /**
         * {@inheritDoc}
         */
        @Override
        public void actionPerformed(final ActionEvent e) {
            updateDocumentViewModel();
            if(m_displayTagsButton.isSelected()){
                ImageIcon icon_on = ImgLoaderUtil.loadImageIcon("tag_blue.png", "ON");
                m_displayTagsButton.setIcon(icon_on);
                m_displayTagsButton.setToolTipText("click here to disable tags");
            } else {
                m_displayTagsButton.setToolTipText("click here to display tags");

            }


        }

    }

    /**
     *
     *
     * @author Hermann Azong, KNIME.com, Berlin, Germany
     */

    private class DisableListener implements ActionListener{

        /**
         * {@inheritDoc}
         */
        @Override
        public void actionPerformed(final ActionEvent e) {
            updateDocumentViewModel();
            if(!m_disableHtmlTags.isSelected()){
                //ImageIcon icon_2 = ImgLoaderUtil.loadImageIcon("html.png", "ON");
                //m_disableHtmlTags.setIcon(icon_2);
                m_disableHtmlTags.setToolTipText("display the original document with html interpretation");
            } else {
                m_disableHtmlTags.setToolTipText("display the original document without html interpretation");
            }

        }

    }

    /**
     *
     * @author Hermann Azong, KNIME.com, Berlin, Germany
     */
    private class SearchListener implements ActionListener, DocumentListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void actionPerformed(final ActionEvent e) {
            updateDocumentViewModel();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void insertUpdate(final DocumentEvent e) {
            updateDocumentViewModel();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void removeUpdate(final DocumentEvent e) {
            updateDocumentViewModel();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void changedUpdate(final DocumentEvent e) {
            updateDocumentViewModel();
        }
    }

    /**
     *
     * @author Hermann Azong, KNIME.com, Berlin, Germany
     */
    private class LinkSourceListener implements ActionListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void actionPerformed(final ActionEvent e) {
            updateDocumentViewModel();
        }
    }

    /**
     *
     * @author Hermann Azong, KNIME.com, Berlin, Germany
     */
    private class ColorButtonListener implements ActionListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void actionPerformed(final ActionEvent e) {
            Color taggedEntityColor =
                JColorChooser.showDialog(null, "Choose hilite color", m_docViewModel.getTaggedEntityColor());
            if (taggedEntityColor == null) {
                taggedEntityColor = Color.RED;
            }
            m_colorChooserButton.setOpaque(true);
            m_colorChooserButton.setBorder(BorderFactory.createLineBorder(taggedEntityColor));
            repaint();
            m_docViewModel.setTaggedEntityColor(taggedEntityColor);
            updateDocumentViewModel();
        }
    }

}
